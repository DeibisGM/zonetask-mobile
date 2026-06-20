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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

private data class SpaceRotationReasonFilter(
    val key: String,
    val label: String
)

private val SpaceRotationReasonFilters = listOf(
    SpaceRotationReasonFilter("all", "Todas"),
    SpaceRotationReasonFilter("schedule", "Horario"),
    SpaceRotationReasonFilter("completion", "Finalización"),
    SpaceRotationReasonFilter("manual", "Manual"),
    SpaceRotationReasonFilter("member_left", "Miembro salió"),
    SpaceRotationReasonFilter("recalculation", "Recalculo")
)

private val SpaceScheduleColor = AppPrimary
private val SpaceCompletionColor = Color(0xFF66BB6A)
private val SpaceManualColor = Color(0xFFFFB74D)
private val SpaceMemberLeftColor = AppError
private val SpaceRecalculationColor = AppSecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceRotationHistoryScreen(
    spaceId: Int,
    requestingUserId: Int,
    initialTaskId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: SpaceRotationHistoryViewModel = viewModel(
        factory = SpaceRotationHistoryViewModelFactory(
            spaceId = spaceId,
            requestingUserId = requestingUserId,
            initialTaskId = initialTaskId
        )
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
                    "Historial del espacio",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Cambios de asignación en todo el espacio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = AppPrimary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "Filtros por tarea, zona y usuario",
                        color = AppPrimary,
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
                    "Filtros",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SpaceRotationFilterChip(
                            label = "Tarea",
                            value = uiState.taskOptions.firstOrNull { it.id == uiState.selectedTaskId }?.label
                                ?: "Todas",
                            allLabel = "Todas",
                            selected = uiState.selectedTaskId != null,
                            options = uiState.taskOptions,
                            onSelected = viewModel::onTaskChanged
                        )
                    }
                    item {
                        SpaceRotationFilterChip(
                            label = "Zona",
                            value = uiState.zoneOptions.firstOrNull { it.id == uiState.selectedZoneId }?.label
                                ?: "Todas",
                            allLabel = "Todas",
                            selected = uiState.selectedZoneId != null,
                            options = uiState.zoneOptions,
                            onSelected = viewModel::onZoneChanged
                        )
                    }
                    item {
                        SpaceRotationFilterChip(
                            label = "Usuario",
                            value = uiState.userOptions.firstOrNull { it.id == uiState.selectedUserId }?.label
                                ?: "Todos",
                            allLabel = "Todos",
                            selected = uiState.selectedUserId != null,
                            options = uiState.userOptions,
                            onSelected = viewModel::onUserChanged
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpaceDatePickerChip(
                        label = "Desde",
                        selectedDate = uiState.dateFrom,
                        onDateSelected = viewModel::onDateFromChanged,
                        modifier = Modifier.weight(1f)
                    )
                    SpaceDatePickerChip(
                        label = "Hasta",
                        selectedDate = uiState.dateTo,
                        onDateSelected = viewModel::onDateToChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SpaceRotationReasonFilters) { filter ->
                        SpaceRotationReasonChip(
                            label = filter.label,
                            selected = uiState.triggerReason == filter.key,
                            onClick = { viewModel.onTriggerReasonChanged(filter.key) }
                        )
                    }
                }

                if (hasSpaceRotationFilters(uiState)) {
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
                            "Limpiar filtros",
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
                        "Aplicar",
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
                            Text("Reintentar", color = AppPrimary)
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
                        text = "No se encontraron registros de rotación.",
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
                            text = "${uiState.items.size} ${if (uiState.items.size == 1) "registro" else "registros"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppSecondaryText
                        )
                    }

                    items(uiState.items) { item ->
                        SpaceRotationHistoryCard(item)
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceDatePickerChip(
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
                        onDateSelected(spaceMillisToDateString(millis))
                    }
                    showDialog = false
                }) {
                    Text("Aceptar", color = AppPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = AppSecondaryText)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SpaceRotationFilterChip(
    label: String,
    value: String,
    allLabel: String,
    selected: Boolean,
    options: List<RotationHistoryFilterOption>,
    onSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = {
                Text(
                    text = "$label: $value",
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (selected) AppOnPrimary else AppSecondaryText,
                    modifier = Modifier.size(16.dp)
                )
            },
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SpaceRotationReasonChip(
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
private fun SpaceRotationHistoryCard(item: RotationHistoryResponse) {
    val reasonColor = spaceRotationReasonColor(item.triggerReason)
    val reasonLabel = spaceRotationReasonLabel(item.triggerReason)
    val previousAssignee = item.fromDisplayName?.takeIf { it.isNotBlank() } ?: "Asignación inicial"
    val zoneLabel = item.zoneName?.takeIf { it.isNotBlank() }

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
                    text = "Ronda #${item.rotationRound}",
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

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Tarea",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppSecondaryText
                )
                Text(
                    text = item.taskTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            zoneLabel?.let { zone ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Zona",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppSecondaryText
                    )
                    Text(
                        text = zone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = AppBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpaceHistoryPersonColumn(
                    label = "Asignado anterior",
                    value = previousAssignee,
                    modifier = Modifier.weight(1f)
                )
                SpaceHistoryPersonColumn(
                    label = "Nuevo asignado",
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
                    text = formatSpaceRotationDateTime(item.triggeredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppSecondaryText
                )
            }
        }
    }
}

@Composable
private fun SpaceHistoryPersonColumn(
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

private fun hasSpaceRotationFilters(uiState: SpaceRotationHistoryUiState): Boolean {
    return uiState.selectedTaskId != null ||
        uiState.selectedZoneId != null ||
        uiState.selectedUserId != null ||
        uiState.dateFrom.isNotBlank() ||
        uiState.dateTo.isNotBlank() ||
        uiState.triggerReason != "all"
}

private fun spaceRotationReasonLabel(reason: String): String {
    return when (reason.lowercase()) {
        "schedule", "scheduled_time" -> "Horario"
        "completion" -> "Finalización"
        "manual" -> "Manual"
        "member_left" -> "Miembro salió"
        "recalculation" -> "Recalculo"
        else -> reason.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun spaceRotationReasonColor(reason: String): Color {
    return when (reason.lowercase()) {
        "schedule", "scheduled_time" -> SpaceScheduleColor
        "completion" -> SpaceCompletionColor
        "manual" -> SpaceManualColor
        "member_left" -> SpaceMemberLeftColor
        "recalculation" -> SpaceRecalculationColor
        else -> AppSecondaryText
    }
}

private fun formatSpaceRotationDateTime(value: String): String {
    return value.takeIf { it.length >= 16 }?.let {
        val date = it.substring(0, 10)
        val time = it.substring(11, 16)
        "$date  $time"
    } ?: value
}

private fun spaceMillisToDateString(millis: Long): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return String.format("%04d-%02d-%02d", year, month, day)
}
