package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.UserMessages
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.SpaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpacesViewModel(
    private val spaceRepository: SpaceRepository,
    val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpacesUiState())
    val uiState: StateFlow<SpacesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SpacesEvent>()
    val events: SharedFlow<SpacesEvent> = _events.asSharedFlow()

    init {
        fetchSpaces()
    }

    fun fetchSpaces() {
        _uiState.value = _uiState.value.copy(
            isLoading   = true,
            errorBanner = null
        )

        viewModelScope.launch {
            when (val result = spaceRepository.getSpacesByUser(userId)) {
                is ApiResult.Success -> {
                    val spaces = result.data

                    // Pedir los permisos de todos los espacios en paralelo
                    val rolesMap = spaces
                        .map { space ->
                            async {
                                val permResult = spaceRepository.getSpacePermissions(
                                    spaceId = space.spaceId,
                                    userId  = userId
                                )
                                val role = when {
                                    permResult is ApiResult.Success ->
                                        permResult.data.requestingUserRole
                                    space.ownerId == userId -> "owner"
                                    else -> "member"
                                }
                                space.spaceId to role
                            }
                        }
                        .awaitAll()
                        .toMap()

                    _uiState.value = _uiState.value.copy(
                        isLoading   = false,
                        spaces      = spaces,
                        spaceRoles  = rolesMap,
                        errorBanner = null
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading   = false,
                        errorBanner = result.message
                    )
                }
            }
        }
    }

    fun deleteSpace(spaceId: Int) {
        if (_uiState.value.deletingSpaceId != null) return

        _uiState.value = _uiState.value.copy(deletingSpaceId = spaceId)

        viewModelScope.launch {
            when (val result = spaceRepository.deleteSpace(spaceId, userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        deletingSpaceId = null,
                        spaces          = _uiState.value.spaces.filter { it.spaceId != spaceId },
                        spaceRoles      = _uiState.value.spaceRoles - spaceId
                    )
                    _events.emit(SpacesEvent.ShowMessage(UserMessages.Spaces.DELETE_SUCCESS))
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(deletingSpaceId = null)
                    val message = if (result.statusCode == 403) {
                        UserMessages.Spaces.DELETE_FORBIDDEN
                    } else {
                        result.message
                    }
                    _events.emit(SpacesEvent.ShowMessage(message))
                }
            }
        }
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun notifyDeleteNotAllowed() {
        viewModelScope.launch {
            _events.emit(SpacesEvent.ShowMessage(UserMessages.Spaces.DELETE_NOT_OWNER))
        }
    }
}

sealed class SpacesEvent {
    data class ShowMessage(val message: String) : SpacesEvent()
}

class SpacesViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpacesViewModel(
            spaceRepository = spaceRepository,
            userId          = userId
        ) as T
}
