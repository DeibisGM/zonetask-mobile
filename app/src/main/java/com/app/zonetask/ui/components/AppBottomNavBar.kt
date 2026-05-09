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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.zonetask.R
import com.app.zonetask.ui.theme.AppPrimary

enum class NavDestination(
    val label: String,
    @DrawableRes val iconRes: Int,
    val enabled: Boolean
) {
    HOME(label = "Home", iconRes = R.drawable.ic_house, enabled = false),
    TASKS(label = "Tasks", iconRes = R.drawable.ic_check_square, enabled = true),
    CHAT(label = "Chat", iconRes = R.drawable.ic_chat_circle, enabled = false),
    PROFILE(label = "Profile", iconRes = R.drawable.ic_user, enabled = false),
    SPACES(label = "Spaces", iconRes = R.drawable.ic_buildings, enabled = true)
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
                            contentDescription = destination.label,
                            modifier = Modifier.size(28.dp),
                            tint = tint
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
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
