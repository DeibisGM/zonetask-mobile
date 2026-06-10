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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.data.remote.dto.CompletedTaskHistoryResponse
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import java.util.Calendar
import java.util.TimeZone

private val OnTimeColor = Color(0xFF66BB6A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTaskHistoryScreen(
    spaceId: Int,
    modifier: Modifier = Modifier,
    viewModel: CompletedTaskHistoryViewModel = viewModel(
        factory = CompletedTaskHistoryViewModelFactory(spaceId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { info ->
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@collect
                if (lastVisible >= info.totalItemsCount - 3) {
                    viewModel.loadNextPage()
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {

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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DatePickerChip(
                        label = "Desde",
                        selectedDate = uiState.dateFrom,
                        onDateSelected = viewModel::onDateFromChanged,
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerChip(
                        label = "Hasta",
                        selectedDate = uiState.dateTo,
                        onDateSelected = viewModel::onDateToChanged,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = viewModel::applyFilters,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
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
                if (uiState.dateFrom.isNotBlank() || uiState.dateTo.isNotBlank()) {
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
                    Text("No se encontraron tareas completadas.", color = AppSecondaryText)
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${uiState.totalCount} ${if (uiState.totalCount != 1) "tareas completas" else "tarea completa"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppSecondaryText
                        )
                    }

                    items(uiState.items) { item ->
                        CompletionHistoryCard(item)
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AppPrimary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }

                    if (!uiState.isLoadingMore && uiState.currentPage < uiState.totalPages) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = viewModel::loadNextPage) {
                                    Text("Cargar más", color = AppPrimary)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
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
private fun CompletionHistoryCard(item: CompletedTaskHistoryResponse) {
    val isOnTime = item.completionType?.equals("on_time", ignoreCase = true) == true
    val typeColor = if (isOnTime) OnTimeColor else AppError
    val typeLabel = if (isOnTime) "A tiempo" else "Tardía"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.taskTitle ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            null,
                            tint = typeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item.zoneName?.let { zone ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Place, null, tint = AppSecondaryText, modifier = Modifier.size(14.dp))
                    Text(zone, style = MaterialTheme.typography.bodySmall, color = AppSecondaryText)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Person, null, tint = AppSecondaryText, modifier = Modifier.size(14.dp))
                Text(item.completedBy ?: "—", style = MaterialTheme.typography.bodySmall, color = AppSecondaryText)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = AppSecondaryText, modifier = Modifier.size(14.dp))
                Text(
                    if (item.completedAt != null) formatCompletedAt(item.completedAt) else "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppSecondaryText
                )
            }
        }
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

private fun formatCompletedAt(raw: String): String {
    return try {
        val datePart = raw.substringBefore('T')
        val timePart = raw.substringAfter('T', "").substringBefore('.')
        if (timePart.isNotBlank()) "$datePart  $timePart" else datePart
    } catch (_: Exception) {
        raw
    }
}
