package com.app.zonetask.navigation.plans

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.plan.PlanEditorScreen
import com.app.zonetask.ui.screens.plan.PlanListScreen
import com.app.zonetask.ui.screens.plan.TemplateSelectScreen
import com.app.zonetask.ui.theme.AppPrimary

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
            title         = "Floors",
            showBack      = true,
            onBackClick   = actions.onBack,
            snackbarHostState = listSnackbar,
            onAddClick = { actions.onCreatePlan(spaceId) }
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

    // ── Template selection ───────────────────────────────────────────────────
    composable(
        route     = PlansDestinations.TEMPLATE_SELECT,
        arguments = listOf(navArgument(PlansDestinations.ARG_SPACE_ID) { type = NavType.IntType })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(PlansDestinations.ARG_SPACE_ID) ?: return@composable

        ZoneTaskScaffold(
            title         = "Elegir plantilla",
            showBack      = true,
            onBackClick   = actions.onBack,
            snackbarHostState = rootSnackbarHostState
        ) { padding ->
            TemplateSelectScreen(
                modifier         = Modifier.padding(padding),
                onSelectTemplate = { templateId -> actions.onApplyTemplate(spaceId, templateId) },
                onSelectBlank    = { actions.onApplyTemplate(spaceId, null) }
            )
        }
    }

    // ── Create new plan (optionally from a template) ─────────────────────────
    composable(
        route     = PlansDestinations.NEW,
        arguments = listOf(
            navArgument(PlansDestinations.ARG_SPACE_ID) { type = NavType.IntType },
            // NavType.IntType cannot be nullable, so a non-template plan uses the -1 sentinel.
            navArgument(PlansDestinations.ARG_TEMPLATE_ID) {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { backStackEntry ->
        var saveFloorAction by remember { mutableStateOf<(() -> Unit)?>(null) }
        var canSaveFloor by remember { mutableStateOf(false) }
        val spaceId    = backStackEntry.arguments?.getInt(PlansDestinations.ARG_SPACE_ID) ?: return@composable
        val templateId = backStackEntry.arguments?.getInt(PlansDestinations.ARG_TEMPLATE_ID)?.takeIf { it >= 0 }

        ZoneTaskScaffold(
            title         = "Create floor",
            showBack      = true,
            onBackClick   = actions.onBack,
            topBarActions = {
                if (canSaveFloor) IconButton(onClick = { saveFloorAction?.invoke() }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save", tint = AppPrimary)
                }
            },
            snackbarHostState = rootSnackbarHostState
        ) { padding ->
            PlanEditorScreen(
                spaceId    = spaceId,
                planId     = null,
                templateId = templateId,
                modifier   = Modifier.padding(padding),
                onSaved    = { message -> actions.onPlanSaved(message) },
                onBack     = actions.onBack,
                onSaveActionChanged = { action, enabled -> saveFloorAction = action; canSaveFloor = enabled }
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
        var saveFloorAction by remember { mutableStateOf<(() -> Unit)?>(null) }
        var canSaveFloor by remember { mutableStateOf(false) }
        val spaceId = backStackEntry.arguments?.getInt(PlansDestinations.ARG_SPACE_ID) ?: return@composable
        val planId  = backStackEntry.arguments?.getInt(PlansDestinations.ARG_PLAN_ID)  ?: return@composable

        ZoneTaskScaffold(
            title         = "Edit floor",
            showBack      = true,
            onBackClick   = actions.onBack,
            topBarActions = {
                if (canSaveFloor) IconButton(onClick = { saveFloorAction?.invoke() }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save", tint = AppPrimary)
                }
            },
            snackbarHostState = rootSnackbarHostState
        ) { padding ->
            PlanEditorScreen(
                spaceId  = spaceId,
                planId   = planId,
                modifier = Modifier.padding(padding),
                onSaved  = { message -> actions.onPlanSaved(message) },
                onBack   = actions.onBack,
                onSaveActionChanged = { action, enabled -> saveFloorAction = action; canSaveFloor = enabled }
            )
        }
    }
}
