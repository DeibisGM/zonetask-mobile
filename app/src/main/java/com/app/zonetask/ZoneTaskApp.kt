package com.app.zonetask

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.FirebaseMessagingTokenProvider
import com.app.zonetask.navigation.AppNavHost
import com.app.zonetask.ui.theme.ZoneTaskTheme

@Composable
fun ZoneTaskApp() {
    val context = LocalContext.current

    ZoneTaskTheme {
        AuthSessionStore.initialize(context)

        LaunchedEffect(Unit) {
            FirebaseMessagingTokenProvider.getToken()
        }
        AppNavHost()
    }
}
