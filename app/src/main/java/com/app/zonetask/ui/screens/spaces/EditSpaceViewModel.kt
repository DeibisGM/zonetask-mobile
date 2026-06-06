package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.EditSpaceRequest
import com.app.zonetask.data.repository.SpaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditSpaceViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditSpaceUiState(spaceId = spaceId))
    val uiState: StateFlow<EditSpaceUiState> = _uiState.asStateFlow()

    init {
        loadSpace()
    }
    fun loadSpace() {
        _uiState.value = _uiState.value.copy(isLoadingData = true, errorBanner = null)
        viewModelScope.launch {
            when (val result = spaceRepository.getSpaceById(spaceId)) {
                is ApiResult.Success -> {
                    val space = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoadingData = false,
                        name          = space.name,
                        description   = space.description ?: "",
                        spaceType     = space.spaceType,
                        coverImageUrl = space.coverImageUrl ?: ""
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingData = false,
                        errorBanner   = result.message
                    )
                }
            }
        }
    }

    fun onNameChange(value: String)          { _uiState.value = _uiState.value.copy(name = value, errorBanner = null) }
    fun onDescriptionChange(value: String)   { _uiState.value = _uiState.value.copy(description = value, errorBanner = null) }
    fun onSpaceTypeChange(value: String)     { _uiState.value = _uiState.value.copy(spaceType = value, errorBanner = null) }
    fun onCoverImageUrlChange(value: String) { _uiState.value = _uiState.value.copy(coverImageUrl = value, errorBanner = null) }
    fun clearErrorBanner()                   { _uiState.value = _uiState.value.copy(errorBanner = null) }

    fun updateSpace() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorBanner = "Space name is required")
            return
        }
        if (state.spaceType.isBlank()) {
            _uiState.value = state.copy(errorBanner = "Space type is required")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorBanner = null)

        viewModelScope.launch {
            val request = EditSpaceRequest(
                name          = state.name.trim(),
                description   = state.description.trim().ifBlank { null },
                spaceType     = state.spaceType.trim(),
                coverImageUrl = state.coverImageUrl.trim().ifBlank { null }
            )

            when (val result = spaceRepository.updateSpace(spaceId, request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorBanner = result.message)
                }
            }
        }
    }
}

class EditSpaceViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EditSpaceViewModel(spaceRepository = spaceRepository, spaceId = spaceId) as T
}