package com.app.zonetask.ui.screens.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Workspaces
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.app.zonetask.data.remote.dto.SpaceReportEntry
import com.app.zonetask.data.remote.dto.UserSpaceReportsResponse
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import java.util.Calendar
import java.util.TimeZone

private val OnTimeColor = Color(0xFF66BB6A)
private val RankGoldColor = Color(0xFFFFC107)
private val RankSilverColor = Color(0xFF9E9E9E)
private val RankBronzeColor = Color(0xFF8D6E63)

private fun formatDate(raw: String): String {
    val formats = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd")
    for (pattern in formats) {
        try {
            val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(raw) ?: continue
            return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date)
        } catch (_: Exception) { }
    }
    return raw
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

private fun rateColor(rate: Double): Color = when {
    rate >= 80.0 -> OnTimeColor
    rate >= 50.0 -> AppPrimary
    else         -> AppError
}

@Composable
fun SpaceReportsScreen(
    userId: Int,
    modifier: Modifier = Modifier,
    viewModel: SpaceReportsViewModel = viewModel(
        factory = SpaceReportsViewModelFactory(userId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        Surface(
            color = AppSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                Text(
                    "Período",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = uiState.selectedPeriod == period,
                            onClick  = { viewModel.onPeriodSelected(period) },
                            label    = { Text(period.label, style = MaterialTheme.typography.labelMedium) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppPrimary,
                                selectedLabelColor     = Color.Black,
                                containerColor         = AppSurface,
                                labelColor             = AppSecondaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled             = true,
                                selected            = uiState.selectedPeriod == period,
                                borderColor         = AppBorder,
                                selectedBorderColor = AppPrimary
                            )
                        )
                    }
                }

                if (uiState.selectedPeriod == StatsPeriod.CUSTOM) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpaceReportsDatePickerChip(
                            label          = "Desde",
                            selectedDate   = uiState.dateFrom,
                            onDateSelected = viewModel::onDateFromChanged,
                            modifier       = Modifier.weight(1f)
                        )
                        SpaceReportsDatePickerChip(
                            label          = "Hasta",
                            selectedDate   = uiState.dateTo,
                            onDateSelected = viewModel::onDateToChanged,
                            modifier       = Modifier.weight(1f)
                        )
                        Button(
                            onClick        = viewModel::applyCustomRange,
                            shape          = RoundedCornerShape(10.dp),
                            colors         = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Aplicar",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "Ordenar por",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpaceReportSortBy.entries.forEach { sort ->
                        FilterChip(
                            selected = uiState.sortBy == sort,
                            onClick  = { viewModel.onSortBySelected(sort) },
                            label    = { Text(sort.label, style = MaterialTheme.typography.labelMedium) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppPrimary,
                                selectedLabelColor     = Color.Black,
                                containerColor         = AppSurface,
                                labelColor             = AppSecondaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled             = true,
                                selected            = uiState.sortBy == sort,
                                borderColor         = AppBorder,
                                selectedBorderColor = AppPrimary
                            )
                        )
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            }

            uiState.errorMessage != null -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = AppSecondaryText)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::retry) {
                            Text("Reintentar", color = AppPrimary)
                        }
                    }
                }
            }

            uiState.reports != null && uiState.reports!!.spaces.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "No perteneces a ningún espacio todavía.",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.reports != null -> {
                val reports = uiState.reports!!
                LazyColumn(
                    modifier            = Modifier.weight(1f),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${formatDate(reports.dateFrom)}  –  ${formatDate(reports.dateTo)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppSecondaryText
                            )
                            Text(
                                "${reports.totalSpaces} espacios",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppSecondaryText
                            )
                        }
                    }

                    item { OverallSummaryCard(reports = reports) }

                    items(reports.spaces, key = { it.spaceId }) { entry ->
                        SpaceReportCard(entry = entry)
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun OverallSummaryCard(reports: UserSpaceReportsResponse) {
    val color = rateColor(reports.completionRate)

    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = AppSurface),
        border   = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Resumen general",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Todos tus espacios",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppSecondaryText
                    )
                }
                Text(
                    text       = "${"%.1f".format(reports.completionRate)}%",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
            }

            LinearProgressIndicator(
                progress = { (reports.completionRate / 100.0).toFloat().coerceIn(0f, 1f) },
                color = color,
                trackColor = AppBorder,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(Icons.Outlined.AssignmentTurnedIn, AppPrimary, reports.totalAssigned.toString(), "Asignadas", Modifier.weight(1f))
                MetricChip(Icons.Outlined.CheckCircle, OnTimeColor, reports.completedTasks.toString(), "Completadas", Modifier.weight(1f))
                MetricChip(Icons.Outlined.Error, AppError, reports.overdueTasks.toString(), "Vencidas", Modifier.weight(1f))
                MetricChip(Icons.Outlined.HourglassEmpty, AppSecondaryText, reports.pendingTasks.toString(), "Pendientes", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SpaceReportCard(entry: SpaceReportEntry) {
    val color = rateColor(entry.completionRate)
    val rankColor = when (entry.rank) {
        1    -> RankGoldColor
        2    -> RankSilverColor
        3    -> RankBronzeColor
        else -> AppSecondaryText
    }

    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = AppSurface),
        border   = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .background(rankColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "#${entry.rank}",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = rankColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = entry.spaceName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Workspaces, null, tint = AppSecondaryText, modifier = Modifier.size(12.dp))
                        Text(
                            text  = entry.spaceType.ifBlank { entry.role },
                            style = MaterialTheme.typography.labelSmall,
                            color = AppSecondaryText
                        )
                    }
                }

                Text(
                    text       = "${"%.1f".format(entry.completionRate)}%",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
            }

            LinearProgressIndicator(
                progress = { (entry.completionRate / 100.0).toFloat().coerceIn(0f, 1f) },
                color = color,
                trackColor = AppBorder,
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(Icons.Outlined.AssignmentTurnedIn, AppPrimary, entry.totalAssigned.toString(), "Asignadas", Modifier.weight(1f))
                MetricChip(Icons.Outlined.CheckCircle, OnTimeColor, entry.completedTasks.toString(), "Completadas", Modifier.weight(1f))
                MetricChip(Icons.Outlined.Error, AppError, entry.overdueTasks.toString(), "Vencidas", Modifier.weight(1f))
                MetricChip(Icons.Outlined.HourglassEmpty, AppSecondaryText, entry.pendingTasks.toString(), "Pendientes", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = tint.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppSecondaryText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceReportsDatePickerChip(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val hasDate = selectedDate.isNotBlank()

    OutlinedButton(
        onClick  = { showDialog = true },
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(1.dp, if (hasDate) AppPrimary else AppBorder),
        colors   = ButtonDefaults.outlinedButtonColors(
            contentColor = if (hasDate) AppPrimary else AppSecondaryText
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text     = if (hasDate) formatDate(selectedDate) else label,
            style    = MaterialTheme.typography.bodySmall,
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
