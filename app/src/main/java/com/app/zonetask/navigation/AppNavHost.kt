package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.spaces.CreateSpaceScreen
import com.app.zonetask.ui.screens.spaces.SpacesScreen
import com.app.zonetask.ui.screens.spaces.SpaceDetailScreen
import com.app.zonetask.ui.screens.spaces.SpacesScreen
import com.app.zonetask.ui.screens.taskcreate.TaskCreateScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.TASK_CREATE,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = AppDestinations.TASK_CREATE) {
            TaskCreateScreen(
                onNavigate = { route ->
                    if (route == AppDestinations.SPACES) {
                        navController.navigate(AppDestinations.SPACES)
                    }
                }
            )
        }

            val successMessage = it.savedStateHandle
                .getStateFlow<String?>("successMessage", null)
                .collectAsStateWithLifecycle()

            ZoneTaskScaffold(
                title             = UserMessages.Screens.SPACES_TITLE,
                showBack          = false,
                onBackClick       = {},
                snackbarHostState = snackbarHostState,
                onAddClick        = { navController.navigate(AppDestinations.CREATE_SPACE) }
            ) { padding ->
                SpacesScreen(
                    snackbarHostState = snackbarHostState,
                    userId = 3,
                    modifier = Modifier.padding(padding),
                    onSpaceClick = { space ->
                        navController.navigate(
                            "${AppDestinations.SPACE_DETAIL_ROUTE}/${space.spaceId}"
                        )
                    }
                )
            }
        }

        composable(route = AppDestinations.CREATE_SPACE) {
            val snackbarHostState = remember { SnackbarHostState() }

            ZoneTaskScaffold(
                title             = "Crear un nuevo espacio",
                showBack          = true,
                onBackClick       = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            ) { padding ->
                CreateSpaceScreen(
                    ownerId  = 2,
                    modifier = Modifier.padding(padding),
                    onSaved  = { successMessage ->
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
            val detailSnackbarHostState = remember { SnackbarHostState() }

            ZoneTaskScaffold(
                title = UserMessages.SpaceDetail.TITLE,
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = detailSnackbarHostState
            ) { padding ->
                SpaceDetailScreen(
                    spaceId = spaceId,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

object AppDestinations {
    const val LOGIN              = "login"
    const val SPACES             = "spaces"
    const val SPACE_DETAIL_ROUTE = "space_detail"
    const val SPACE_DETAIL       = "space_detail/{spaceId}"
    const val CREATE_SPACE       = "create_space"
}
