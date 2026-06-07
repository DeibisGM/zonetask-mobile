package com.app.zonetask

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.zonetask.core.FirebaseMessagingTokenProvider
import com.app.zonetask.navigation.AppNavHost
import com.app.zonetask.ui.theme.ZoneTaskTheme

@Composable
fun ZoneTaskApp() {
    ZoneTaskTheme {
        LaunchedEffect(Unit) {
            FirebaseMessagingTokenProvider.getToken()
        }
        AppNavHost()
    }
}
