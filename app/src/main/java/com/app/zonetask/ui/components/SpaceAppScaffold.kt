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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneTaskScaffold(
    title: String,
    showBack: Boolean,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    onAddClick: (() -> Unit)? = null,
    showBottomBar: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    var selectedDestination by rememberSaveable { mutableStateOf(NavDestination.SPACES) }

    Scaffold(
        modifier       = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = UserMessages.Accessibility.BACK,
                                tint               = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    if (onAddClick != null) {
                        IconButton(onClick = onAddClick) {
                            Icon(
                                imageVector        = Icons.Default.Add,
                                contentDescription = "Agregar",
                                tint               = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (showBottomBar && !showBack) {
                AppBottomNavBar(
                    currentDestination    = selectedDestination,
                    onDestinationSelected = { selectedDestination = it }
                )
            } else {
                bottomBar()
            }
        },
        snackbarHost = {
            snackbarHostState?.let { host ->
                SnackbarHost(hostState = host) { data ->
                    Snackbar(
                        modifier       = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        snackbarData   = data,
                        containerColor = AppPrimary,
                        contentColor   = AppOnPrimary
                    )
                }
            }
        },
        content = content
    )
}