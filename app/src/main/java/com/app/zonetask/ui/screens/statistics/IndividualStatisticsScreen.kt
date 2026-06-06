package com.app.zonetask.ui.screens.statistics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.data.remote.dto.UserStatisticsResponse
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

private val OnTimeColor = Color(0xFF66BB6A)

@Composable
fun IndividualStatisticsScreen(
    spaceId: Int,
    userId: Int,
    modifier: Modifier = Modifier,
    viewModel: IndividualStatisticsViewModel = viewModel(
        factory = IndividualStatisticsViewModelFactory(spaceId, userId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        // Period selector
        Surface(
            color = AppSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "Period",
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
                                selectedContainerColor     = AppPrimary,
                                selectedLabelColor         = Color.Black,
                                containerColor             = AppSurface,
                                labelColor                 = AppSecondaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled          = true,
                                selected         = uiState.selectedPeriod == period,
                                borderColor      = AppBorder,
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
                        OutlinedTextField(
                            value       = uiState.dateFrom,
                            onValueChange = viewModel::onDateFromChanged,
                            placeholder = { Text("From (YYYY-MM-DD)", style = MaterialTheme.typography.bodySmall) },
                            singleLine  = true,
                            modifier    = Modifier.weight(1f),
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = AppPrimary,
                                unfocusedBorderColor = AppBorder,
                                focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
                                cursorColor          = AppPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value       = uiState.dateTo,
                            onValueChange = viewModel::onDateToChanged,
                            placeholder = { Text("To (YYYY-MM-DD)", style = MaterialTheme.typography.bodySmall) },
                            singleLine  = true,
                            modifier    = Modifier.weight(1f),
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = AppPrimary,
                                unfocusedBorderColor = AppBorder,
                                focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
                                cursorColor          = AppPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.applyCustomRange() }),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick  = viewModel::applyCustomRange,
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text("Apply", color = Color.Black, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
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
                            Text("Retry", color = AppPrimary)
                        }
                    }
                }
            }

            uiState.statistics != null -> {
                StatisticsContent(
                    stats    = uiState.statistics!!,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    stats: UserStatisticsResponse,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier       = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date range label
        item {
            Text(
                "${stats.dateFrom}  –  ${stats.dateTo}",
                style  = MaterialTheme.typography.labelSmall,
                color  = AppSecondaryText
            )
        }

        // Completion rate — hero card
        item {
            CompletionRateCard(rate = stats.completionRate)
        }

        // Metric grid (2 columns)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    icon      = Icons.Outlined.AssignmentTurnedIn,
                    iconTint  = AppPrimary,
                    label     = "Total Assigned",
                    value     = stats.totalAssigned.toString(),
                    modifier  = Modifier.weight(1f)
                )
                MetricCard(
                    icon      = Icons.Outlined.CheckCircle,
                    iconTint  = OnTimeColor,
                    label     = "Completed",
                    value     = stats.completedTasks.toString(),
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    icon      = Icons.Outlined.Error,
                    iconTint  = AppError,
                    label     = "Overdue",
                    value     = stats.overdueTasks.toString(),
                    modifier  = Modifier.weight(1f)
                )
                MetricCard(
                    icon      = Icons.Outlined.HourglassEmpty,
                    iconTint  = AppSecondaryText,
                    label     = "Pending",
                    value     = stats.pendingTasks.toString(),
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun CompletionRateCard(rate: Double) {
    val rateColor = when {
        rate >= 80.0 -> OnTimeColor
        rate >= 50.0 -> AppPrimary
        else         -> AppError
    }

    Card(
        shape   = RoundedCornerShape(16.dp),
        colors  = CardDefaults.cardColors(containerColor = AppSurface),
        border  = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Percent, null, tint = rateColor, modifier = Modifier.size(18.dp))
                Text(
                    "Completion Rate",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text       = "${"%.1f".format(rate)}%",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = rateColor
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = AppSurface),
        border   = BorderStroke(1.dp, AppBorder),
        modifier = modifier
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppSecondaryText
            )
        }
    }
}
