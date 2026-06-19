package com.app.zonetask.ui.screens.taskhistory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.data.remote.dto.RotationHistoryResponse
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import java.util.Calendar
import java.util.TimeZone

private data class RotationReasonFilter(
    val key: String,
    val label: String
)

private val RotationReasonFilters = listOf(
    RotationReasonFilter("all", "All"),
    RotationReasonFilter("schedule", "Schedule"),
    RotationReasonFilter("completion", "Completion"),
    RotationReasonFilter("manual", "Manual"),
    RotationReasonFilter("member_left", "Member left"),
    RotationReasonFilter("recalculation", "Recalculation")
)

private val ScheduleColor = AppPrimary
private val CompletionColor = Color(0xFF66BB6A)
private val ManualColor = Color(0xFFFFB74D)
private val MemberLeftColor = AppError
private val RecalculationColor = AppSecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRotationHistoryScreen(
    taskId: Int,
    modifier: Modifier = Modifier,
    viewModel: TaskRotationHistoryViewModel = viewModel(
        factory = TaskRotationHistoryViewModelFactory(taskId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = AppSurface,
            border = BorderStroke(0.dp, AppBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    "Task",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = uiState.taskTitle.ifBlank { "Task #$taskId" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                uiState.taskDescription?.takeIf { it.isNotBlank() }?.let { description ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppSecondaryText
                    )
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (uiState.isTaskRotating) AppPrimary.copy(alpha = 0.16f) else AppBorder
                ) {
                    Text(
                        text = if (uiState.isTaskRotating) "Rotation enabled" else "Rotation disabled",
                        color = if (uiState.isTaskRotating) AppPrimary else AppSecondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = AppBorder)

        Surface(
            color = AppSurface,
            border = BorderStroke(0.dp, AppBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DatePickerChip(
                        label = "From",
                        selectedDate = uiState.dateFrom,
                        onDateSelected = viewModel::onDateFromChanged,
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerChip(
                        label = "To",
                        selectedDate = uiState.dateTo,
                        onDateSelected = viewModel::onDateToChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RotationReasonFilters) { filter ->
                        RotationReasonChip(
                            label = filter.label,
                            selected = uiState.triggerReason == filter.key,
                            onClick = { viewModel.onTriggerReasonChanged(filter.key) }
                        )
                    }
                }
                if (hasFilters(uiState)) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = viewModel::clearFilters,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = AppSecondaryText
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Clear filters",
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = viewModel::applyFilters,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Apply",
                        color = AppOnPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        HorizontalDivider(color = AppBorder)

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            }

            uiState.errorMessage != null && uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = AppSecondaryText)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::retry) {
                            Text("Retry", color = AppPrimary)
                        }
                    }
                }
            }

            uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No rotation records found.",
                        color = AppSecondaryText
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.items.size} ${if (uiState.items.size == 1) "entry" else "entries"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppSecondaryText
                        )
                    }

                    items(uiState.items) { item ->
                        RotationHistoryCard(item)
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerChip(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val hasDate = selectedDate.isNotBlank()

    OutlinedButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (hasDate) AppPrimary else AppBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (hasDate) AppPrimary else AppSecondaryText
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (hasDate) selectedDate else label,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millisToDateString(millis))
                    }
                    showDialog = false
                }) {
                    Text("Accept", color = AppPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = AppSecondaryText)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RotationReasonChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppPrimary,
            selectedLabelColor = AppOnPrimary,
            containerColor = AppSurface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) AppPrimary else AppBorder,
            selectedBorderColor = AppPrimary
        )
    )
}

@Composable
private fun RotationHistoryCard(item: RotationHistoryResponse) {
    val reasonColor = rotationReasonColor(item.triggerReason)
    val reasonLabel = rotationReasonLabel(item.triggerReason)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Round #${item.rotationRound}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = reasonColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Repeat,
                            contentDescription = null,
                            tint = reasonColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = reasonLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = reasonColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = AppBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryPersonColumn(
                    label = "From",
                    value = item.fromDisplayName?.takeIf { it.isNotBlank() } ?: "Initial assignment",
                    modifier = Modifier.weight(1f)
                )
                HistoryPersonColumn(
                    label = "To",
                    value = item.toDisplayName,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = formatRotationDateTime(item.triggeredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppSecondaryText
                )
            }
        }
    }
}

@Composable
private fun HistoryPersonColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppSecondaryText
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = AppSecondaryText,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun hasFilters(uiState: TaskRotationHistoryUiState): Boolean {
    return uiState.dateFrom.isNotBlank() || uiState.dateTo.isNotBlank() || uiState.triggerReason != "all"
}

private fun rotationReasonLabel(reason: String): String {
    return when (reason.lowercase()) {
        "schedule", "scheduled_time" -> "Schedule"
        "completion" -> "Completion"
        "manual" -> "Manual"
        "member_left" -> "Member left"
        "recalculation" -> "Recalculation"
        else -> reason.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun rotationReasonColor(reason: String): Color {
    return when (reason.lowercase()) {
        "schedule", "scheduled_time" -> ScheduleColor
        "completion" -> CompletionColor
        "manual" -> ManualColor
        "member_left" -> MemberLeftColor
        "recalculation" -> RecalculationColor
        else -> AppSecondaryText
    }
}

private fun millisToDateString(millis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return "%04d-%02d-%02d".format(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}

private fun formatRotationDateTime(raw: String): String {
    return try {
        val datePart = raw.substringBefore('T')
        val timePart = raw.substringAfter('T', "")
            .substringBefore('.')
            .substringBefore('Z')
            .takeIf { it.isNotBlank() }
            ?.take(5)

        if (!timePart.isNullOrBlank()) "$datePart  $timePart" else datePart
    } catch (_: Exception) {
        raw
    }
}
