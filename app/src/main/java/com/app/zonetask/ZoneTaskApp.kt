package com.app.zonetask

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.FirebaseMessagingTokenProvider
import com.app.zonetask.core.PlanDraftStore
import com.app.zonetask.navigation.AppNavHost
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.ZoneTaskTheme

@Composable
fun ZoneTaskApp() {
    val context = LocalContext.current

    ZoneTaskTheme {
        AuthSessionStore.initialize(context)
        PlanDraftStore.initialize(context)

        LaunchedEffect(Unit) {
            FirebaseMessagingTokenProvider.getToken()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
        ) {
            AppNavHost()
        }
    }
}
