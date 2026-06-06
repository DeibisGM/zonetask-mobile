package com.app.zonetask.ui.screens.plan

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.FloorPlan
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

/**
 * Lists the floor plans for a space and lets the user open an existing one
 * or create a new blank plan.
 */
@Composable
fun PlanListScreen(
    spaceId:       Int,
    modifier:      Modifier = Modifier,
    reloadTrigger: Boolean  = false,
    onReloadHandled: () -> Unit = {},
    onOpenPlan:    (planId: Int) -> Unit = {},
    onCreatePlan:  () -> Unit = {},
    viewModel: PlanListViewModel = viewModel(
        factory = PlanListViewModelFactory(
            repository = AppContainer.floorPlanRepository,
            spaceId    = spaceId
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger) {
            viewModel.loadPlans()
            onReloadHandled()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppPrimary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = "Loading plans...",
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            state.errorBanner != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text      = state.errorBanner!!,
                            color     = AppSecondaryText,
                            style     = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = viewModel::loadPlans) {
                            Text("Retry", color = AppPrimary)
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier        = Modifier.weight(1f),
                    contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.plans.isEmpty()) {
                        item {
                            Column(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Spacer(Modifier.height(40.dp))
                                Icon(
                                    imageVector        = Icons.Outlined.GridView,
                                    contentDescription = null,
                                    tint               = AppSecondaryText,
                                    modifier           = Modifier.size(48.dp)
                                )
                                Text(
                                    text      = "No floor plans in this space yet.",
                                    color     = AppSecondaryText,
                                    style     = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text      = "Create a blank plan to get started.",
                                    color     = AppSecondaryText,
                                    style     = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text  = "Space plans",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(state.plans, key = { it.planId }) { plan ->
                            PlanCard(plan = plan, onClick = { onOpenPlan(plan.planId) })
                        }
                    }

                    item { HorizontalDivider(color = AppBorder, modifier = Modifier.padding(vertical = 4.dp)) }
                }

                // Fixed bottom bar with "New plan" button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick  = onCreatePlan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Add,
                            contentDescription = null,
                            tint               = AppOnPrimary,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "New plan",
                            color      = AppOnPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style      = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: FloorPlan, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(16.dp),
        color   = AppSurface,
        border  = BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.GridView,
                contentDescription = null,
                tint               = AppPrimary,
                modifier           = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = plan.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = "${plan.canvasWidth.toInt()} × ${plan.canvasHeight.toInt()} px",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppSecondaryText
                )
            }
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint               = AppSecondaryText,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}
