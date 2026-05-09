package com.app.zonetask.ui.screens.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.di.AppContainer
import com.app.zonetask.data.remote.dto.LookupOptionResponse
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.data.repository.SpaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpaceDetailViewModel(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpaceDetailUiState())
    val uiState: StateFlow<SpaceDetailUiState> = _uiState.asStateFlow()

    init {
        loadSpace()
        loadTaskGroups()
    }

    fun loadSpace() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorBanner = null)

        viewModelScope.launch {
            when (val result = spaceRepository.getSpaceById(spaceId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        space = result.data,
                        errorBanner = null
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorBanner = result.message
                    )
                }
            }
        }
    }

    fun loadTaskGroups() {
        _uiState.value = _uiState.value.copy(tasksLoading = true, tasksError = null)

        viewModelScope.launch {
            val formOptionsResult = AppContainer.taskLookupRepository.getTaskFormOptions(spaceId)
            val spaceTasksResult = AppContainer.taskRepository.getTasksBySpace(spaceId)

            val lookupZones = when (formOptionsResult) {
                is ApiResult.Success -> formOptionsResult.data.zones
                is ApiResult.Error -> emptyList()
            }
            val spaceTasks = when (spaceTasksResult) {
                is ApiResult.Success -> spaceTasksResult.data
                is ApiResult.Error -> emptyList()
            }

            val groupedTasks = if (lookupZones.isNotEmpty()) {
                buildZoneGroupsFromLookup(lookupZones, spaceTasks)
            } else {
                buildZoneGroupsFromTasks(spaceTasks)
            }

            val errorMessage = when {
                formOptionsResult is ApiResult.Error && spaceTasksResult is ApiResult.Error ->
                    formOptionsResult.message
                formOptionsResult is ApiResult.Error && lookupZones.isEmpty() ->
                    null
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                tasksLoading = false,
                taskGroups = groupedTasks,
                tasksError = errorMessage
            )
        }
    }

    private suspend fun buildZoneGroupsFromLookup(
        zones: List<LookupOptionResponse>,
        fallbackTasks: List<TaskResponse>
    ): List<ZoneTaskGroupUiState> = coroutineScope {
        val zoneResults = zones.map { zone ->
            async {
                zone to AppContainer.taskRepository.getTasksByZone(zone.id)
            }
        }.map { it.await() }

        val groups = zoneResults.map { (zone, result) ->
            val tasks = when (result) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> emptyList()
            }

            ZoneTaskGroupUiState(
                zoneId = zone.id,
                zoneName = zone.name,
                tasks = tasks
            )
        }.toMutableList()

        val unassignedTasks = fallbackTasks.filter { it.zoneId == null }
        if (unassignedTasks.isNotEmpty()) {
            groups += ZoneTaskGroupUiState(
                zoneId = null,
                zoneName = "Sin zona",
                tasks = unassignedTasks
            )
        }

        groups
    }

    private fun buildZoneGroupsFromTasks(tasks: List<TaskResponse>): List<ZoneTaskGroupUiState> {
        return tasks
            .groupBy { it.zoneId }
            .entries
            .sortedBy { it.key ?: Int.MAX_VALUE }
            .map { (zoneId, zoneTasks) ->
                ZoneTaskGroupUiState(
                    zoneId = zoneId,
                    zoneName = zoneId?.let { "Zona $it" } ?: "Sin zona",
                    tasks = zoneTasks
                )
            }
    }
}

class SpaceDetailViewModelFactory(
    private val spaceRepository: SpaceRepository,
    private val spaceId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpaceDetailViewModel(
            spaceRepository = spaceRepository,
            spaceId = spaceId
        ) as T
}
