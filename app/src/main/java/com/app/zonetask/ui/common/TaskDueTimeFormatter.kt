package com.app.zonetask.ui.common

import com.app.zonetask.data.remote.dto.TaskAssignmentResponse
import java.time.Instant
import java.time.OffsetDateTime

data class TaskDueTimeUiState(
    val label: String = "Sin fecha límite",
    val statusKey: String = "none"
)

fun List<TaskAssignmentResponse>.resolveDueTimeUiState(): TaskDueTimeUiState {
    val dueAssignment = selectRelevantDueAssignment() ?: return TaskDueTimeUiState()

    return TaskDueTimeUiState(
        label = dueAssignment.dueStatusLabel.takeIf { it.isNotBlank() } ?: "Sin fecha límite",
        statusKey = dueAssignment.dueStatusKey.ifBlank { "none" }
    )
}

private fun List<TaskAssignmentResponse>.selectRelevantDueAssignment(): TaskAssignmentResponse? {
    if (isEmpty()) return null

    val activeAssignments = filterNot { it.status.equals("completed", ignoreCase = true) }
    val prioritized = if (activeAssignments.isNotEmpty()) activeAssignments else this

    return prioritized.minWithOrNull(
        compareBy<TaskAssignmentResponse> { it.dueAt.toInstantOrNull() ?: Instant.MAX }
            .thenBy { it.assignmentId }
    )
}

private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null

    return runCatching { OffsetDateTime.parse(this).toInstant() }.getOrNull()
}
