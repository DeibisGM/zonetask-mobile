package com.app.zonetask.ui.screens.taskcreate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TaskCreateViewModel : ViewModel() {

    var uiState by mutableStateOf(TaskCreateUiState())
        private set
    
    fun updateState(block: TaskCreateUiState.() -> TaskCreateUiState) {
        uiState = uiState.block()
    }

    fun validate(): Boolean {
        val isValid = uiState.isValid
        uiState = uiState.copy(
            isTitleValid = uiState.title.isNotBlank(),
            showErrors = !isValid
        )
        return isValid
    }
}
