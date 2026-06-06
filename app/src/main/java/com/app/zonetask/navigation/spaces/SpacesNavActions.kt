package com.app.zonetask.navigation.spaces

data class SpacesNavActions(
    val onOpenDetail: (spaceId: Int) -> Unit,
    val onOpenCreate: () -> Unit,
    val onOpenEdit: (spaceId: Int) -> Unit,
    val onOpenPermissions: (spaceId: Int) -> Unit,
    val onCreateTaskForSpace: (spaceId: Int) -> Unit,
    val onOpenPlans: (spaceId: Int) -> Unit,
    val onOpenCompletedTasks: (spaceId: Int) -> Unit,
    val onOpenStatistics: (spaceId: Int, userId: Int) -> Unit,
    val onBack: () -> Unit,
    val onSpaceCreated: (message: String) -> Unit,
    val onSpaceEdited: (message: String) -> Unit,
    val onSpaceDeleted: (message: String) -> Unit
)