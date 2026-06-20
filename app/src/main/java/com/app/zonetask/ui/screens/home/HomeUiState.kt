package com.app.zonetask.ui.screens.home

import com.app.zonetask.domain.model.FloorPlan
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.components.FloorPlanZonePreview

data class HomeTaskItem(
    val taskId: Int,
    val title: String,
    val scheduledTime: String?,
    val zoneId: Int?,
    val zoneName: String,
    val assigneeName: String?,
    val dueLabel: String,
    val dueStatusKey: String
)

data class HomeUiState(
    val spaceName: String = "",
    val spaceType: String = "",
    val plans: List<FloorPlan> = emptyList(),
    val activePlan: FloorPlan? = null,
    val planZones: List<FloorPlanZonePreview> = emptyList(),
    val zoneTaskCounts: Map<Int, Int> = emptyMap(),
    val pendingTasks: List<HomeTaskItem> = emptyList(),
    val userSpaces: List<Space> = emptyList(),
    val currentSpaceId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val plansErrorMessage: String? = null,
    val tasksErrorMessage: String? = null,
    val isZonesLoading: Boolean = false,
    val zonesErrorMessage: String? = null
)
