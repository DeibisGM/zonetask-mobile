package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.SpaceRepository
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpaceDetailViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceDetailUiState())
    val uiState: StateFlow<SpaceDetailUiState> = _uiState.asStateFlow()

    init {
        loadSpace()
        loadTasks()
    }

    fun loadSpace() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorBanner = null)

        viewModelScope.launch {
            when (val result = spaceRepository.getSpaceById(spaceId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        space = result.data,
                        errorBanner = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorBanner = result.message
                    )
                }
            }
        }
    }

    fun loadTasks() {
        _uiState.value = _uiState.value.copy(tasksLoading = true, tasksError = null)

        viewModelScope.launch {
            when (val result = AppContainer.taskRepository.getTasksBySpace(spaceId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        tasksLoading = false,
                        tasks = result.data,
                        tasksError = null
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        tasksLoading = false,
                        tasksError = result.message
                    )
                }
            }
        }
    }
}

class SpaceDetailViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceDetailViewModel(
            spaceRepository = spaceRepository,
            spaceId         = spaceId
        ) as T
}
