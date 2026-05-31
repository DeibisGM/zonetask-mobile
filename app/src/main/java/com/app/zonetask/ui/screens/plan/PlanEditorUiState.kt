package com.app.zonetask.ui.screens.plan

data class PlanEditorUiState(
    // null → new plan (not saved yet); non-null → existing plan being edited
    val planId:       Int?    = null,
    val spaceId:      Int     = 0,
    val name:         String  = "",
    val canvasWidth:  String  = "1000",
    val canvasHeight: String  = "800",
    val isLoading:    Boolean = false,
    val isSaving:     Boolean = false,
    val isDirty:      Boolean = false,
    val isSaved:      Boolean = false,
    val errorBanner:  String? = null
)
