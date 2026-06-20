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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

/**
 * Hub screen that groups every statistics/report option for a space in one place.
 * Owner/admin-only reports are gated by the requesting user's role.
 */
@Composable
fun SpaceStatisticsMenuScreen(
    spaceId: Int,
    userId: Int,
    modifier: Modifier = Modifier,
    onOpenMyStatistics: () -> Unit = {},
    onOpenSpaceStatistics: () -> Unit = {},
    onOpenUserReports: () -> Unit = {},
    onOpenOverdueTrends: () -> Unit = {},
    viewModel: SpaceStatisticsMenuViewModel = viewModel(
        factory = SpaceStatisticsMenuViewModelFactory(spaceId, userId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Estadísticas y reportes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            StatMenuRow(
                icon = Icons.Outlined.BarChart,
                title = "My Statistics",
                subtitle = "Tu desempeño en este espacio",
                onClick = onOpenMyStatistics
            )
        }

        item {
            StatMenuRow(
                icon = Icons.Outlined.Groups,
                title = "Space Statistics",
                subtitle = "Resumen del espacio completo",
                onClick = onOpenSpaceStatistics
            )
        }

        // Owner/admin-only reports.
        if (uiState.canViewReports) {
            item {
                StatMenuRow(
                    icon = Icons.Outlined.Person,
                    title = "Reports by User",
                    subtitle = "Ranking de desempeño por miembro",
                    onClick = onOpenUserReports
                )
            }
            item {
                StatMenuRow(
                    icon = Icons.Outlined.TrendingUp,
                    title = "Overdue Trends",
                    subtitle = "Tendencias de tareas vencidas",
                    onClick = onOpenOverdueTrends
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun StatMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = AppSurface,
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = AppSecondaryText)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = AppSecondaryText, modifier = Modifier.size(18.dp))
        }
    }
}
