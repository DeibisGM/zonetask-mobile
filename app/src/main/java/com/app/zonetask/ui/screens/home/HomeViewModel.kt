package com.app.zonetask.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.WorkspaceStore
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.FloorPlanZonePreview
import com.app.zonetask.ui.common.resolveDueTimeUiState
import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var zoneLoadVersion = 0

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                plansErrorMessage = null,
                tasksErrorMessage = null,
                zonesErrorMessage = null,
                isZonesLoading = false
            )
            _uiState.value = loadHomeSnapshot()
        }
    }

    fun selectPlan(planId: Int) {
        val state = _uiState.value
        val plan = state.plans.firstOrNull { it.planId == planId } ?: return

        if (state.activePlan?.planId == planId) return

        val currentSpaceId = state.currentSpaceId ?: spaceId
        if (currentSpaceId > 0) {
            WorkspaceStore.rememberPlan(userId, currentSpaceId, planId)
        }

        val requestVersion = ++zoneLoadVersion
        _uiState.value = state.copy(
            activePlan = plan,
            isZonesLoading = true,
            zonesErrorMessage = null
        )

        viewModelScope.launch {
            val zoneLoad = loadZonePreviewState(plan, state.zoneTaskCounts)
            if (requestVersion != zoneLoadVersion) return@launch

            _uiState.value = _uiState.value.copy(
                activePlan = plan,
                planZones = zoneLoad.zones,
                pendingTasks = enrichTaskZoneNames(_uiState.value.pendingTasks, zoneLoad.zones),
                isZonesLoading = false,
                zonesErrorMessage = zoneLoad.errorMessage
            )
        }
    }

    private suspend fun loadHomeSnapshot(): HomeUiState = coroutineScope {
        val spacesDeferred = async { AppContainer.spaceRepository.getSpacesByUser(userId) }
        val usersDeferred = async { AppContainer.userRepository.getUsers() }

        val spacesResult = spacesDeferred.await()
        val spaces = when (spacesResult) {
            is ApiResult.Success -> spacesResult.data
            is ApiResult.Error -> {
                return@coroutineScope HomeUiState(
                    isLoading = false,
                    errorMessage = spacesResult.message
                )
            }
        }

        if (spaces.isEmpty()) {
            return@coroutineScope HomeUiState(
                isLoading = false,
                errorMessage = "You don't have any spaces yet.",
                userSpaces = spaces
            )
        }

        val resolvedSpaceId = resolveSpaceId(spaces)
        val currentSpace = spaces.firstOrNull { it.spaceId == resolvedSpaceId } ?: spaces.first()
        WorkspaceStore.rememberSpace(userId, currentSpace.spaceId)

        val plansDeferred = async { AppContainer.floorPlanRepository.getPlansBySpace(currentSpace.spaceId) }
        val tasksDeferred = async { AppContainer.taskRepository.getTasksBySpace(currentSpace.spaceId) }

        val usersResult = usersDeferred.await()
        val userNamesById = when (usersResult) {
            is ApiResult.Success -> usersResult.data.associate { user ->
                user.userId to user.displayName.ifBlank {
                    val fullName = listOfNotNull(user.firstName, user.lastName)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                    fullName.ifBlank { user.username.ifBlank { "User ${user.userId}" } }
                }
            }

            is ApiResult.Error -> emptyMap()
        }

        val plansResult = plansDeferred.await()
        val plans = when (plansResult) {
            is ApiResult.Success -> plansResult.data
            is ApiResult.Error -> emptyList()
        }

        val tasksResult = tasksDeferred.await()
        val tasks = when (tasksResult) {
            is ApiResult.Success -> tasksResult.data
            is ApiResult.Error -> emptyList()
        }

        val zoneTaskCounts = tasks
            .filter { it.isActive }
            .mapNotNull { it.zoneId }
            .groupingBy { it }
            .eachCount()

        val pendingTaskItems = buildPendingTaskItems(tasks, userNamesById)
        val activePlan = resolveActivePlan(plans, currentSpace.spaceId)

        val zoneLoad = if (activePlan != null) {
            loadZonePreviewState(activePlan, zoneTaskCounts)
        } else {
            ZoneLoadState(emptyList(), null)
        }

        val enrichedTasks = enrichTaskZoneNames(pendingTaskItems, zoneLoad.zones)

        if (activePlan != null) {
            WorkspaceStore.rememberPlan(userId, currentSpace.spaceId, activePlan.planId)
        }

        HomeUiState(
            spaceName = currentSpace.name,
            spaceType = currentSpace.spaceType,
            plans = plans,
            activePlan = activePlan,
            planZones = zoneLoad.zones,
            zoneTaskCounts = zoneTaskCounts,
            pendingTasks = enrichedTasks,
            userSpaces = spaces,
            currentSpaceId = currentSpace.spaceId,
            isLoading = false,
            errorMessage = null,
            plansErrorMessage = if (plansResult is ApiResult.Error) plansResult.message else null,
            tasksErrorMessage = if (tasksResult is ApiResult.Error) tasksResult.message else null,
            zonesErrorMessage = zoneLoad.errorMessage
        )
    }

    private suspend fun buildPendingTaskItems(
        tasks: List<TaskResponse>,
        userNamesById: Map<Int, String>
    ): List<HomeTaskItem> = coroutineScope {
        tasks
            .filter { it.isActive }
            .map { task ->
                async { buildPendingTaskItem(task, userNamesById) }
            }
            .map { deferred -> deferred.await() }
            .sortedWith(
                compareBy<HomeTaskItem> { dueStatusPriority(it.dueStatusKey) }
                    .thenBy { it.scheduledTime ?: "" }
                    .thenBy { it.title.lowercase() }
            )
            .take(10)
    }

    private suspend fun buildPendingTaskItem(
        task: TaskResponse,
        userNamesById: Map<Int, String>
    ): HomeTaskItem {
        val assignmentsResult = AppContainer.taskRepository.getTaskAssignments(task.taskId)
        val assignments = when (assignmentsResult) {
            is ApiResult.Success -> assignmentsResult.data
            is ApiResult.Error -> emptyList()
        }

        val assigneeName = assignments
            .mapNotNull { userNamesById[it.assignedUserId] }
            .firstOrNull()
            ?: task.assignedUserId?.let { userNamesById[it] }

        val dueTimeState = assignments.resolveDueTimeUiState(userId)
        val zoneName = task.zoneId?.let { "Zone $it" } ?: "No zone"

        return HomeTaskItem(
            taskId = task.taskId,
            title = task.title,
            scheduledTime = task.scheduledTime,
            zoneId = task.zoneId,
            zoneName = zoneName,
            assigneeName = assigneeName,
            dueLabel = dueTimeState.label,
            dueStatusKey = dueTimeState.statusKey
        )
    }

    private suspend fun loadZonePreviewState(
        plan: com.app.zonetask.domain.model.FloorPlan,
        zoneTaskCounts: Map<Int, Int>
    ): ZoneLoadState {
        return when (val zonesResult = AppContainer.zoneRepository.getZonesByPlan(
            plan.planId,
            plan.canvasWidth,
            plan.canvasHeight
        )) {
            is ApiResult.Success -> {
                val previews = zonesResult.data.map { draft ->
                    draft.toPreview(zoneTaskCounts[draft.backendId ?: -1] ?: 0)
                }

                ZoneLoadState(previews, null)
            }

            is ApiResult.Error -> ZoneLoadState(emptyList(), zonesResult.message)
        }
    }

    private fun resolveSpaceId(spaces: List<com.app.zonetask.domain.model.Space>): Int {
        val rememberedSpaceId = WorkspaceStore.getLastSpaceId(userId)
        return when {
            spaceId > 0 && spaces.any { it.spaceId == spaceId } -> spaceId
            rememberedSpaceId > 0 && spaces.any { it.spaceId == rememberedSpaceId } -> rememberedSpaceId
            else -> spaces.firstOrNull()?.spaceId ?: 0
        }
    }

    private fun resolveActivePlan(
        plans: List<com.app.zonetask.domain.model.FloorPlan>,
        currentSpaceId: Int
    ): com.app.zonetask.domain.model.FloorPlan? {
        if (plans.isEmpty()) return null

        val rememberedPlanId = WorkspaceStore.getLastPlanId(userId, currentSpaceId)
        return when {
            rememberedPlanId != null -> plans.firstOrNull { it.planId == rememberedPlanId }
            else -> plans.firstOrNull()
        } ?: plans.firstOrNull()
    }

    private fun enrichTaskZoneNames(
        tasks: List<HomeTaskItem>,
        zones: List<FloorPlanZonePreview>
    ): List<HomeTaskItem> {
        if (zones.isEmpty()) return tasks

        val namesByZoneId = zones.mapNotNull { zone ->
            zone.backendId?.let { backendId -> backendId to zone.name }
        }.toMap()

        return tasks.map { task ->
            val zoneName = task.zoneId?.let { zoneId -> namesByZoneId[zoneId] } ?: task.zoneName
            task.copy(zoneName = zoneName ?: task.zoneName)
        }
    }

    private fun dueStatusPriority(key: String): Int = when (key.lowercase()) {
        "overdue" -> 0
        "upcoming" -> 1
        "completed" -> 2
        else -> 3
    }

    private fun PlanZoneDraft.toPreview(taskCount: Int): FloorPlanZonePreview {
        return FloorPlanZonePreview(
            id = "zone_$backendId",
            backendId = backendId,
            name = name,
            x = x,
            y = y,
            width = width,
            height = height,
            fillColor = fillColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            opacity = opacity,
            shapeType = "rectangle",
            taskCount = taskCount
        )
    }

    private data class ZoneLoadState(
        val zones: List<FloorPlanZonePreview>,
        val errorMessage: String?
    )
}

class HomeViewModelFactory(
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(spaceId = spaceId, userId = userId) as T
}
