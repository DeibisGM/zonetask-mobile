package com.app.zonetask.ui.screens.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.data.remote.dto.OverdueByUserEntry
import com.app.zonetask.data.remote.dto.OverdueByZoneEntry
import com.app.zonetask.data.remote.dto.OverdueTrendBucket
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import java.util.Calendar
import java.util.TimeZone

private fun formatDate(raw: String): String {
    val formats = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "yyyy-MM")
    for (pattern in formats) {
        try {
            val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(raw) ?: continue
            val out = if (pattern == "yyyy-MM") "MM/yyyy" else "dd/MM/yyyy"
            return java.text.SimpleDateFormat(out, java.util.Locale.getDefault()).format(date)
        } catch (_: Exception) { }
    }
    return raw
}

private fun shortLabel(raw: String): String {
    // "2026-06-09" -> "09/06" ; "2026-06" -> "06/26"
    val parts = raw.split("-")
    return when (parts.size) {
        3 -> "${parts[2]}/${parts[1]}"
        2 -> "${parts[1]}/${parts[0].takeLast(2)}"
        else -> raw
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

private fun rateColor(rate: Double): Color = when {
    rate >= 50.0 -> AppError
    rate >= 20.0 -> Color(0xFFFFB300)
    else         -> AppPrimary
}

@Composable
fun OverdueTrendsScreen(
    spaceId: Int,
    modifier: Modifier = Modifier,
    viewModel: OverdueTrendsViewModel = viewModel(
        factory = OverdueTrendsViewModelFactory(spaceId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        Surface(color = AppSurface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Período",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppSecondaryText,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            color = AppPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        IconButton(onClick = viewModel::refresh, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar", tint = AppPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatsPeriod.entries.forEach { period ->
                        SelectableChip(
                            selected = uiState.selectedPeriod == period,
                            label = period.label,
                            onClick = { viewModel.onPeriodSelected(period) }
                        )
                    }
                }

                if (uiState.selectedPeriod == StatsPeriod.CUSTOM) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrendsDatePickerChip("Desde", uiState.dateFrom, viewModel::onDateFromChanged, Modifier.weight(1f))
                        TrendsDatePickerChip("Hasta", uiState.dateTo, viewModel::onDateToChanged, Modifier.weight(1f))
                        Button(
                            onClick = viewModel::applyCustomRange,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text("Aplicar", color = Color.Black, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Agrupar por", style = MaterialTheme.typography.labelMedium, color = AppSecondaryText)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrendGroupBy.entries.forEach { group ->
                        SelectableChip(
                            selected = uiState.groupBy == group,
                            label = group.label,
                            onClick = { viewModel.onGroupBySelected(group) }
                        )
                    }
                }

                if (uiState.groupBy == TrendGroupBy.TIME) {
                    Spacer(Modifier.height(10.dp))
                    Text("Intervalo", style = MaterialTheme.typography.labelMedium, color = AppSecondaryText)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TrendInterval.entries.forEach { iv ->
                            val active = uiState.interval == iv ||
                                (uiState.interval == null && uiState.trends?.interval == iv.apiValue)
                            SelectableChip(
                                selected = active,
                                label = iv.label,
                                onClick = { viewModel.onIntervalSelected(iv) }
                            )
                        }
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            }

            uiState.errorMessage != null -> {
                Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.errorMessage!!, color = AppSecondaryText)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::retry) { Text("Reintentar", color = AppPrimary) }
                    }
                }
            }

            uiState.trends != null -> {
                val trends = uiState.trends!!
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${formatDate(trends.dateFrom)}  –  ${formatDate(trends.dateTo)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppSecondaryText
                            )
                        }
                    }

                    item { OverdueSummaryCard(totalOverdue = trends.totalOverdue, totalDue = trends.totalDue, rate = trends.overdueRate) }

                    when (uiState.groupBy) {
                        TrendGroupBy.TIME -> {
                            item { TrendChartCard(buckets = trends.buckets) }
                        }
                        TrendGroupBy.USER -> {
                            if (trends.byUser.isEmpty()) {
                                item { EmptyHint("No hay datos de vencimiento por usuario en este rango.") }
                            } else {
                                items(trends.byUser, key = { it.userId }) { UserOverdueCard(it) }
                            }
                        }
                        TrendGroupBy.ZONE -> {
                            if (trends.byZone.isEmpty()) {
                                item { EmptyHint("No hay datos de vencimiento por zona en este rango.") }
                            } else {
                                items(trends.byZone, key = { it.zoneId ?: -1 }) { ZoneOverdueCard(it) }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun OverdueSummaryCard(totalOverdue: Int, totalDue: Int, rate: Double) {
    val color = rateColor(rate)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryStat(value = totalOverdue.toString(), label = "Vencidas", color = color, modifier = Modifier.weight(1f))
            SummaryStat(value = totalDue.toString(), label = "Con vencimiento", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            SummaryStat(value = "${"%.1f".format(rate)}%", label = "Tasa vencidas", color = color, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppSecondaryText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TrendChartCard(buckets: List<OverdueTrendBucket>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tareas vencidas por período", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

            val maxOverdue = (buckets.maxOfOrNull { it.overdueCount } ?: 0).coerceAtLeast(1)
            val maxBarHeight = 140.dp

            if (buckets.all { it.overdueCount == 0 }) {
                Text("Sin tareas vencidas en este rango.", style = MaterialTheme.typography.bodySmall, color = AppSecondaryText)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    buckets.forEach { bucket ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                if (bucket.overdueCount > 0) bucket.overdueCount.toString() else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppSecondaryText
                            )
                            val fraction = bucket.overdueCount.toFloat() / maxOverdue.toFloat()
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .heightIn(min = 2.dp)
                                    .height(maxBarHeight * fraction.coerceIn(0.02f, 1f))
                                    .background(rateColor(bucket.overdueRate), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                            Text(
                                shortLabel(bucket.label),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppSecondaryText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserOverdueCard(entry: OverdueByUserEntry) {
    OverdueBreakdownCard(
        leadingIcon = { TintCircle(Icons.Outlined.Person) },
        title = entry.fullName.ifBlank { entry.username },
        subtitle = "@${entry.username}",
        overdueCount = entry.overdueCount,
        dueCount = entry.dueCount,
        rate = entry.overdueRate
    )
}

@Composable
private fun ZoneOverdueCard(entry: OverdueByZoneEntry) {
    OverdueBreakdownCard(
        leadingIcon = { TintCircle(Icons.Outlined.Place) },
        title = entry.zoneName,
        subtitle = null,
        overdueCount = entry.overdueCount,
        dueCount = entry.dueCount,
        rate = entry.overdueRate
    )
}

@Composable
private fun OverdueBreakdownCard(
    leadingIcon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    overdueCount: Int,
    dueCount: Int,
    rate: Double
) {
    val color = rateColor(rate)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingIcon()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    subtitle ?: "$overdueCount de $dueCount con vencimiento",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppSecondaryText
                )
                if (subtitle != null) {
                    Text("$overdueCount de $dueCount con vencimiento", style = MaterialTheme.typography.labelSmall, color = AppSecondaryText)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$overdueCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                Text("${"%.0f".format(rate)}%", style = MaterialTheme.typography.labelSmall, color = AppSecondaryText)
            }
        }
    }
}

@Composable
private fun TintCircle(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier.size(36.dp).background(AppPrimary.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = AppPrimary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = AppSecondaryText, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SelectableChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppPrimary,
            selectedLabelColor = Color.Black,
            containerColor = AppSurface,
            labelColor = AppSecondaryText
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = AppBorder,
            selectedBorderColor = AppPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendsDatePickerChip(
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
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (hasDate) AppPrimary else AppSecondaryText),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(if (hasDate) formatDate(selectedDate) else label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(millisToDateString(it)) }
                    showDialog = false
                }) { Text("Aceptar", color = AppPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = AppSecondaryText) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
