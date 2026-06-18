package com.app.zonetask.ui.screens.taskhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompletedTaskHistoryViewModel(
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompletedTaskHistoryUiState())
    val uiState: StateFlow<CompletedTaskHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPage(1)
    }

    fun onDateFromChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateFrom = value)
    }

    fun onDateToChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateTo = value)
    }

    fun applyFilters() {
        loadPage(1)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.currentPage < state.totalPages && !state.isLoadingMore && !state.isLoading) {
            loadPage(state.currentPage + 1)
        }
    }

    fun retry() {
        loadPage(1)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(dateFrom = "", dateTo = "")
        loadPage(1)
    }

    private fun loadPage(page: Int) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = page == 1 && state.items.isEmpty(),
                isLoadingMore = page > 1,
                errorMessage = null
            )

            val dateFrom = state.dateFrom.takeIf { it.isNotBlank() }
            val dateTo   = state.dateTo.takeIf { it.isNotBlank() }

            when (val result = AppContainer.completionRepository.getCompletedTasks(
                spaceId  = spaceId,
                page     = page,
                dateFrom = dateFrom,
                dateTo   = dateTo
            )) {
                is ApiResult.Success -> {
                    val data = result.data
                    val newItems = if (page == 1) data.items else _uiState.value.items + data.items
                    _uiState.value = _uiState.value.copy(
                        isLoading     = false,
                        isLoadingMore = false,
                        items         = newItems,
                        currentPage   = data.page,
                        totalPages    = data.totalPages,
                        totalCount    = data.totalCount,
                        errorMessage  = null
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading     = false,
                        isLoadingMore = false,
                        errorMessage  = result.message
                    )
                }
            }
        }
    }
}

class CompletedTaskHistoryViewModelFactory(
    private val spaceId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CompletedTaskHistoryViewModel(spaceId = spaceId) as T
}
