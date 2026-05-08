package com.app.zonetask.ui.screens.taskcreate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.launch

class TaskCreateViewModel : ViewModel() {

    var uiState by mutableStateOf(TaskCreateUiState())
        private set

    var formOptionsUiState by mutableStateOf(TaskFormOptionsUiState())
        private set

    init {
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

    private fun TaskCreateUiState.revalidate(showErrors: Boolean = this.showErrors): TaskCreateUiState {
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
