package com.app.zonetask.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary

enum class NavDestination(
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean
) {
    HOME(label = "Home", icon = Icons.Outlined.Home, enabled = false),
    SPACES(label = "Spaces", icon = Icons.Outlined.SpaceDashboard, enabled = true),
    PROFILE(label = "Profile", icon = Icons.Outlined.Person, enabled = false),
    TASKS(label = "Tasks", icon = Icons.Outlined.TaskAlt, enabled = false)
}

@Composable
fun AppBottomNavBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0A0A0A),
        tonalElevation = 0.dp
    ) {
        NavDestination.entries.forEach { destination ->
            val selected = destination == currentDestination
            val contentColor = when {
                selected -> AppPrimary
                destination.enabled -> Color(0xFF8A8A8A)
                else -> Color(0xFF3A3A3A)
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (destination.enabled) {
                        onDestinationSelected(destination)
                    }
                },
                enabled = destination.enabled,
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        modifier = Modifier.size(22.dp),
                        tint = contentColor
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        color = contentColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppPrimary,
                    selectedTextColor = AppPrimary,
                    indicatorColor = AppPrimary.copy(alpha = 0.12f),
                    unselectedIconColor = Color(0xFF3A3A3A),
                    unselectedTextColor = Color(0xFF3A3A3A),
                    disabledIconColor = Color(0xFF3A3A3A),
                    disabledTextColor = Color(0xFF3A3A3A)
                )
            )
        }
    }
}