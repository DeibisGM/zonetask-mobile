package com.app.zonetask.ui.screens.spaces

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

class SpaceDetailViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceDetailUiState())
    val uiState: StateFlow<SpaceDetailUiState> = _uiState.asStateFlow()

    init {
        loadSpace()
    }

    fun loadSpace() {
        _uiState.value = SpaceDetailUiState(isLoading = true)

        viewModelScope.launch {
            val spaceDeferred       = async { spaceRepository.getSpaceById(spaceId) }
            val permissionsDeferred = async { spaceRepository.getSpacePermissions(spaceId, userId) }

            val spaceResult       = spaceDeferred.await()
            val permissionsResult = permissionsDeferred.await()

            if (spaceResult is ApiResult.Error) {
                _uiState.value = SpaceDetailUiState(errorBanner = spaceResult.message)
                return@launch
            }

            val space = (spaceResult as ApiResult.Success).data

            val userRole = when {
                permissionsResult is ApiResult.Success -> permissionsResult.data.requestingUserRole
                space.ownerId == userId               -> "owner"
                else                                  -> "member"
            }

            _uiState.value = SpaceDetailUiState(
                space    = space,
                userRole = userRole
            )
        }
    }
}

class SpaceDetailViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceDetailViewModel(
            spaceRepository = spaceRepository,
            spaceId         = spaceId,
            userId          = userId
        ) as T
}
