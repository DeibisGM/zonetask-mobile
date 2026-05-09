package com.app.zonetask.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.TaskAssignmentResponse
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksViewModel(
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState(isLoadingSpaces = true, isLoadingTasks = true))
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var userNamesById: Map<Int, String> = emptyMap()

    init {
        loadInitialData()
    }

    fun selectSpace(spaceId: Int) {
        if (_uiState.value.selectedSpaceId == spaceId) return
        val spaceName = _uiState.value.spaces.firstOrNull { it.spaceId == spaceId }?.name.orEmpty()
        _uiState.value = _uiState.value.copy(
            selectedSpaceId = spaceId,
            selectedSpaceName = spaceName,
            isLoadingTasks = true,
            taskErrorMessage = null,
            zoneGroups = emptyList()
        )
        loadTasksForSpace(spaceId)
    }

    fun retrySelectedSpace() {
        _uiState.value.selectedSpaceId?.let { loadTasksForSpace(it) }
    }

    fun deleteTask(taskId: Int, onResult: (Boolean, String) -> Unit) {
        if (_uiState.value.deletingTaskId != null) return

        _uiState.value = _uiState.value.copy(deletingTaskId = taskId)

        viewModelScope.launch {
            when (val result = AppContainer.taskRepository.deleteTask(taskId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        deletingTaskId = null,
                        zoneGroups = _uiState.value.zoneGroups
                            .mapNotNull { group ->
                                val remainingTasks = group.tasks.filterNot { it.task.taskId == taskId }
                                if (remainingTasks.isEmpty()) null
                                else group.copy(tasks = remainingTasks)
                            }
                    )
                    onResult(true, "Eliminado")
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(deletingTaskId = null)
                    onResult(false, result.message)
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingSpaces = true,
                isLoadingTasks = true,
                errorMessage = null,
                taskErrorMessage = null
            )

            coroutineScope {
                val spacesDeferred = async { AppContainer.spaceRepository.getSpacesByUser(userId) }
                val usersDeferred = async { AppContainer.userRepository.getUsers() }

                val spacesResult = spacesDeferred.await()
                when (spacesResult) {
                    is ApiResult.Success -> {
                        val spaces = spacesResult.data
                        val firstSpace = spaces.firstOrNull()
                        _uiState.value = _uiState.value.copy(
                            isLoadingSpaces = false,
                            spaces = spaces,
                            selectedSpaceId = firstSpace?.spaceId,
                            selectedSpaceName = firstSpace?.name.orEmpty(),
                            errorMessage = if (spaces.isEmpty()) "No tenés espacios todavía." else null
                        )
                    }

                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingSpaces = false,
                            errorMessage = spacesResult.message
                        )
                    }
                }

                val usersResult = usersDeferred.await()
                if (usersResult is ApiResult.Success) {
                    userNamesById = usersResult.data.associate { user ->
                        user.userId to user.displayName.ifBlank {
                            val fullName = listOfNotNull(user.firstName, user.lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            fullName.ifBlank { user.username.ifBlank { "Usuario ${user.userId}" } }
                        }
                    }
                }

                _uiState.value.selectedSpaceId?.let { loadTasksForSpace(it) }
                if (_uiState.value.selectedSpaceId == null) {
                    _uiState.value = _uiState.value.copy(isLoadingTasks = false)
                }
            }
        }
    }

    private fun loadTasksForSpace(spaceId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingTasks = true,
                taskErrorMessage = null,
                zoneGroups = emptyList()
            )

            val result = buildGroupsForSpace(spaceId)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingTasks = false,
                        zoneGroups = result.data,
                        taskErrorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingTasks = false,
                        taskErrorMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun buildGroupsForSpace(spaceId: Int): ApiResult<List<ZoneTaskGroupUiState>> = coroutineScope {
        val lookupsDeferred = async { AppContainer.taskLookupRepository.getTaskFormOptions(spaceId) }
        val tasksDeferred = async { AppContainer.taskRepository.getTasksBySpace(spaceId) }

        val lookupsResult = lookupsDeferred.await()
        val tasksResult = tasksDeferred.await()

        val tasks = when (tasksResult) {
            is ApiResult.Success -> tasksResult.data
            is ApiResult.Error -> return@coroutineScope ApiResult.Error(tasksResult.message, tasksResult.statusCode)
        }

        val zoneNamesById = when (lookupsResult) {
            is ApiResult.Success -> lookupsResult.data.zones.associate { it.id to it.name }
            is ApiResult.Error -> emptyMap()
        }

        val taskCards = tasks.map { task ->
            async {
                buildTaskItem(task, zoneNamesById)
            }
        }.map { it.await() }

        val groups = taskCards
            .groupBy { it.task.zoneId }
            .map { (zoneId, zoneTasks) ->
                ZoneTaskGroupUiState(
                    zoneId = zoneId,
                    zoneName = zoneId?.let { zoneNamesById[it] ?: "Zona $it" } ?: "Sin zona",
                    tasks = zoneTasks.sortedBy { it.task.title.lowercase() }
                )
            }
            .sortedBy { it.zoneId ?: Int.MAX_VALUE }

        ApiResult.Success(groups)
    }

    private suspend fun buildTaskItem(
        task: TaskResponse,
        zoneNamesById: Map<Int, String>
    ): TaskItemUiState {
        val assignmentsResult = AppContainer.taskRepository.getTaskAssignments(task.taskId)
        val assignments = when (assignmentsResult) {
            is ApiResult.Success -> assignmentsResult.data
            is ApiResult.Error -> emptyList()
        }

        val assignees = assignments
            .mapNotNull { assignment ->
                val displayName = userNamesById[assignment.assignedUserId]
                displayName?.let {
                    TaskAssigneeUiState(
                        userId = assignment.assignedUserId,
                        displayName = it
                    )
                }
            }
            .distinctBy { it.userId }

        val zoneName = task.zoneId?.let { zoneNamesById[it] ?: "Zona $it" } ?: "Sin zona"
        return TaskItemUiState(
            task = task,
            zoneName = zoneName,
            assignees = assignees,
            statusLabel = resolveStatusLabel(assignments)
        )
    }

    private fun resolveStatusLabel(assignments: List<TaskAssignmentResponse>): String {
        if (assignments.isEmpty()) return "Sin asignar"

        val normalizedStatuses = assignments.map { it.status.lowercase() }
        return when {
            normalizedStatuses.all { it == "completed" } -> "Completada"
            normalizedStatuses.any { it == "in_progress" } -> "En progreso"
            normalizedStatuses.any { it == "pending" } -> "Pendiente"
            else -> assignments.first().status.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
    }
}

class TasksViewModelFactory(
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TasksViewModel(userId = userId) as T
}
