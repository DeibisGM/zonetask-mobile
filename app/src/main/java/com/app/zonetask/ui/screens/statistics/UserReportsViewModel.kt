package com.app.zonetask.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserReportsViewModel(
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserReportsUiState())
    val uiState: StateFlow<UserReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun onPeriodSelected(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        if (period != StatsPeriod.CUSTOM) loadReports()
    }

    fun onDateFromChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateFrom = value)
    }

    fun onDateToChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateTo = value)
    }

    fun applyCustomRange() {
        loadReports()
    }

    fun onSortBySelected(sortBy: ReportSortBy) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        loadReports()
    }

    fun retry() {
        loadReports()
    }

    private fun loadReports() {
        val state = _uiState.value
        val isCustom = state.selectedPeriod == StatsPeriod.CUSTOM

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = AppContainer.statisticsRepository.getUserReports(
                spaceId  = spaceId,
                period   = if (!isCustom) state.selectedPeriod.apiValue else null,
                dateFrom = if (isCustom) state.dateFrom.takeIf { it.isNotBlank() } else null,
                dateTo   = if (isCustom) state.dateTo.takeIf { it.isNotBlank() } else null,
                sortBy   = state.sortBy.apiValue
            )

            when (result) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    reports      = result.data,
                    errorMessage = null
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = result.message
                )
            }
        }
    }
}

class UserReportsViewModelFactory(
    private val spaceId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        UserReportsViewModel(spaceId = spaceId) as T
}
