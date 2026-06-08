package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.UserMessages
import com.app.zonetask.navigation.plans.PlansDestinations
import com.app.zonetask.navigation.plans.PlansNavActions
import com.app.zonetask.navigation.plans.PlansNavKeys
import com.app.zonetask.navigation.plans.plansNavGraph
import com.app.zonetask.navigation.spaces.SpacesDestinations
import com.app.zonetask.navigation.spaces.SpacesNavActions
import com.app.zonetask.navigation.spaces.SpacesNavKeys
import com.app.zonetask.navigation.spaces.spacesNavGraph
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.home.HomeScreen
import com.app.zonetask.ui.screens.login.LoginScreen
import com.app.zonetask.ui.screens.passwordreset.ForgotPasswordScreen
import com.app.zonetask.ui.screens.profile.ProfileEditScreen
import com.app.zonetask.ui.screens.profile.ProfileScreen
import com.app.zonetask.ui.screens.register.RegisterScreen
import com.app.zonetask.ui.screens.taskcreate.TaskCreateScreen
import com.app.zonetask.ui.screens.taskdetail.TaskDetailScreen
import com.app.zonetask.ui.screens.tasks.TasksScreen

private const val AUTH_NOTICE_KEY = "authNotice"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentUserId by rememberSaveable {
        mutableIntStateOf(AuthSessionStore.currentUser?.userId ?: 0)
    }
    var currentSpaceId by rememberSaveable { mutableIntStateOf(0) }
    val startDestination = if (currentUserId > 0) {
        AppDestinations.homeRoute(0)
    } else {
        AppDestinations.LOGIN
    }

    val performLogout = {
        AuthSessionStore.clear()
        currentUserId = 0
        currentSpaceId = 0
        navController.navigate(AppDestinations.LOGIN) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val onTabSelected: (NavDestination) -> Unit = { destination ->
        navigateToTab(navController, destination, currentUserId, currentSpaceId)
    }

    val spacesNavActions = rememberSpacesNavActions(navController, currentUserId)
    val plansNavActions  = rememberPlansNavActions(navController)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {

        composable(route = AppDestinations.LOGIN) { backStackEntry ->
            val registrationNotice by backStackEntry.savedStateHandle
                .getStateFlow<String?>(AUTH_NOTICE_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(registrationNotice) {
                if (!registrationNotice.isNullOrBlank()) {
                    backStackEntry.savedStateHandle[AUTH_NOTICE_KEY] = null
                }
            }

            LoginScreen(
                onLoginSuccess = { userId ->
                    currentUserId = userId
                    navController.navigate(AppDestinations.homeRoute(0)) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onCreateAccount = {
                    navController.navigate(AppDestinations.REGISTER)
                },
                onForgotPassword = {
                    navController.navigate(AppDestinations.FORGOT_PASSWORD)
                },
                authNotice = registrationNotice
            )
        }

        composable(route = AppDestinations.PROFILE) { backStackEntry ->
            val profileChanged by backStackEntry.savedStateHandle
                .getStateFlow("profileChanged", false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(profileChanged) {
                if (profileChanged) {
                    backStackEntry.savedStateHandle["profileChanged"] = false
                }
            }

            ProfileScreen(
                userId = currentUserId,
                onTabSelected = onTabSelected,
                onEditProfile = {
                    navController.navigate(AppDestinations.PROFILE_EDIT)
                },
                onLogout = performLogout,
                refreshTrigger = profileChanged
            )
        }

        composable(route = AppDestinations.PROFILE_EDIT) {
            ProfileEditScreen(
                userId = currentUserId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("profileChanged", true)
                }
            )
        }

        composable(route = AppDestinations.REGISTER) {
            RegisterScreen(
                onBackToLogin = { message ->
                    // The registration flow stores a one-time message for the login screen before returning.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            AUTH_NOTICE_KEY,
                            message ?: UserMessages.Login.REGISTRATION_NOTICE
                        )
                    navController.popBackStack(AppDestinations.LOGIN, inclusive = false)
                }
            )
        }

        composable(route = AppDestinations.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = {
                    navController.popBackStack(AppDestinations.LOGIN, inclusive = false)
                }
            )
        }

        composable(
            route = AppDestinations.HOME,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 0

            ZoneTaskScaffold(
                title = "",
                showBack = false,
                onBackClick = {},
                showTopBar = false,
                currentDestination = NavDestination.HOME,
                onDestinationSelected = onTabSelected,
                snackbarHostState = snackbarHostState
            ) { padding ->
                HomeScreen(
                    spaceId = spaceId,
                    userId = currentUserId,
                    modifier = Modifier.padding(padding),
                    onNavigateToCreateSpace = {
                        navController.navigate(SpacesDestinations.CREATE)
                    },
                    onNavigateToCreateTask = {
                        val sid = if (currentSpaceId > 0) currentSpaceId else spaceId
                        if (sid > 0) {
                            navController.navigate(AppDestinations.taskCreateRoute(sid))
                        }
                    },
                    onNavigateToManageSpaces = {
                        navController.navigate(SpacesDestinations.list(currentUserId))
                    },
                    onNavigateToTaskDetail = { sid, taskId ->
                        navController.navigate(AppDestinations.taskDetailRoute(sid, taskId))
                    },
                    onSpaceChanged = { newSpaceId ->
                        currentSpaceId = newSpaceId
                        navController.navigate(AppDestinations.homeRoute(newSpaceId)) {
                            popUpTo(AppDestinations.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = AppDestinations.TASK_DETAIL,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.IntType },
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
            val taskId  = backStackEntry.arguments?.getInt("taskId") ?: return@composable

            TaskDetailScreen(
                spaceId = spaceId,
                taskId = taskId,
                modifier = Modifier.fillMaxSize(),
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(AppDestinations.taskEditRoute(spaceId, id))
                },
                onDeleted = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("taskChanged", true)
                    navController.popBackStack()
                }
            )
        }

        spacesNavGraph(
            currentUserId = currentUserId,
            rootSnackbarHostState = snackbarHostState,
            actions = spacesNavActions,
            onTabSelected = onTabSelected
        )

        plansNavGraph(
            actions = plansNavActions,
            rootSnackbarHostState = snackbarHostState
        )

        tasksGraph(
            navController = navController,
            currentUserId = currentUserId,
            snackbarHostState = snackbarHostState,
            onTabSelected = onTabSelected,
            onLogout = performLogout
        )
    }
}

private fun navigateToTab(
    navController: NavHostController,
    destination: NavDestination,
    userId: Int,
    currentSpaceId: Int
) {
    if (userId <= 0) return
    val route = when (destination) {
        NavDestination.HOME -> {
            val sid = if (currentSpaceId > 0) currentSpaceId else 0
            AppDestinations.homeRoute(sid)
        }
        NavDestination.TASKS   -> AppDestinations.tasksRoute(userId)
        NavDestination.PROFILE -> AppDestinations.PROFILE
        NavDestination.SETTINGS -> SpacesDestinations.list(userId)
        else -> return
    }
    navController.navigate(route) { launchSingleTop = true }
}

@Composable
private fun rememberSpacesNavActions(
    navController: NavHostController,
    currentUserId: Int
): SpacesNavActions = remember(navController, currentUserId) {
    SpacesNavActions(
        onOpenDetail         = { id -> navController.navigate(SpacesDestinations.detail(id)) },
        onOpenCreate         = { navController.navigate(SpacesDestinations.CREATE) },
        onOpenEdit           = { id -> navController.navigate(SpacesDestinations.edit(id)) },
        onOpenPermissions    = { id -> navController.navigate(SpacesDestinations.permissions(id)) },
        onCreateTaskForSpace = { spaceId ->
            navController.navigate(AppDestinations.taskCreateRoute(spaceId))
        },
        onOpenPlans = { spaceId ->
            navController.navigate(PlansDestinations.list(spaceId))
        },
        onBack = { navController.popBackStack() },

        onSpaceCreated = { message ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(SpacesNavKeys.SUCCESS_MESSAGE, message)
            navController.popBackStack()
        },
        onSpaceEdited = { message ->
            navController.getBackStackEntry(SpacesDestinations.LIST)
                .savedStateHandle[SpacesNavKeys.SUCCESS_MESSAGE] = message
            navController.getBackStackEntry(SpacesDestinations.DETAIL)
                .savedStateHandle[SpacesNavKeys.REFRESH_DETAIL] = true
            navController.popBackStack()
        },
        onSpaceDeleted = { message ->
            navController.getBackStackEntry(SpacesDestinations.LIST)
                .savedStateHandle[SpacesNavKeys.SUCCESS_MESSAGE] = message
            navController.popBackStack(SpacesDestinations.LIST, inclusive = false)
        }
    )
}

@Composable
private fun rememberPlansNavActions(
    navController: NavHostController
): PlansNavActions = remember(navController) {
    PlansNavActions(
        onOpenList   = { spaceId -> navController.navigate(PlansDestinations.list(spaceId)) },
        onCreatePlan = { spaceId -> navController.navigate(PlansDestinations.newPlan(spaceId)) },
        onOpenPlan   = { spaceId, planId -> navController.navigate(PlansDestinations.editor(spaceId, planId)) },
        onPlanSaved  = { message ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(PlansNavKeys.PLAN_SAVED_MESSAGE, message)
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(PlansNavKeys.RELOAD_PLANS, true)
            navController.popBackStack()
        },
        onBack = { navController.popBackStack() }
    )
}

private fun androidx.navigation.NavGraphBuilder.tasksGraph(
    navController: NavHostController,
    currentUserId: Int,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (NavDestination) -> Unit,
    onLogout: () -> Unit
) {
    composable(route = AppDestinations.TASK_CREATE) {
            TaskCreateScreen(
                initialSpaceId = 1,
                initialCreatedBy = currentUserId,
                onNavigate = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
                onLogout = onLogout,
                onClose = { navController.popBackStack() }
            )
        }

    composable(
        route = AppDestinations.TASK_CREATE_WITH_SPACE,
        arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
            TaskCreateScreen(
                initialSpaceId = spaceId,
                initialCreatedBy = currentUserId,
                onNavigate = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
                onLogout = onLogout,
                onClose = { navController.popBackStack() }
            )
        }

    composable(
        route = AppDestinations.TASK_EDIT_WITH_SPACE,
        arguments = listOf(
            navArgument("spaceId") { type = NavType.IntType },
            navArgument("taskId") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
        val taskId  = backStackEntry.arguments?.getInt("taskId") ?: return@composable
        TaskCreateScreen(
            initialSpaceId = spaceId,
            initialCreatedBy = currentUserId,
            taskId = taskId,
            onNavigate = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onLogout = onLogout,
            onClose = { navController.popBackStack() }
        )
    }

    composable(
        route = AppDestinations.TASKS,
        arguments = listOf(navArgument("userId") { type = NavType.IntType })
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getInt("userId") ?: currentUserId
        val taskChanged by backStackEntry.savedStateHandle
            .getStateFlow("taskChanged", false)
            .collectAsStateWithLifecycle()

        ZoneTaskScaffold(
            title = UserMessages.Screens.TASKS_TITLE,
            showBack = false,
            onBackClick = {},
            showTopBar = false,
            currentDestination = NavDestination.TASKS,
            onDestinationSelected = onTabSelected,
            snackbarHostState = snackbarHostState
        ) { padding ->
            TasksScreen(
                userId = userId,
                modifier = Modifier.padding(padding),
                reloadTrigger = taskChanged,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle["taskChanged"] = false
                },
                onCreateTask = { spaceId ->
                    navController.navigate(AppDestinations.taskCreateRoute(spaceId))
                },
                onEditTask = { spaceId, taskId ->
                    navController.navigate(AppDestinations.taskEditRoute(spaceId, taskId))
                },
                onTaskClick = { spaceId, taskId ->
                    navController.navigate(AppDestinations.taskDetailRoute(spaceId, taskId))
                }
            )
        }
    }
}

private fun navigateToSpacesFromTasks(
    navController: NavHostController,
    route: String,
    currentUserId: Int
) {
    if ((route == "spaces" || route.startsWith("spaces/")) && currentUserId > 0) {
        navController.navigate(SpacesDestinations.list(currentUserId)) {
            launchSingleTop = true
        }
        return
    }

    if (route == "profile" && currentUserId > 0) {
        navController.navigate(AppDestinations.PROFILE) {
            launchSingleTop = true
        }
        return
    }
}
