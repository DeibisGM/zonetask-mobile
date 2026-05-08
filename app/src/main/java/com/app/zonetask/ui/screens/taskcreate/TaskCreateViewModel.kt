package com.app.zonetask.ui.screens.taskcreate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TaskCreateViewModel : ViewModel() {

    var uiState by mutableStateOf(TaskCreateUiState())
        private set
    
    fun updateState(block: TaskCreateUiState.() -> TaskCreateUiState) {
        uiState = uiState.block().revalidate()
    }

    fun validate(): Boolean {
        val validatedState = uiState.revalidate(showErrors = true)
        uiState = validatedState
        return validatedState.isValid
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
