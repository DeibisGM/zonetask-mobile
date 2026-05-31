package com.app.zonetask.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.common.resolveDueTimeUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            coroutineScope {
                val spacesDeferred = async { AppContainer.spaceRepository.getSpacesByUser(userId) }
                val plansDeferred = async { AppContainer.floorPlanRepository.getPlansBySpace(spaceId) }
                val tasksDeferred = async { AppContainer.taskRepository.getTasksBySpace(spaceId) }
                val usersDeferred = async { AppContainer.userRepository.getUsers() }

                val spacesResult = spacesDeferred.await()
                val plansResult = plansDeferred.await()
                val tasksResult = tasksDeferred.await()
                val usersResult = usersDeferred.await()

                val userNamesById = if (usersResult is ApiResult.Success) {
                    usersResult.data.associate { user ->
                        user.userId to (user.displayName.ifBlank {
                            val fullName = listOfNotNull(user.firstName, user.lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            fullName.ifBlank { user.username.ifBlank { "User ${user.userId}" } }
                        })
                    }
                } else emptyMap()

                val spaces = when (spacesResult) {
                    is ApiResult.Success -> spacesResult.data
                    is ApiResult.Error -> emptyList()
                }

                val currentSpace = if (spaceId > 0) {
                    spaces.firstOrNull { it.spaceId == spaceId }
                } else {
                    spaces.firstOrNull()
                }

                val resolvedSpaceId = currentSpace?.spaceId ?: spaceId

                val plans = when (plansResult) {
                    is ApiResult.Success -> plansResult.data
                    is ApiResult.Error -> emptyList()
                }

                val allTasks = when (tasksResult) {
                    is ApiResult.Success -> tasksResult.data
                    is ApiResult.Error -> emptyList()
                }

                val pendingTaskItems = allTasks
                    .filter { it.isActive }
                    .mapNotNull { task ->
                        buildPendingTaskItem(task, userNamesById)
                    }
                    .take(10)

                _uiState.value = _uiState.value.copy(
                    spaceName = currentSpace?.name ?: "No space",
                    plans = plans,
                    activePlan = plans.firstOrNull(),
                    pendingTasks = pendingTaskItems,
                    userSpaces = spaces,
                    currentSpaceId = resolvedSpaceId,
                    isLoading = false,
                    errorMessage = if (spaces.isEmpty()) "You don't have any spaces yet." else null
                )
            }
        }
    }

    private suspend fun buildPendingTaskItem(
        task: TaskResponse,
        userNamesById: Map<Int, String>
    ): HomeTaskItem? = coroutineScope {
        val assignmentsResult = AppContainer.taskRepository.getTaskAssignments(task.taskId)
        val assignments = when (assignmentsResult) {
            is ApiResult.Success -> assignmentsResult.data
            is ApiResult.Error -> emptyList()
        }

        val assigneeName = assignments
            .mapNotNull { userNamesById[it.assignedUserId] }
            .firstOrNull()

        val dueTimeState = assignments.resolveDueTimeUiState(userId)

        val zoneName = task.zoneId?.let { "Zone $it" } ?: "No zone"

        HomeTaskItem(
            taskId = task.taskId,
            title = task.title,
            scheduledTime = task.scheduledTime,
            zoneName = zoneName,
            assigneeName = assigneeName,
            dueLabel = dueTimeState.label,
            dueStatusKey = dueTimeState.statusKey
        )
    }
}

class HomeViewModelFactory(
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(spaceId = spaceId, userId = userId) as T
}
