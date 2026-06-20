package com.app.zonetask.ui.screens.taskcreate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.UserMessages
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.SpaceMember
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class TaskCreateViewModel(
    initialSpaceId: Int = 1,
    initialCreatedBy: Int = 1,
    private val taskId: Int? = null
) : ViewModel() {

    private val requestingUserId: Int = initialCreatedBy

    var uiState by mutableStateOf(TaskCreateUiState())
        private set

    var formOptionsUiState by mutableStateOf(TaskFormOptionsUiState())
        private set

    init {
        uiState = uiState.copy(
            spaceId = initialSpaceId,
            createdBy = initialCreatedBy
        )
        // Load the right lookup set for create or edit mode.
        if (taskId != null) {
            loadTaskForEdit(taskId)
        } else {
            loadFormOptions(initialSpaceId)
            loadAssignableMembers(initialSpaceId, requestingUserId)
        }
    }

    val isEditMode: Boolean
        get() = taskId != null
    
    fun updateState(block: TaskCreateUiState.() -> TaskCreateUiState) {
        uiState = uiState.block().revalidate()
    }

    fun validate(): Boolean {
        val validatedState = uiState.revalidate(showErrors = true)
        uiState = validatedState
        return validatedState.isValid
    }

    fun loadFormOptions(spaceId: Int = 1) {
        viewModelScope.launch {
            formOptionsUiState = formOptionsUiState.copy(isLoading = true, errorMessage = null)

            when (val result = AppContainer.taskLookupRepository.getTaskFormOptions(spaceId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    formOptionsUiState = formOptionsUiState.copy(
                        categories = data.categories.map { it.name to it.id.toString() },
                        zones = data.zones.map { it.name to it.id.toString() },
                        isLoading = false,
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    formOptionsUiState = formOptionsUiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun loadZoneObjects(zoneId: Int) {
        viewModelScope.launch {
            formOptionsUiState = formOptionsUiState.copy(objectsLoading = true, errorMessage = null)

            when (val result = AppContainer.taskLookupRepository.getZoneObjects(zoneId)) {
                is ApiResult.Success -> {
                    val data = result.data
                    formOptionsUiState = formOptionsUiState.copy(
                        objects = data.map { it.name to it.id.toString() },
                        objectsLoading = false,
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    formOptionsUiState = formOptionsUiState.copy(
                        objects = emptyList(),
                        objectsLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun loadAssignableMembers(spaceId: Int, requestingUserId: Int) {
        viewModelScope.launch {
            formOptionsUiState = formOptionsUiState.copy(assigneesLoading = true, assigneesError = null)

            coroutineScope {
                val membersDeferred = async { AppContainer.spaceRepository.getSpaceMembers(spaceId, requestingUserId) }
                val usersDeferred = async { AppContainer.userRepository.getUsers() }

                val membersResult = membersDeferred.await()
                val usersResult = usersDeferred.await()

                val userNamesById = if (usersResult is ApiResult.Success) {
                    usersResult.data.associate { user ->
                        user.userId to user.displayName.ifBlank {
                            val fullName = listOfNotNull(user.firstName, user.lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            fullName.ifBlank { user.username.ifBlank { "User ${user.userId}" } }
                        }
                    }
                } else {
                    emptyMap()
                }

                val assignableMembers = if (membersResult is ApiResult.Success) {
                    membersResult.data
                        .filter { member ->
                            member.status.equals("accepted", ignoreCase = true) ||
                                member.status.equals("active", ignoreCase = true)
                        }
                        .sortedWith(
                            compareBy<SpaceMember> { it.rotationOrder ?: Int.MAX_VALUE }
                                .thenBy { it.joinedAt ?: "" }
                                .thenBy { it.memberId }
                        )
                        .mapNotNull { member ->
                            val displayName = userNamesById[member.userId]
                                ?: "User ${member.userId}"
                            displayName to member.userId.toString()
                        }
                        .distinctBy { it.second }
                } else {
                    emptyList()
                }

                formOptionsUiState = formOptionsUiState.copy(
                    assignees = assignableMembers,
                    assigneesLoading = false,
                    assigneesError = when (membersResult) {
                        is ApiResult.Error -> membersResult.message
                        else -> null
                    }
                )
            }
        }
    }

    fun loadTaskForEdit(taskId: Int) {
        viewModelScope.launch {
            // Pull the saved task first so the form can reuse its values.
            when (val result = AppContainer.taskRepository.getTaskById(taskId)) {
                is ApiResult.Success -> {
                    applyTaskToForm(result.data)
                    loadAssignableMembers(result.data.spaceId, requestingUserId)
                }
                is ApiResult.Error -> {
                    formOptionsUiState = formOptionsUiState.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun saveTask(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // The API expects time in HH:mm:ss format.
            val normalizedTime = uiState.scheduledTime
                .takeIf { it.isNotBlank() }
                ?.let { value ->
                    if (value.count { it == ':' } == 1) {
                        "$value:00"
                    } else {
                        value
                    }
                }

            val request = CreateTaskRequestDto(
                title = uiState.title.trim(),
                description = uiState.description.takeIf { it.isNotBlank() }?.trim(),
                frequency = uiState.frequency,
                rotationStrategy = uiState.rotationStrategy,
                recurrenceRule = uiState.recurrenceRule,
                scheduledTime = normalizedTime,
                startDate = uiState.startDate.ifBlank { null },
                endDate = uiState.endDate,
                rotating = uiState.rotating,
                isActive = uiState.isActive,
                reminderEnabled = uiState.reminderEnabled,
                reminderMinutes = uiState.reminderMinutes,
                requiresProof = uiState.requiresProof,
                requiresDescription = uiState.requiresDescription,
                estimatedMinutes = uiState.estimatedMinutes,
                createdBy = uiState.createdBy,
                categoryId = uiState.categoryId,
                spaceId = uiState.spaceId,
                zoneId = uiState.zoneId,
                assignedUserId = if (uiState.rotating) null else uiState.assignedUserId,
                objectId = uiState.selectedObjectIds.firstOrNull(),
                objectIds = if (uiState.objectSelectionEnabled) uiState.selectedObjectIds else emptyList()
            )

            // Return the API result to the UI so it can show success or error.
            val result = if (taskId == null) {
                AppContainer.taskRepository.createTask(request)
            } else {
                AppContainer.taskRepository.updateTask(taskId, request)
            }

            when (result) {
                is ApiResult.Success -> onResult(true, UserMessages.TaskCreate.SAVE_SNACKBAR)
                is ApiResult.Error -> onResult(false, result.message)
            }
        }
    }

    fun resetForm() {
        // Start over with a fresh form after a successful save.
        uiState = TaskCreateUiState()
    }

    private fun TaskCreateUiState.revalidate(showErrors: Boolean = this.showErrors): TaskCreateUiState {
        // Keep validation state in sync while the user types.
        val titleValid = title.isNotBlank()
        val startDateValid = startDate.isNotBlank()
        val timeValid = scheduledTime.isNotBlank()
        val estimatedTimeValid = estimatedMinutes != null

        return copy(
            isTitleValid = titleValid,
            isStartDateValid = startDateValid,
            isTimeValid = timeValid,
            isEstimatedTimeValid = estimatedTimeValid,
            showErrors = showErrors
        )
    }

    private fun applyTaskToForm(task: TaskResponse) {
        uiState = uiState.copy(
            title = task.title,
            description = task.description.orEmpty(),
            targetLevel = if (task.zoneId != null) "zone" else "space",
            frequency = task.frequency,
            recurrenceRule = task.recurrenceRule,
            scheduledTime = task.scheduledTime?.let { value ->
                if (value.length >= 5) value.substring(0, 5) else value
            } ?: "",
            startDate = task.startDate.orEmpty(),
            endDate = task.endDate,
            rotating = task.rotating,
            isActive = task.isActive,
            reminderEnabled = task.reminderEnabled,
            reminderMinutes = task.reminderMinutes,
            requiresProof = task.requiresProof,
            requiresDescription = task.requiresDescription,
            estimatedMinutes = task.estimatedMinutes,
            rotationStrategy = task.rotationStrategy ?: "sequential",
            createdBy = task.createdBy,
            categoryId = task.categoryId,
            spaceId = task.spaceId,
            zoneId = task.zoneId,
            objectId = task.objectId,
            objectSelectionEnabled = task.objectIds.isNotEmpty(),
            selectedObjectIds = task.objectIds,
            assignedUserId = task.assignedUserId,
            showErrors = false
        )

        // Load the proper lookup set for the task's saved space.
        loadFormOptions(task.spaceId)
    }

}

class TaskCreateViewModelFactory(
    private val initialSpaceId: Int,
    private val initialCreatedBy: Int,
    private val taskId: Int? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TaskCreateViewModel(
            initialSpaceId = initialSpaceId,
            initialCreatedBy = initialCreatedBy,
            taskId = taskId
        ) as T
}
