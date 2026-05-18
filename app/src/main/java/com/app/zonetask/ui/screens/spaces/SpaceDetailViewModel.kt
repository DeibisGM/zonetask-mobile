package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.MarkTaskCompletionRequestDto
import com.app.zonetask.data.remote.dto.TaskAssignmentResponse
import com.app.zonetask.data.remote.dto.SpacePermissionsResponse
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.data.repository.SpaceRepository
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.app.zonetask.ui.common.resolveDueTimeUiState

class SpaceDetailViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceDetailUiState())
    val uiState: StateFlow<SpaceDetailUiState> = _uiState.asStateFlow()

    init {
        loadSpace()
        loadTasks()
    }

    fun loadSpace() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorBanner = null)
        viewModelScope.launch {
            val spaceDeferred       = async { spaceRepository.getSpaceById(spaceId) }
            val permissionsDeferred = async { spaceRepository.getSpacePermissions(spaceId, userId) }

            val spaceResult       = spaceDeferred.await()
            val permissionsResult = permissionsDeferred.await()

            if (spaceResult is ApiResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading   = false,
                    errorBanner = spaceResult.message
                )
                return@launch
            }

            val space       = (spaceResult as ApiResult.Success).data
            val permissions = permissionsResult.asSpacePermissionsOrNull()

            val userRole = when {
                permissions != null  -> permissions.requestingUserRole
                space.ownerId == userId -> "owner"
                else                 -> "member"
            }

            _uiState.value = _uiState.value.copy(
                isLoading   = false,
                space       = space,
                userRole    = userRole,
                errorBanner = null
            )
        }
    }

    fun loadTasks() {
        _uiState.value = _uiState.value.copy(
            tasksLoading    = true,
            tasksError      = null,
            completionError = null
        )
        viewModelScope.launch {
            when (val result = AppContainer.taskRepository.getTasksBySpace(spaceId)) {
                is ApiResult.Success -> {
                    val tasks = buildSpaceTaskItems(result.data)
                    _uiState.value = _uiState.value.copy(
                        tasksLoading    = false,
                        tasks           = tasks,
                        tasksError      = null,
                        completionError = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        tasksLoading = false,
                        tasksError   = result.message
                    )
                }
            }
        }
    }

    fun deleteSpace() {
        _uiState.value = _uiState.value.copy(isDeleting = true, errorBanner = null)
        viewModelScope.launch {
            when (val result = spaceRepository.deleteSpace(spaceId, userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting    = false,
                        deleteSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting  = false,
                        errorBanner = result.message
                    )
                }
            }
        }
    }

    fun consumeDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }

    fun completeAssignment(assignmentId: Int) {
        if (_uiState.value.completingAssignmentId != null) return

        _uiState.value = _uiState.value.copy(
            completingAssignmentId = assignmentId,
            completionError        = null
        )

        viewModelScope.launch {
            when (val result = AppContainer.taskRepository.completeTaskAssignment(
                assignmentId = assignmentId,
                request      = MarkTaskCompletionRequestDto(requestingUserId = userId)
            )) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        completingAssignmentId = null,
                        completionError        = null
                    )
                    loadTasks()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        completingAssignmentId = null,
                        completionError        = result.message
                    )
                }
            }
        }
    }

    private suspend fun buildSpaceTaskItems(tasks: List<TaskResponse>): List<SpaceTaskUiState> = coroutineScope {
        tasks.map { task ->
            async {
                val assignmentsResult = AppContainer.taskRepository.getTaskAssignments(task.taskId)
                val assignments = when (assignmentsResult) {
                    is ApiResult.Success -> assignmentsResult.data
                    is ApiResult.Error   -> emptyList()
                }

                val dueTimeState = assignments.resolveDueTimeUiState(userId)
                SpaceTaskUiState(
                    task                   = task,
                    dueLabel               = dueTimeState.label,
                    dueStatusKey           = dueTimeState.statusKey,
                    completionAssignmentId = dueTimeState.assignmentId,
                    canComplete            = dueTimeState.canComplete
                )
            }
        }.map { it.await() }
    }

    private fun ApiResult<SpacePermissionsResponse>.asSpacePermissionsOrNull(): SpacePermissionsResponse? {
        return when (this) {
            is ApiResult.Success -> data
            is ApiResult.Error   -> null
        }
    }
}

class SpaceDetailViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceDetailViewModel(
            spaceRepository = spaceRepository,
            spaceId         = spaceId,
            userId          = userId
        ) as T
}