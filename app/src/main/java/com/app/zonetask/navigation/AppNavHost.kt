package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.login.LoginScreen
import com.app.zonetask.ui.screens.spaces.CreateSpaceScreen
import com.app.zonetask.ui.screens.spaces.EditSpaceScreen
import com.app.zonetask.ui.screens.spaces.SpaceDetailScreen
import com.app.zonetask.ui.screens.spaces.SpacePermissionsScreen
import com.app.zonetask.ui.screens.spaces.SpacesScreen
import com.app.zonetask.ui.screens.taskcreate.TaskCreateScreen
import com.app.zonetask.ui.screens.tasks.TasksScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentUserId by rememberSaveable { mutableIntStateOf(0) }

    val navigateToTab: (NavDestination) -> Unit = { destination ->
        val targetUserId = currentUserId
        if (targetUserId > 0) {
            val route = when (destination) {
                NavDestination.SPACES -> AppDestinations.spacesRoute(targetUserId)
                NavDestination.TASKS -> AppDestinations.tasksRoute(targetUserId)
                else -> null
            }

            route?.let {
                navController.navigate(it) { launchSingleTop = true }
            }
        }
    }

    val navigateToSpaces: (String) -> Unit = { route ->
        if (route == "spaces" || route.startsWith("spaces/")) {
            val targetUserId = currentUserId
            if (targetUserId > 0) {
                navController.navigate(AppDestinations.spacesRoute(targetUserId)) {
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = { userId ->
                    currentUserId = userId
                    navController.navigate(AppDestinations.spacesRoute(userId)) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(route = AppDestinations.TASK_CREATE) {
            TaskCreateScreen(
                initialSpaceId = 1,
                initialCreatedBy = currentUserId,
                onNavigate = navigateToSpaces,
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
                onNavigate = navigateToSpaces,
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
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable
            TaskCreateScreen(
                initialSpaceId = spaceId,
                initialCreatedBy = currentUserId,
                taskId = taskId,
                onNavigate = navigateToSpaces,
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestinations.SPACES,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: currentUserId
            val successMessage by backStackEntry.savedStateHandle
                .getStateFlow<String?>("successMessage", null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(successMessage) {
                successMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    backStackEntry.savedStateHandle["successMessage"] = null
                }
            }

            ZoneTaskScaffold(
                title = UserMessages.Screens.SPACES_TITLE,
                showBack = false,
                onBackClick = {},
                currentDestination = NavDestination.SPACES,
                onDestinationSelected = navigateToTab,
                snackbarHostState = snackbarHostState,
                onAddClick = { navController.navigate(AppDestinations.CREATE_SPACE) }
            ) { padding ->
                SpacesScreen(
                    snackbarHostState = snackbarHostState,
                    userId = userId,
                    modifier = Modifier.padding(padding),
                    reloadTrigger = successMessage != null,
                    onSuccessMessageShown = {
                        backStackEntry.savedStateHandle["successMessage"] = null
                    },
                    onSpaceClick = { space ->
                        navController.navigate("${AppDestinations.SPACE_DETAIL_ROUTE}/${space.spaceId}")
                    }
                )
            }
        }

        composable(
            route = AppDestinations.TASKS,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: currentUserId

            ZoneTaskScaffold(
                title = UserMessages.Screens.TASKS_TITLE,
                showBack = false,
                onBackClick = {},
                showTopBar = false,
                currentDestination = NavDestination.TASKS,
                onDestinationSelected = navigateToTab,
                snackbarHostState = snackbarHostState
            ) { padding ->
                TasksScreen(
                    userId = userId,
                    modifier = Modifier.padding(padding),
                    onCreateTask = { spaceId ->
                        navController.navigate(AppDestinations.taskCreateRoute(spaceId))
                    },
                    onEditTask = { spaceId, taskId ->
                        navController.navigate(AppDestinations.taskEditRoute(spaceId, taskId))
                    }
                )
            }
        }

        composable(route = AppDestinations.CREATE_SPACE) {
            ZoneTaskScaffold(
                title = "Crear un nuevo espacio",
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            ) { padding ->
                CreateSpaceScreen(
                    ownerId = currentUserId,
                    modifier = Modifier.padding(padding),
                    onSaved = { successMessage ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("successMessage", successMessage)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = AppDestinations.SPACE_DETAIL,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: return@composable

            ZoneTaskScaffold(
                title = UserMessages.SpaceDetail.TITLE,
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = SnackbarHostState()
            ) { padding ->
                SpaceDetailScreen(
                    spaceId = spaceId,
                    userId = currentUserId,
                    modifier = Modifier.padding(padding),
                    onNavigateToPermissions = { id ->
                        navController.navigate("${AppDestinations.SPACE_PERMISSIONS_ROUTE}/$id")
                    },
                    onCreateTaskClick = {
                        navController.navigate(AppDestinations.taskCreateRoute(spaceId))
                    },
                    onEditClick = { id ->
                        navController.navigate("${AppDestinations.EDIT_SPACE_ROUTE}/$id")
                    }
                )
            }
        }

        composable(
            route = AppDestinations.EDIT_SPACE,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: return@composable

            ZoneTaskScaffold(
                title = "Editar espacio",
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = SnackbarHostState()
            ) { padding ->
                EditSpaceScreen(
                    spaceId = spaceId,
                    modifier = Modifier.padding(padding),
                    onSaved = { msg ->
                        navController.getBackStackEntry(AppDestinations.SPACES)
                            .savedStateHandle["successMessage"] = msg
                        navController.getBackStackEntry(AppDestinations.SPACE_DETAIL)
                            .savedStateHandle["refreshDetail"] = true
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = AppDestinations.SPACE_PERMISSIONS,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: return@composable
            val permissionsSnackbarHostState = remember { SnackbarHostState() }

            ZoneTaskScaffold(
                title = UserMessages.SpacePermissions.TITLE,
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = permissionsSnackbarHostState
            ) { padding ->
                SpacePermissionsScreen(
                    spaceId = spaceId,
                    userId = currentUserId,
                    snackbarHostState = permissionsSnackbarHostState,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
