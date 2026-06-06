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

class IndividualStatisticsViewModel(
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndividualStatisticsUiState())
    val uiState: StateFlow<IndividualStatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun onPeriodSelected(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        if (period != StatsPeriod.CUSTOM) {
            loadStatistics()
        }
    }

    fun onDateFromChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateFrom = value)
    }

    fun onDateToChanged(value: String) {
        _uiState.value = _uiState.value.copy(dateTo = value)
    }

    fun applyCustomRange() {
        loadStatistics()
    }

    fun retry() {
        loadStatistics()
    }

    private fun loadStatistics() {
        val state = _uiState.value
        val isCustom = state.selectedPeriod == StatsPeriod.CUSTOM

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = AppContainer.statisticsRepository.getUserStatistics(
                spaceId  = spaceId,
                userId   = userId,
                period   = if (!isCustom) state.selectedPeriod.apiValue else null,
                dateFrom = if (isCustom) state.dateFrom.takeIf { it.isNotBlank() } else null,
                dateTo   = if (isCustom) state.dateTo.takeIf { it.isNotBlank() } else null
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading  = false,
                        statistics = result.data,
                        errorMessage = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

class IndividualStatisticsViewModelFactory(
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IndividualStatisticsViewModel(spaceId = spaceId, userId = userId) as T
}
