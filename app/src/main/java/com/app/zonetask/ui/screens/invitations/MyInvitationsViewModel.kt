package com.app.zonetask.ui.screens.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.UserMessages
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.InvitationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyInvitationsViewModel(
    private val invitationRepository: InvitationRepository,
    private val userId: Int,
    private val email: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyInvitationsUiState())
    val uiState: StateFlow<MyInvitationsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MyInvitationsEvent>()
    val events: SharedFlow<MyInvitationsEvent> = _events.asSharedFlow()

    init {
        loadInvitations()
    }

    fun loadInvitations() {
        if (email.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorBanner = UserMessages.Invitations.MISSING_IDENTITY)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorBanner = null) }

        viewModelScope.launch {
            when (val result = invitationRepository.getInvitationsByUser(userId, email)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, invitations = result.data, errorBanner = null)
                }

                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorBanner = result.message)
                }
            }
        }
    }

    fun respond(invitationId: Int, accepted: Boolean) {
        if (_uiState.value.respondingInvitationId != null) return

        _uiState.update { it.copy(respondingInvitationId = invitationId) }

        viewModelScope.launch {
            val result = invitationRepository.respondToInvitation(
                invitationId = invitationId,
                accepted     = accepted,
                userId       = userId,
                email        = email
            )

            when (result) {
                is ApiResult.Success -> {
                    // The returned DTO carries the new status, so the badge and
                    // buttons update in place without a refetch.
                    _uiState.update { state ->
                        state.copy(
                            respondingInvitationId = null,
                            invitations = state.invitations.map {
                                if (it.invitationId == invitationId) result.data else it
                            }
                        )
                    }
                    val message = if (accepted) {
                        UserMessages.Invitations.ACCEPT_SUCCESS
                    } else {
                        UserMessages.Invitations.REJECT_SUCCESS
                    }
                    _events.emit(MyInvitationsEvent.ShowMessage(message))
                }

                is ApiResult.Error -> {
                    _uiState.update { it.copy(respondingInvitationId = null) }
                    val message = when (result.statusCode) {
                        400  -> UserMessages.Invitations.RESPOND_UNAVAILABLE
                        403  -> UserMessages.Invitations.RESPOND_FORBIDDEN
                        404  -> UserMessages.Invitations.RESPOND_NOT_FOUND
                        else -> result.message
                    }
                    _events.emit(MyInvitationsEvent.ShowMessage(message))

                    // A 400 means the backend changed the invitation state
                    // (lazily expired or already responded). Refetch so the badge
                    // reflects the real status instead of a stale "pending".
                    if (result.statusCode == 400) loadInvitations()
                }
            }
        }
    }
}

sealed class MyInvitationsEvent {
    data class ShowMessage(val message: String) : MyInvitationsEvent()
}

class MyInvitationsViewModelFactory(
    private val invitationRepository: InvitationRepository,
    private val userId: Int,
    private val email: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MyInvitationsViewModel(
            invitationRepository = invitationRepository,
            userId               = userId,
            email                = email
        ) as T
}
