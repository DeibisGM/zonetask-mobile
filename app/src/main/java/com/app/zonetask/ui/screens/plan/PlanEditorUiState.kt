package com.app.zonetask.ui.screens.plan

data class PlanEditorUiState(
    // null → new plan (not saved yet); non-null → existing plan being edited
    val planId:            Int?    = null,
    val spaceId:           Int     = 0,
    val templateId:        Int?    = null,
    val name:              String  = "",
    val canvasWidth:       String  = "240",
    val canvasHeight:      String  = "240",
    val setupComplete:     Boolean = false,
    val zones:             List<PlanZoneDraft> = emptyList(),
    val selectedZoneId:    String? = null,
    val isLoading:         Boolean = false,
    val isLoadingTemplate: Boolean = false,
    val isSaving:          Boolean = false,
    val isDirty:           Boolean = false,
    val isSaved:           Boolean = false,
    val errorBanner:       String? = null
)
