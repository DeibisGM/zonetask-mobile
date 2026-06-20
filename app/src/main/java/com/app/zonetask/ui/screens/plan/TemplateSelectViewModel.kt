package com.app.zonetask.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.repository.FloorPlanTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateSelectViewModel(
    private val templateRepository: FloorPlanTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateSelectUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() = viewModelScope.launch {
        when (val result = templateRepository.getAll()) {
            is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, templates = result.data)
        }
    }
}

class TemplateSelectViewModelFactory(
    private val templateRepository: FloorPlanTemplateRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TemplateSelectViewModel(templateRepository) as T
}
