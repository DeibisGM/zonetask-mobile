package com.app.zonetask.navigation.plans

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.plan.PlanEditorScreen
import com.app.zonetask.ui.screens.plan.PlanListScreen

fun NavGraphBuilder.plansNavGraph(
    actions:  PlansNavActions,
    rootSnackbarHostState: SnackbarHostState
) {
    // ── Plan list ────────────────────────────────────────────────────────────
    composable(
        route     = PlansDestinations.LIST,
        arguments = listOf(navArgument(PlansDestinations.ARG_SPACE_ID) { type = NavType.IntType })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(PlansDestinations.ARG_SPACE_ID) ?: return@composable

        val reloadPlans by backStackEntry.savedStateHandle
            .getStateFlow(PlansNavKeys.RELOAD_PLANS, false)
            .collectAsStateWithLifecycle()

        val savedMessage by backStackEntry.savedStateHandle
            .getStateFlow<String?>(PlansNavKeys.PLAN_SAVED_MESSAGE, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(savedMessage) {
            savedMessage?.let { message ->
                rootSnackbarHostState.showSnackbar(message)
                backStackEntry.savedStateHandle[PlansNavKeys.PLAN_SAVED_MESSAGE] = null
            }
        }

        val listSnackbar = remember { SnackbarHostState() }

        ZoneTaskScaffold(
            title         = "Floor plans",
            showBack      = true,
            onBackClick   = actions.onBack,
            snackbarHostState = listSnackbar
        ) { padding ->
            PlanListScreen(
                spaceId         = spaceId,
                modifier        = Modifier.padding(padding),
                reloadTrigger   = reloadPlans,
                onReloadHandled = {
                    backStackEntry.savedStateHandle[PlansNavKeys.RELOAD_PLANS] = false
                },
                onOpenPlan   = { planId -> actions.onOpenPlan(spaceId, planId) },
                onCreatePlan = { actions.onCreatePlan(spaceId) }
            )
        }
    }

    // ── Create new plan ──────────────────────────────────────────────────────
    composable(
        route     = PlansDestinations.NEW,
        arguments = listOf(navArgument(PlansDestinations.ARG_SPACE_ID) { type = NavType.IntType })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(PlansDestinations.ARG_SPACE_ID) ?: return@composable

        ZoneTaskScaffold(
            title         = "New plan",
            showBack      = true,
            onBackClick   = actions.onBack,
            snackbarHostState = rootSnackbarHostState
        ) { padding ->
            PlanEditorScreen(
                spaceId  = spaceId,
                planId   = null,
                modifier = Modifier.padding(padding),
                onSaved  = { message -> actions.onPlanSaved(message) },
                onBack   = actions.onBack
            )
        }
    }

    // ── Edit existing plan ───────────────────────────────────────────────────
    composable(
        route     = PlansDestinations.EDITOR,
        arguments = listOf(
            navArgument(PlansDestinations.ARG_SPACE_ID) { type = NavType.IntType },
            navArgument(PlansDestinations.ARG_PLAN_ID)  { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt(PlansDestinations.ARG_SPACE_ID) ?: return@composable
        val planId  = backStackEntry.arguments?.getInt(PlansDestinations.ARG_PLAN_ID)  ?: return@composable

        ZoneTaskScaffold(
            title         = "Edit plan",
            showBack      = true,
            onBackClick   = actions.onBack,
            snackbarHostState = rootSnackbarHostState
        ) { padding ->
            PlanEditorScreen(
                spaceId  = spaceId,
                planId   = planId,
                modifier = Modifier.padding(padding),
                onSaved  = { message -> actions.onPlanSaved(message) },
                onBack   = actions.onBack
            )
        }
    }
}
