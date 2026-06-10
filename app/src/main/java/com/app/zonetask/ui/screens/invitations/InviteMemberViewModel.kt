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

class InviteMemberViewModel(
    private val invitationRepository: InvitationRepository,
    private val spaceId: Int,
    private val invitedBy: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteMemberUiState())
    val uiState: StateFlow<InviteMemberUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<InviteMemberEvent>()
    val events: SharedFlow<InviteMemberEvent> = _events.asSharedFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onMessageChange(value: String) {
        // Backend caps the message at 255 chars (SpaceInvitationInput.message).
        _uiState.update { it.copy(message = value.take(MAX_MESSAGE_LENGTH)) }
    }

    fun submit() {
        val current = _uiState.value
        if (current.isSending) return

        val email = current.email.trim()
        if (!EMAIL_REGEX.matches(email)) {
            _uiState.update { it.copy(emailError = UserMessages.Invitations.INVALID_EMAIL) }
            return
        }

        _uiState.update { it.copy(isSending = true, emailError = null) }

        viewModelScope.launch {
            val result = invitationRepository.createInvitation(
                spaceId   = spaceId,
                invitedBy = invitedBy,
                email     = email,
                message   = current.message.trim().ifBlank { null }
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSending = false, email = "", message = "")
                    }
                    _events.emit(InviteMemberEvent.ShowMessage(UserMessages.Invitations.SEND_SUCCESS))
                }

                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSending = false) }
                    val message = when (result.statusCode) {
                        403  -> UserMessages.Invitations.FORBIDDEN
                        404  -> UserMessages.Invitations.NOT_REGISTERED
                        409  -> UserMessages.Invitations.CONFLICT
                        else -> result.message
                    }
                    _events.emit(InviteMemberEvent.ShowMessage(message))
                }
            }
        }
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 255

        // Local regex instead of android.util.Patterns so the ViewModel stays
        // unit-testable without an Android runtime / Robolectric.
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

sealed class InviteMemberEvent {
    data class ShowMessage(val message: String) : InviteMemberEvent()
}

class InviteMemberViewModelFactory(
    private val invitationRepository: InvitationRepository,
    private val spaceId: Int,
    private val invitedBy: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        InviteMemberViewModel(
            invitationRepository = invitationRepository,
            spaceId              = spaceId,
            invitedBy            = invitedBy
        ) as T
}