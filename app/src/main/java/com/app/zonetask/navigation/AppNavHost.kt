package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.zonetask.ui.screens.taskcreate.TaskCreateScreen
import com.app.zonetask.ui.screens.spaces.SpacesScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val startDestination = AppDestinations.TASK_CREATE

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = AppDestinations.TASK_CREATE) {
            TaskCreateScreen(
                onNavigate = { route ->
                    if (route == "spaces") {
                        navController.navigate(AppDestinations.SPACES)
                    }
                    // Add other routes as needed
                }
            )
        }
        
        composable(route = AppDestinations.SPACES) {
            SpacesScreen(
                snackbarHostState = snackbarHostState,
                userId = 1, // Placeholder ID
                onNavigate = { route ->
                    if (route == "task_create") {
                        navController.navigate(AppDestinations.TASK_CREATE)
                    }
                    // Add other routes as needed
                }
            )
        }
    }
}
