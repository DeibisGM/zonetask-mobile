package com.app.zonetask.ui.screens.spacemembers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.SpaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpaceMembersViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceMembersUiState())
    val uiState: StateFlow<SpaceMembersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val membersDeferred     = async { spaceRepository.getMemberDirectory(spaceId, userId) }
            val invitationsDeferred = async { spaceRepository.getSpacePendingInvitations(spaceId, userId) }

            val membersResult     = membersDeferred.await()
            val invitationsResult = invitationsDeferred.await()

            val members = when (membersResult) {
                is ApiResult.Success -> membersResult.data
                is ApiResult.Error   -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = membersResult.message
                    )
                    return@launch
                }
            }

            val invitations = when (invitationsResult) {
                is ApiResult.Success -> invitationsResult.data
                is ApiResult.Error   -> emptyList() // 403 for non-admins — silently ignored
            }

            _uiState.value = _uiState.value.copy(
                isLoading          = false,
                members            = members,
                pendingInvitations = invitations
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

class SpaceMembersViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceMembersViewModel(spaceRepository = spaceRepository, spaceId = spaceId, userId = userId) as T
}