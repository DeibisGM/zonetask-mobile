package com.app.zonetask.ui.screens.taskhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpaceRotationHistoryViewModel(
    private val spaceId: Int,
    private val requestingUserId: Int,
    initialTaskId: Int? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SpaceRotationHistoryUiState(selectedTaskId = initialTaskId)
    )
    val uiState: StateFlow<SpaceRotationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadFilterOptions()
        loadHistory()
    }

    fun onTaskChanged(value: Int?) {
        _uiState.value = _uiState.value.copy(selectedTaskId = value)
    }

    fun onZoneChanged(value: Int?) {
        _uiState.value = _uiState.value.copy(selectedZoneId = value)
    }

    fun onUserChanged(value: Int?) {
        _uiState.value = _uiState.value.copy(selectedUserId = value)
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
            selectedTaskId = null,
            selectedZoneId = null,
            selectedUserId = null,
            dateFrom = "",
            dateTo = "",
            triggerReason = "all"
        )
        loadHistory()
    }

    fun retry() {
        loadHistory()
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            coroutineScope {
                val tasksDeferred = async { AppContainer.taskRepository.getTasksBySpace(spaceId) }
                val zonesDeferred = async { AppContainer.taskLookupRepository.getTaskFormOptions(spaceId) }
                val membersDeferred = async { AppContainer.spaceRepository.getSpaceMembers(spaceId, requestingUserId) }
                val usersDeferred = async { AppContainer.userRepository.getUsers() }

                when (val tasksResult = tasksDeferred.await()) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            taskOptions = tasksResult.data.map { task ->
                                RotationHistoryFilterOption(
                                    id = task.taskId,
                                    label = task.title
                                )
                            }
                        )
                    }

                    is ApiResult.Error -> Unit
                }

                when (val zonesResult = zonesDeferred.await()) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            zoneOptions = zonesResult.data.zones.map { zone ->
                                RotationHistoryFilterOption(
                                    id = zone.id,
                                    label = zone.name
                                )
                            }
                        )
                    }

                    is ApiResult.Error -> Unit
                }

                val spaceMembers = when (val membersResult = membersDeferred.await()) {
                    is ApiResult.Success -> membersResult.data
                    is ApiResult.Error -> emptyList()
                }

                val users = when (val usersResult = usersDeferred.await()) {
                    is ApiResult.Success -> usersResult.data
                    is ApiResult.Error -> emptyList()
                }

                val usersById = users.associateBy { it.userId }
                val memberUserOptions = spaceMembers.mapNotNull { member ->
                    usersById[member.userId]?.let { user ->
                        RotationHistoryFilterOption(
                            id = user.userId,
                            label = resolveUserLabel(user)
                        )
                    }
                }
                    .distinctBy { it.id }
                    .sortedBy { it.label.lowercase() }

                _uiState.value = _uiState.value.copy(
                    userOptions = memberUserOptions
                )
            }
        }
    }

    private fun loadHistory() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = AppContainer.taskRepository.getSpaceRotationHistory(
                spaceId = spaceId,
                taskId = state.selectedTaskId,
                zoneId = state.selectedZoneId,
                userId = state.selectedUserId,
                dateFrom = state.dateFrom.takeIf { it.isNotBlank() },
                dateTo = state.dateTo.takeIf { it.isNotBlank() },
                triggerReason = state.triggerReason.takeIf { it.isNotBlank() && it != "all" }
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

    private fun resolveUserLabel(user: com.app.zonetask.data.remote.dto.UserResponse): String {
        val displayName = user.displayName.trim()
        if (displayName.isNotBlank()) {
            return displayName
        }

        val fullName = listOf(user.firstName, user.lastName.orEmpty())
            .joinToString(" ")
            .trim()

        return when {
            fullName.isNotBlank() -> fullName
            user.username.isNotBlank() -> user.username
            else -> "Usuario #${user.userId}"
        }
    }
}

class SpaceRotationHistoryViewModelFactory(
    private val spaceId: Int,
    private val requestingUserId: Int,
    private val initialTaskId: Int? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceRotationHistoryViewModel(
            spaceId = spaceId,
            requestingUserId = requestingUserId,
            initialTaskId = initialTaskId
        ) as T
}
