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

class OverdueTrendsViewModel(
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(OverdueTrendsUiState())
    val uiState: StateFlow<OverdueTrendsUiState> = _uiState.asStateFlow()

    init {
        loadTrends()
    }

    fun onPeriodSelected(period: StatsPeriod) {
        // Reset the interval so the backend picks a sensible default for the new period.
        _uiState.value = _uiState.value.copy(selectedPeriod = period, interval = null)
        if (period != StatsPeriod.CUSTOM) loadTrends()
    }

    fun onDateFromChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateFrom = value)
    }

    fun onDateToChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateTo = value)
    }

    fun applyCustomRange() {
        loadTrends()
    }

    fun onIntervalSelected(interval: TrendInterval) {
        _uiState.value = _uiState.value.copy(interval = interval)
        loadTrends()
    }

    fun onGroupBySelected(groupBy: TrendGroupBy) {
        _uiState.value = _uiState.value.copy(groupBy = groupBy)
    }

    fun retry() {
        loadTrends()
    }

    /** Re-fetches live data so the report reflects new overdue events (#224). */
    fun refresh() {
        loadTrends(isRefresh = true)
    }

    private fun loadTrends(isRefresh: Boolean = false) {
        val state = _uiState.value
        val isCustom = state.selectedPeriod == StatsPeriod.CUSTOM

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading    = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )

            val result = AppContainer.statisticsRepository.getOverdueTrends(
                spaceId  = spaceId,
                period   = if (!isCustom) state.selectedPeriod.apiValue else null,
                dateFrom = if (isCustom) state.dateFrom.takeIf { it.isNotBlank() } else null,
                dateTo   = if (isCustom) state.dateTo.takeIf { it.isNotBlank() } else null,
                interval = state.interval?.apiValue
            )

            when (result) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    isRefreshing = false,
                    trends       = result.data,
                    errorMessage = null
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    isRefreshing = false,
                    errorMessage = result.message
                )
            }
        }
    }
}

class OverdueTrendsViewModelFactory(
    private val spaceId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OverdueTrendsViewModel(spaceId = spaceId) as T
}
