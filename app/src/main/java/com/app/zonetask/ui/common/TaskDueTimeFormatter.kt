package com.app.zonetask.ui.common

import com.app.zonetask.data.remote.dto.TaskAssignmentResponse

data class TaskDueTimeUiState(
    val label: String = "No due date",
    val statusKey: String = "none",
    val assignmentId: Int? = null,
    val canComplete: Boolean = false
)

fun List<TaskAssignmentResponse>.resolveDueTimeUiState(currentUserId: Int? = null): TaskDueTimeUiState {
    // The same selected assignment drives both the due-time label and the Complete button.
    val dueAssignment = selectRelevantDueAssignment(currentUserId) ?: return TaskDueTimeUiState()

    return TaskDueTimeUiState(
        label = dueAssignment.dueStatusLabel.takeIf { it.isNotBlank() } ?: "No due date",
        statusKey = dueAssignment.dueStatusKey.ifBlank { "none" },
        assignmentId = dueAssignment.assignmentId,
        // The button is only enabled for the user's own active assignment.
        canComplete = currentUserId != null &&
            dueAssignment.assignedUserId == currentUserId &&
            !dueAssignment.status.equals("completed", ignoreCase = true)
    )
}

private fun List<TaskAssignmentResponse>.selectRelevantDueAssignment(currentUserId: Int?): TaskAssignmentResponse? {
    if (isEmpty()) return null

    // Completed assignments are no longer actionable; prefer the user's current active round.
    val activeAssignments = filterNot { it.status.equals("completed", ignoreCase = true) }
    val prioritized = when {
        currentUserId != null -> {
            // In a rotation, "mine" means the active assignment where assigned_user_id matches the session user.
            val mine = activeAssignments.firstOrNull { it.assignedUserId == currentUserId }
            mine ?: activeAssignments.firstOrNull() ?: firstOrNull()
        }
        activeAssignments.isNotEmpty() -> activeAssignments.firstOrNull()
        else -> firstOrNull()
    } ?: return null

    return prioritized
}
