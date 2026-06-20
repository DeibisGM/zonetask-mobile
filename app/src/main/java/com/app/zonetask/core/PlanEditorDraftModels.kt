package com.app.zonetask.core

import com.app.zonetask.ui.screens.plan.PlanZoneDraftSnapshot

data class PlanEditorDraftSnapshot(
    val name: String,
    val canvasWidth: String,
    val canvasHeight: String,
    val setupComplete: Boolean = false,
    val selectedZoneId: String?,
    val zones: List<PlanZoneDraftSnapshot>
)
