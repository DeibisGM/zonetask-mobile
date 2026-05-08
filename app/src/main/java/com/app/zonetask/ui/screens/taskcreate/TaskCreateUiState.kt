package com.app.zonetask.ui.screens.taskcreate

data class TaskCreateUiState(
    val title: String = "",
    val description: String = "",
    val targetLevel: String = "space",
    val frequency: String = "once",
    val recurrenceRule: String? = null,
    val scheduledTime: String = "12:00",
    val startDate: String = "",
    val endDate: String? = null,
    val rotating: Boolean = false,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 30,
    val requiresProof: Boolean = false,
    val requiresDescription: Boolean = false,
    val estimatedMinutes: Int? = null,
    val createdBy: Int = 1,
    val categoryId: Int? = 1,
    val spaceId: Int = 1,
    val zoneId: Int? = 1,
    val objectId: Int? = null,
    val objectSelectionEnabled: Boolean = false,
    val selectedObjectIds: List<Int> = emptyList(),
    
    // UI specific
    val deadline: String = "",
    val hour: String = "8:00 PM",
    val assignedSpace: String = "1",
    
    // Validation
    val isTitleValid: Boolean = true,
    val isTargetLevelValid: Boolean = true,
    val isZoneValid: Boolean = true,
    val isFrequencyValid: Boolean = true,
    val isStartDateValid: Boolean = true,
    val isTimeValid: Boolean = true,
    val isEstimatedTimeValid: Boolean = true,
    val showErrors: Boolean = false
) {
    val isValid: Boolean get() = title.isNotBlank() && 
                                 startDate.isNotBlank() && 
                                 scheduledTime.isNotBlank() && 
                                 estimatedMinutes != null
}
