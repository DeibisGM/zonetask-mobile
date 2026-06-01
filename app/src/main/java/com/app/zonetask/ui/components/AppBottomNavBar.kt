package com.app.zonetask.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.zonetask.R
import com.app.zonetask.ui.theme.AppPrimary

enum class NavDestination(
    @DrawableRes val iconRes: Int,
    val enabled: Boolean
) {
    HOME(iconRes = R.drawable.ic_house, enabled = true),
    TASKS(iconRes = R.drawable.ic_check_square, enabled = true),
    CHAT(iconRes = R.drawable.ic_chat_circle, enabled = false),
    PROFILE(iconRes = R.drawable.ic_user, enabled = false),
    SETTINGS(iconRes = R.drawable.ic_gear_six, enabled = false)
}

@Composable
fun AppBottomNavBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit
) {
    Surface(
        color = Color(0xFF0A0A0A)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            NavDestination.entries.forEach { destination ->
                val selected = destination == currentDestination
                val tint = when {
                    selected -> AppPrimary
                    destination.enabled -> Color(0xFF8A8A8A)
                    else -> Color(0xFF383838)
                }

                NavigationBarItem(
                    selected = selected,
                    onClick = { if (destination.enabled) onDestinationSelected(destination) },
                    enabled = destination.enabled,
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = tint
                        )
                    },
                    label = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppPrimary,
                        selectedTextColor = AppPrimary,
                        indicatorColor = Color.Transparent,
                        unselectedTextColor = Color(0xFF8A8A8A),
                        unselectedIconColor = Color(0xFF8A8A8A),
                        disabledIconColor = Color(0xFF383838)
                    )
                )
            }
        }
    }
}
