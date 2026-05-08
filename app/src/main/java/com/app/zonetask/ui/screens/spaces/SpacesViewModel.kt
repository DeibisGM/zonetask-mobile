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

class SpacesViewModel(
    private val spaceRepository: SpaceRepository,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpacesUiState())
    val uiState: StateFlow<SpacesUiState> = _uiState.asStateFlow()

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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        spaces    = result.data,
                        errorBanner = null
                    )
                }

                is ApiResult.Error -> {
                    // Preserve existing list so the user doesn't lose data on a failed refresh
                    _uiState.value = _uiState.value.copy(
                        isLoading   = false,
                        errorBanner = result.message
                    )
                }
            }
        }
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }
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