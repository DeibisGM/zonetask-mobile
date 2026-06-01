package com.app.zonetask.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.repository.FloorPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlanListViewModel(
    private val repository: FloorPlanRepository,
    private val spaceId:    Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanListUiState())
    val uiState: StateFlow<PlanListUiState> = _uiState.asStateFlow()

    init { loadPlans() }

    fun loadPlans() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorBanner = null)
        viewModelScope.launch {
            when (val result = repository.getPlansBySpace(spaceId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        plans     = result.data
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
}

class PlanListViewModelFactory(
    private val repository: FloorPlanRepository,
    private val spaceId:    Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlanListViewModel(repository, spaceId) as T
}
