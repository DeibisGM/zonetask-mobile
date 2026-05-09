package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.app.zonetask.ui.screens.spaces.EditSpaceScreen
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
        composable(route = AppDestinations.SPACES) { navBackStackEntry ->
            val snackbarHostState = remember { SnackbarHostState() }
            val successMessage = navBackStackEntry.savedStateHandle
                .getStateFlow<String?>("successMessage", null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(successMessage.value) {
                val msg = successMessage.value
                if (msg != null) {
                    snackbarHostState.showSnackbar(message = msg)
                    navBackStackEntry.savedStateHandle["successMessage"] = null
                }
            }

            ZoneTaskScaffold(
                title             = UserMessages.Screens.SPACES_TITLE,
                showBack          = false,
                onBackClick       = {},
                snackbarHostState = snackbarHostState,
                onAddClick        = { navController.navigate(AppDestinations.CREATE_SPACE) }
            ) { padding ->
                SpacesScreen(
                    snackbarHostState     = snackbarHostState,
                    userId                = 2,
                    modifier              = Modifier.padding(padding),
                    reloadTrigger         = successMessage.value != null,
                    onSpaceClick          = { space ->
                        navController.navigate("${AppDestinations.SPACE_DETAIL_ROUTE}/${space.spaceId}")
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
            val spaceId           = backStackEntry.arguments?.getInt("spaceId") ?: return@composable
            val snackbarHostState = remember { SnackbarHostState() }

            val refreshDetail = backStackEntry.savedStateHandle
                .getStateFlow<Boolean>("refreshDetail", false)
                .collectAsStateWithLifecycle()

            ZoneTaskScaffold(
                title = UserMessages.SpaceDetail.TITLE,
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = detailSnackbarHostState
            ) { padding ->
                SpaceDetailScreen(
                    spaceId         = spaceId,
                    refreshTrigger  = refreshDetail.value,
                    modifier        = Modifier.padding(padding),
                    onEditClick     = { id ->
                        navController.navigate("${AppDestinations.EDIT_SPACE_ROUTE}/$id")
                    },
                    onDeleteSuccess = {
                        backStackEntry.savedStateHandle["successMessage"] = "Espacio eliminado"
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route     = AppDestinations.EDIT_SPACE,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId           = backStackEntry.arguments?.getInt("spaceId") ?: return@composable
            val snackbarHostState = remember { SnackbarHostState() }

            ZoneTaskScaffold(
                title             = "Editar espacio",
                showBack          = true,
                onBackClick       = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            ) { padding ->
                EditSpaceScreen(
                    spaceId  = spaceId,
                    modifier = Modifier.padding(padding),
                    onSaved  = { msg ->
                        navController.getBackStackEntry(AppDestinations.SPACES)
                            .savedStateHandle["successMessage"] = msg
                        navController.getBackStackEntry(AppDestinations.SPACE_DETAIL)
                            .savedStateHandle["refreshDetail"] = true
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

object AppDestinations {
    const val EDIT_SPACE_ROUTE   = "edit_space"

    const val EDIT_SPACE         = "edit_space/{spaceId}"

    const val LOGIN              = "login"
    const val SPACES             = "spaces"
    const val SPACE_DETAIL_ROUTE = "space_detail"
    const val SPACE_DETAIL       = "space_detail/{spaceId}"
    const val CREATE_SPACE       = "create_space"
}
