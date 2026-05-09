package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.SpaceRepository
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
            when (val result = spaceRepository.getSpaceById(spaceId)) {
                is ApiResult.Success -> {
                    val space = result.data

                    val userRole = if (space.ownerId == userId) "owner" else "member"

                    _uiState.value = SpaceDetailUiState(
                        space    = space,
                        userRole = userRole
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = SpaceDetailUiState(errorBanner = result.message)
                }
            }
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
