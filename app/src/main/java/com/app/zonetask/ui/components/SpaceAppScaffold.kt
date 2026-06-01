package com.app.zonetask.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneTaskScaffold(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    currentDestination: NavDestination = NavDestination.SETTINGS,
    onDestinationSelected: (NavDestination) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    onAddClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = UserMessages.Accessibility.BACK,
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    actions = {
                        if (onAddClick != null) {
                            IconButton(onClick = onAddClick) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppTopBar
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar && !showBack) {
                AppBottomNavBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = onDestinationSelected
                )
            } else {
                bottomBar()
            }
        },
        snackbarHost = {
            snackbarHostState?.let { host ->
                SnackbarHost(hostState = host) { data ->
                    Snackbar(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        snackbarData = data,
                        containerColor = AppPrimary,
                        contentColor = AppOnPrimary
                    )
                }
            }
        }
    ) { padding ->
        content(padding)
    }
}
