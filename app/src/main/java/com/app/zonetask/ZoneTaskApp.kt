package com.app.zonetask

import androidx.compose.runtime.Composable
import com.app.zonetask.navigation.AppNavHost
import com.app.zonetask.ui.theme.ZoneTaskTheme

@Composable
fun ZoneTaskApp() {
    ZoneTaskTheme {
        AppNavHost()
    }
}