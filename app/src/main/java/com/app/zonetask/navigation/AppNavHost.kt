package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
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
import com.app.zonetask.core.UserMessages
import com.app.zonetask.navigation.spaces.SpacesDestinations
import com.app.zonetask.navigation.spaces.SpacesNavActions
import com.app.zonetask.navigation.spaces.SpacesNavKeys
import com.app.zonetask.navigation.spaces.spacesNavGraph
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.invitations.MyInvitationsScreen
import com.app.zonetask.ui.screens.login.LoginScreen
import com.app.zonetask.ui.screens.taskcreate.TaskCreateScreen
import com.app.zonetask.ui.screens.tasks.TasksScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentUserId by rememberSaveable { mutableIntStateOf(0) }
    var currentUserEmail by rememberSaveable { mutableStateOf("") }

    val onTabSelected: (NavDestination) -> Unit = { destination ->
        navigateToTab(navController, destination, currentUserId)
    }

    val spacesNavActions = rememberSpacesNavActions(navController, currentUserId)

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN,
        modifier = Modifier.fillMaxSize()
    ) {

        composable(route = AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = { userId, email ->
                    currentUserId    = userId
                    currentUserEmail = email
                    navController.navigate(SpacesDestinations.list(userId)) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestinations.MY_INVITATIONS,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val invitationsSnackbarHostState = remember { SnackbarHostState() }
            ZoneTaskScaffold(
                title = UserMessages.Invitations.MY_TITLE,
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = invitationsSnackbarHostState
            ) { padding ->
                MyInvitationsScreen(
                    userId            = currentUserId,
                    email             = currentUserEmail,
                    snackbarHostState = invitationsSnackbarHostState,
                    modifier          = Modifier.padding(padding)
                )
            }
        }

        spacesNavGraph(
            currentUserId = currentUserId,
            rootSnackbarHostState = snackbarHostState,
            actions = spacesNavActions,
            onTabSelected = onTabSelected
        )

        tasksGraph(
            navController = navController,
            currentUserId = currentUserId,
            snackbarHostState = snackbarHostState,
            onTabSelected = onTabSelected
        )
    }
}

private fun navigateToTab(
    navController: NavHostController,
    destination: NavDestination,
    userId: Int
) {
    if (userId <= 0) return
    val route = when (destination) {
        NavDestination.SPACES -> SpacesDestinations.list(userId)
        NavDestination.TASKS  -> AppDestinations.tasksRoute(userId)
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
        onOpenInvite         = { id -> navController.navigate(SpacesDestinations.invite(id)) },
        onCreateTaskForSpace = { spaceId ->
            navController.navigate(AppDestinations.taskCreateRoute(spaceId))
        },
        onBack = { navController.popBackStack() },
        onOpenInvitations = { navController.navigate(AppDestinations.myInvitationsRoute(currentUserId)) },

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

private fun androidx.navigation.NavGraphBuilder.tasksGraph(
    navController: NavHostController,
    currentUserId: Int,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (NavDestination) -> Unit
) {
    composable(route = AppDestinations.TASK_CREATE) {
        TaskCreateScreen(
            initialSpaceId   = 1,
            initialCreatedBy = currentUserId,
            onNavigate       = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onClose          = { navController.popBackStack() }
        )
    }

    composable(
        route = AppDestinations.TASK_CREATE_WITH_SPACE,
        arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
        TaskCreateScreen(
            initialSpaceId   = spaceId,
            initialCreatedBy = currentUserId,
            onNavigate       = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onClose          = { navController.popBackStack() }
        )
    }

    composable(
        route = AppDestinations.TASK_EDIT_WITH_SPACE,
        arguments = listOf(
            navArgument("spaceId") { type = NavType.IntType },
            navArgument("taskId")  { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
        val taskId  = backStackEntry.arguments?.getInt("taskId") ?: return@composable
        TaskCreateScreen(
            initialSpaceId   = spaceId,
            initialCreatedBy = currentUserId,
            taskId           = taskId,
            onNavigate       = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onClose          = { navController.popBackStack() }
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
            title                = UserMessages.Screens.TASKS_TITLE,
            showBack             = false,
            onBackClick          = {},
            showTopBar           = false,
            currentDestination   = NavDestination.TASKS,
            onDestinationSelected = onTabSelected,
            snackbarHostState    = snackbarHostState
        ) { padding ->
            TasksScreen(
                userId        = userId,
                modifier      = Modifier.padding(padding),
                reloadTrigger = taskChanged,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle["taskChanged"] = false
                },
                onCreateTask = { spaceId ->
                    navController.navigate(AppDestinations.taskCreateRoute(spaceId))
                },
                onEditTask = { spaceId, taskId ->
                    navController.navigate(AppDestinations.taskEditRoute(spaceId, taskId))
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
    }
}