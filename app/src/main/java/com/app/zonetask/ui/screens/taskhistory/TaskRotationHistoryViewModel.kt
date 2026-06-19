package com.app.zonetask.ui.screens.taskhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskRotationHistoryViewModel(
    private val taskId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskRotationHistoryUiState())
    val uiState: StateFlow<TaskRotationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun onDateFromChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateFrom = value)
    }

    fun onDateToChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateTo = value)
    }

    fun onTriggerReasonChanged(value: String) {
        _uiState.value = _uiState.value.copy(triggerReason = value)
    }

    fun applyFilters() {
        loadHistory()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            dateFrom = "",
            dateTo = "",
            triggerReason = "all"
        )
        loadHistory()
    }

    fun retry() {
        loadHistory()
    }

    private fun loadHistory() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            if (state.taskTitle.isBlank()) {
                when (val taskResult = AppContainer.taskRepository.getTaskById(taskId)) {
                    is ApiResult.Success -> {
                        val task = taskResult.data
                        _uiState.value = _uiState.value.copy(
                            taskTitle = task.title,
                            taskDescription = task.description,
                            isTaskRotating = task.rotating
                        )
                    }

                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            taskTitle = "Task #$taskId",
                            errorMessage = taskResult.message
                        )
                    }
                }
            }

            val reasonFilter = state.triggerReason.takeIf { it.isNotBlank() && it != "all" }

            when (val result = AppContainer.taskRepository.getTaskRotationHistory(
                taskId = taskId,
                dateFrom = state.dateFrom.takeIf { it.isNotBlank() },
                dateTo = state.dateTo.takeIf { it.isNotBlank() },
                triggerReason = reasonFilter
            )) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = result.data,
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

class TaskRotationHistoryViewModelFactory(
    private val taskId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TaskRotationHistoryViewModel(taskId = taskId) as T
}
