package com.app.zonetask.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateFloorPlanRequest
import com.app.zonetask.data.remote.dto.UpdateFloorPlanRequest
import com.app.zonetask.data.repository.FloorPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlanEditorViewModel(
    private val repository: FloorPlanRepository,
    private val spaceId:    Int,
    private val planId:     Int?   // null = new plan
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlanEditorUiState(planId = planId, spaceId = spaceId)
    )
    val uiState: StateFlow<PlanEditorUiState> = _uiState.asStateFlow()

    init {
        if (planId != null) loadPlan(planId)
    }

    private fun loadPlan(id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorBanner = null)
        viewModelScope.launch {
            when (val result = repository.getPlanById(id)) {
                is ApiResult.Success -> {
                    val plan = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        planId       = plan.planId,
                        name         = plan.name,
                        canvasWidth  = plan.canvasWidth.toInt().toString(),
                        canvasHeight = plan.canvasHeight.toInt().toString(),
                        isDirty      = false
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

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, isDirty = true, errorBanner = null)
    }

    fun onCanvasWidthChange(value: String) {
        _uiState.value = _uiState.value.copy(canvasWidth = value, isDirty = true, errorBanner = null)
    }

    fun onCanvasHeightChange(value: String) {
        _uiState.value = _uiState.value.copy(canvasHeight = value, isDirty = true, errorBanner = null)
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorBanner = "Plan name is required")
            return
        }

        val width  = state.canvasWidth.toFloatOrNull()
        val height = state.canvasHeight.toFloatOrNull()

        if (width == null || width <= 0f) {
            _uiState.value = state.copy(errorBanner = "Width must be greater than 0")
            return
        }
        if (height == null || height <= 0f) {
            _uiState.value = state.copy(errorBanner = "Height must be greater than 0")
            return
        }

        _uiState.value = state.copy(isSaving = true, errorBanner = null)

        viewModelScope.launch {
            val result = if (state.planId == null) {
                // Create new plan
                repository.createPlan(
                    CreateFloorPlanRequest(
                        name         = state.name.trim(),
                        canvasWidth  = width,
                        canvasHeight = height,
                        spaceId      = state.spaceId
                    )
                )
            } else {
                // Update existing plan
                repository.updatePlan(
                    state.planId,
                    UpdateFloorPlanRequest(
                        name         = state.name.trim(),
                        canvasWidth  = width,
                        canvasHeight = height
                    )
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    val saved = result.data
                    _uiState.value = _uiState.value.copy(
                        planId       = saved.planId,
                        isSaving     = false,
                        isDirty      = false,
                        isSaved      = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving    = false,
                        errorBanner = result.message
                    )
                }
            }
        }
    }

    fun consumeSaved() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}

class PlanEditorViewModelFactory(
    private val repository: FloorPlanRepository,
    private val spaceId:    Int,
    private val planId:     Int?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlanEditorViewModel(repository, spaceId, planId) as T
}
