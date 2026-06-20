package com.app.zonetask.ui.common

import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.data.remote.dto.TaskAssignmentResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TaskDueTimeUiState(
    val label: String = "Sin fecha límite",
    val statusKey: String = "none",
    val assignmentId: Int? = null,
    val canComplete: Boolean = false
)

fun List<TaskAssignmentResponse>.resolveDueTimeUiState(currentUserId: Int? = null): TaskDueTimeUiState {
    // The same selected assignment drives both the due-time label and the Complete button.
    val dueAssignment = selectRelevantDueAssignment(currentUserId) ?: return TaskDueTimeUiState()

    return TaskDueTimeUiState(
        label = dueAssignment.dueStatusLabel.takeIf { it.isNotBlank() } ?: "Sin fecha límite",
        statusKey = dueAssignment.dueStatusKey.ifBlank { "none" },
        assignmentId = dueAssignment.assignmentId,
        // The button is only enabled for the user's own active assignment.
        canComplete = currentUserId != null &&
            dueAssignment.assignedUserId == currentUserId &&
            !dueAssignment.status.equals("completed", ignoreCase = true)
    )
}

fun TaskResponse.resolveDueTimeUiState(
    assignments: List<TaskAssignmentResponse>,
    currentUserId: Int? = null
): TaskDueTimeUiState {
    val dueAssignment = assignments.selectRelevantDueAssignment(currentUserId)
    val plannedDueAt = resolvePlannedDueAt()

    if (dueAssignment == null && plannedDueAt == null) {
        return TaskDueTimeUiState()
    }

    if (dueAssignment?.status?.equals("completed", ignoreCase = true) == true) {
        return TaskDueTimeUiState(
            label = dueAssignment.dueStatusLabel.takeIf { it.isNotBlank() } ?: "Completed",
            statusKey = "completed",
            assignmentId = dueAssignment.assignmentId,
            canComplete = false
        )
    }

    if (plannedDueAt != null) {
        val now = Date()
        val remainingMillis = plannedDueAt.time - now.time
        val statusKey = if (remainingMillis >= 0) "upcoming" else "overdue"

        return TaskDueTimeUiState(
            label = formatRelativeTime(remainingMillis, isFuture = remainingMillis >= 0),
            statusKey = statusKey,
            assignmentId = dueAssignment?.assignmentId,
            canComplete = dueAssignment != null &&
                currentUserId != null &&
                dueAssignment.assignedUserId == currentUserId &&
                !dueAssignment.status.equals("completed", ignoreCase = true)
        )
    }

    return TaskDueTimeUiState(
        label = dueAssignment?.dueStatusLabel?.takeIf { it.isNotBlank() } ?: "Sin fecha límite",
        statusKey = dueAssignment?.dueStatusKey?.ifBlank { "none" } ?: "none",
        assignmentId = dueAssignment?.assignmentId,
        canComplete = dueAssignment != null &&
            currentUserId != null &&
            dueAssignment.assignedUserId == currentUserId &&
            dueAssignment.status.equals("completed", ignoreCase = true).not()
    )
}

private fun TaskResponse.resolvePlannedDueAt(): Date? {
    val startDateText = startDate?.takeIf { it.isNotBlank() }?.take(10) ?: return null
    val scheduledTimeText = scheduledTime?.takeIf { it.isNotBlank() } ?: return null

    val date = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDateText)
    }.getOrNull() ?: return null

    val timeParts = parseScheduledTime(scheduledTimeText) ?: return null
    return Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, timeParts[0])
        set(Calendar.MINUTE, timeParts[1])
        set(Calendar.SECOND, timeParts[2])
        set(Calendar.MILLISECOND, 0)
    }.time
}

private fun parseScheduledTime(value: String): IntArray? {
    val normalized = value.trim()
    val formatterWithSeconds = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formatterWithoutSeconds = SimpleDateFormat("HH:mm", Locale.getDefault())

    val parsed = runCatching { formatterWithSeconds.parse(normalized) }.getOrNull()
        ?: runCatching { formatterWithoutSeconds.parse(normalized) }.getOrNull()
        ?: return null

    val calendar = Calendar.getInstance().apply { time = parsed }
    return intArrayOf(
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND)
    )
}

private fun formatRelativeTime(millis: Long, isFuture: Boolean): String {
    val absoluteMinutes = kotlin.math.abs(millis) / 60000.0
    if (absoluteMinutes < 1.0) {
        return if (isFuture) "Due in less than 1 minute" else "Overdue by less than 1 minute"
    }

    val totalMinutes = absoluteMinutes.toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    if (totalMinutes < 1440) {
        val formatted = String.format(Locale.getDefault(), "%d:%02d", hours, minutes)
        return if (isFuture) "Due in $formatted" else "Overdue by $formatted"
    }

    val days = totalMinutes / 1440
    return if (isFuture) {
        "Due in $days day${if (days == 1) "" else "s"}"
    } else {
        "Overdue by $days day${if (days == 1) "" else "s"}"
    }
}

private fun List<TaskAssignmentResponse>.selectRelevantDueAssignment(currentUserId: Int?): TaskAssignmentResponse? {
    if (isEmpty()) return null

    // Completed, skipped, and cancelled assignments are no longer actionable.
    val activeAssignments = filterNot { assignment ->
        assignment.status.equals("completed", ignoreCase = true) ||
            assignment.status.equals("skipped", ignoreCase = true) ||
            assignment.status.equals("cancelled", ignoreCase = true)
    }

    return activeAssignments.firstOrNull() ?: firstOrNull()
}
