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

data class SpaceStatisticsMenuUiState(
    val isLoading: Boolean = true,
    val userRole: String = "member"
) {
    val canViewReports: Boolean
        get() = userRole == "owner" || userRole == "admin"
}

/** Resolves the requesting user's role so the menu can gate owner/admin-only reports. */
class SpaceStatisticsMenuViewModel(
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceStatisticsMenuUiState())
    val uiState: StateFlow<SpaceStatisticsMenuUiState> = _uiState.asStateFlow()

    init {
        loadRole()
    }

    private fun loadRole() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val role = when (val result = AppContainer.spaceRepository.getSpacePermissions(spaceId, userId)) {
                is ApiResult.Success -> result.data.requestingUserRole
                is ApiResult.Error   -> "member"
            }
            _uiState.value = SpaceStatisticsMenuUiState(isLoading = false, userRole = role)
        }
    }
}

class SpaceStatisticsMenuViewModelFactory(
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceStatisticsMenuViewModel(spaceId = spaceId, userId = userId) as T
}
