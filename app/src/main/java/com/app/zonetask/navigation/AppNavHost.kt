package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.spaces.SpacesScreen
import com.app.zonetask.ui.screens.spaces.SpaceDetailScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = AppDestinations.SPACES,
        modifier         = Modifier.fillMaxSize()
    ) {
        composable(route = AppDestinations.SPACES) {
            val snackbarHostState = remember { SnackbarHostState() }

            ZoneTaskScaffold(
                title             = UserMessages.Screens.SPACES_TITLE,
                showBack          = false,
                onBackClick       = {},
                snackbarHostState = snackbarHostState
            ) { padding ->
                SpacesScreen(
                    snackbarHostState = snackbarHostState,
                    userId           = 3,
                    modifier         = Modifier.padding(padding),
                    onSpaceClick     = { space ->
                        navController.navigate(
                            "${AppDestinations.SPACE_DETAIL_ROUTE}/${space.spaceId}"
                        )
                    }
                )
            }
        }

        composable(
            route     = AppDestinations.SPACE_DETAIL,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: return@composable
            val snackbarHostState = remember { SnackbarHostState() }

            ZoneTaskScaffold(
                title             = UserMessages.SpaceDetail.TITLE,
                showBack          = true,
                onBackClick       = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            ) { padding ->
                SpaceDetailScreen(
                    spaceId  = spaceId,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

object AppDestinations {
    const val LOGIN               = "login"
    const val SPACES              = "spaces"
    const val SPACE_DETAIL_ROUTE  = "space_detail"
    const val SPACE_DETAIL        = "space_detail/{spaceId}"
}