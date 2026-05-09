package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.UserMessages
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.SpaceRepository
import com.app.zonetask.domain.model.SpaceMember
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpacePermissionsViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpacePermissionsUiState())
    val uiState: StateFlow<SpacePermissionsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SpacePermissionsEvent>()
    val events: SharedFlow<SpacePermissionsEvent> = _events.asSharedFlow()

    init { loadPermissions() }

    fun loadPermissions() {
        _uiState.value = SpacePermissionsUiState(isLoading = true)

        viewModelScope.launch {
            val permissionsDeferred = async { spaceRepository.getSpacePermissions(spaceId, userId) }
            val membersDeferred     = async { spaceRepository.getSpaceMembers(spaceId, userId) }

            val permissionsResult = permissionsDeferred.await()
            val membersResult     = membersDeferred.await()

            if (permissionsResult is ApiResult.Error) {
                _uiState.value = SpacePermissionsUiState(errorBanner = permissionsResult.message)
                return@launch
            }

            val permissions = (permissionsResult as ApiResult.Success).data
            val members     = if (membersResult is ApiResult.Success) membersResult.data else emptyList()

            _uiState.value = SpacePermissionsUiState(
                isLoading          = false,
                requestingUserRole = permissions.requestingUserRole,
                roleActions        = permissions.roleActions,
                members            = members
            )
        }
    }

    fun requestRoleChange(member: SpaceMember) {
        _uiState.value = _uiState.value.copy(memberPendingRoleChange = member)
    }

    fun cancelRoleChange() {
        _uiState.value = _uiState.value.copy(memberPendingRoleChange = null)
    }

    fun confirmRoleChange(memberId: Int, newRole: String) {
        if (_uiState.value.updatingMemberId != null) return

        _uiState.value = _uiState.value.copy(
            memberPendingRoleChange = null,
            updatingMemberId        = memberId,
            roleUpdateError         = null
        )

        viewModelScope.launch {
            when (val result = spaceRepository.updateMemberRole(
                spaceId          = spaceId,
                memberId         = memberId,
                newRole          = newRole,
                requestingUserId = userId
            )) {
                is ApiResult.Success -> {
                    val updatedMember = result.data
                    val updatedList   = _uiState.value.members.map { m ->
                        if (m.memberId == updatedMember.memberId) updatedMember else m
                    }
                    _uiState.value = _uiState.value.copy(
                        updatingMemberId = null,
                        members          = updatedList
                    )
                    _events.emit(SpacePermissionsEvent.ShowMessage(
                        UserMessages.SpacePermissions.ROLE_UPDATE_SUCCESS
                    ))
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(updatingMemberId = null)
                    val message = when (result.statusCode) {
                        403  -> UserMessages.SpacePermissions.ROLE_UPDATE_FORBIDDEN
                        else -> result.message
                    }
                    _events.emit(SpacePermissionsEvent.ShowMessage(message))
                }
            }
        }
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }
}

sealed class SpacePermissionsEvent {
    data class ShowMessage(val message: String) : SpacePermissionsEvent()
}

class SpacePermissionsViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpacePermissionsViewModel(
            spaceRepository = spaceRepository,
            spaceId         = spaceId,
            userId          = userId
        ) as T
}