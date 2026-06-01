package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateSpaceRequest
import com.app.zonetask.data.repository.SpaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateSpaceViewModel(
    private val spaceRepository: SpaceRepository,
    private val ownerId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSpaceUiState())
    val uiState: StateFlow<CreateSpaceUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorBanner = null)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, errorBanner = null)
    }

    fun onSpaceTypeChange(value: String) {
        _uiState.value = _uiState.value.copy(spaceType = value, errorBanner = null)
    }

    fun onCoverImageUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(coverImageUrl = value, errorBanner = null)
    }

    fun onRotationTypeChange(value: String) {
        _uiState.value = _uiState.value.copy(rotationType = value, errorBanner = null)
    }

    fun onRequireProofChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(requireProof = value, errorBanner = null)
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun createSpace() {
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
            val request = CreateSpaceRequest(
                name          = state.name.trim(),
                description   = state.description.trim().ifBlank { null },
                spaceType     = state.spaceType.trim(),
                coverImageUrl = state.coverImageUrl.trim().ifBlank { null },
                ownerId       = ownerId,
                rotationType  = state.rotationType.trim().ifBlank { null },
                requireProof  = state.requireProof
            )

            when (val result = spaceRepository.createSpace(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
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
}

class CreateSpaceViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val ownerId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CreateSpaceViewModel(
            spaceRepository = spaceRepository,
            ownerId         = ownerId
        ) as T
}