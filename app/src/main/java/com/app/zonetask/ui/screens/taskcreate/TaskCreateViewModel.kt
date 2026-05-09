package com.app.zonetask.ui.screens.taskcreate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.launch

class TaskCreateViewModel : ViewModel() {

    var uiState by mutableStateOf(TaskCreateUiState())
        private set

    var formOptionsUiState by mutableStateOf(TaskFormOptionsUiState())
        private set

    init {
        // Load dropdown data as soon as the screen opens.
        loadFormOptions()
    }
    
    fun updateState(block: TaskCreateUiState.() -> TaskCreateUiState) {
        uiState = uiState.block().revalidate()
    }

    fun validate(): Boolean {
        val validatedState = uiState.revalidate(showErrors = true)
        uiState = validatedState
        return validatedState.isValid
    }

    fun loadFormOptions(spaceId: Int = 1) {
        viewModelScope.launch {
            formOptionsUiState = formOptionsUiState.copy(isLoading = true, errorMessage = null)

            when (val result = AppContainer.taskLookupRepository.getTaskFormOptions(spaceId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    formOptionsUiState = TaskFormOptionsUiState(
                        categories = data.categories.map { it.name to it.id.toString() },
                        zones = data.zones.map { it.name to it.id.toString() },
                        isLoading = false,
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    formOptionsUiState = formOptionsUiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun loadZoneObjects(zoneId: Int) {
        viewModelScope.launch {
            formOptionsUiState = formOptionsUiState.copy(objectsLoading = true, errorMessage = null)

            when (val result = AppContainer.taskLookupRepository.getZoneObjects(zoneId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    formOptionsUiState = formOptionsUiState.copy(
                        objects = data.map { it.name to it.id.toString() },
                        objectsLoading = false,
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    formOptionsUiState = formOptionsUiState.copy(
                        objects = emptyList(),
                        objectsLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun saveTask(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // The API expects time in HH:mm:ss format.
            val normalizedTime = uiState.scheduledTime
                .takeIf { it.isNotBlank() }
                ?.let { value ->
                    if (value.count { it == ':' } == 1) {
                        "$value:00"
                    } else {
                        value
                    }
                }

            val request = CreateTaskRequestDto(
                title = uiState.title.trim(),
                description = uiState.description.takeIf { it.isNotBlank() }?.trim(),
                frequency = uiState.frequency,
                recurrenceRule = uiState.recurrenceRule,
                scheduledTime = normalizedTime,
                startDate = uiState.startDate.ifBlank { null },
                endDate = uiState.endDate,
                rotating = uiState.rotating,
                isActive = uiState.isActive,
                reminderEnabled = uiState.reminderEnabled,
                reminderMinutes = uiState.reminderMinutes,
                requiresProof = uiState.requiresProof,
                requiresDescription = uiState.requiresDescription,
                estimatedMinutes = uiState.estimatedMinutes,
                createdBy = uiState.createdBy,
                categoryId = uiState.categoryId,
                spaceId = uiState.spaceId,
                zoneId = uiState.zoneId,
                objectId = uiState.selectedObjectIds.firstOrNull(),
                objectIds = if (uiState.objectSelectionEnabled) uiState.selectedObjectIds else emptyList()
            )

            // Return the API result to the UI so it can show success or error.
            when (val result = AppContainer.taskRepository.createTask(request)) {
                is ApiResult.Success -> onResult(true, "Tarea guardada")
                is ApiResult.Error -> onResult(false, result.message)
            }
        }
    }

    fun resetForm() {
        // Start over with a fresh form after a successful save.
        uiState = TaskCreateUiState()
    }

    private fun TaskCreateUiState.revalidate(showErrors: Boolean = this.showErrors): TaskCreateUiState {
        // Keep validation state in sync while the user types.
        val titleValid = title.isNotBlank()
        val startDateValid = startDate.isNotBlank()
        val timeValid = scheduledTime.isNotBlank()
        val estimatedTimeValid = estimatedMinutes != null

        return copy(
            isTitleValid = titleValid,
            isStartDateValid = startDateValid,
            isTimeValid = timeValid,
            isEstimatedTimeValid = estimatedTimeValid,
            showErrors = showErrors
        )
    }
}
