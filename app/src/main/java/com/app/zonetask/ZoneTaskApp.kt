package com.app.zonetask

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.FirebaseMessagingTokenProvider
import com.app.zonetask.core.PlanDraftStore
import com.app.zonetask.core.WorkspaceStore
import com.app.zonetask.di.AppContainer
import com.app.zonetask.messaging.ZoneTaskNotificationManager
import com.app.zonetask.navigation.AppNavHost
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.ZoneTaskTheme

@Composable
fun ZoneTaskApp() {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    var permissionRequested by remember { mutableStateOf(false) }

    ZoneTaskTheme {
        AuthSessionStore.initialize(context)
        PlanDraftStore.initialize(context)
        WorkspaceStore.initialize(context)

        LaunchedEffect(Unit) {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED &&
                !permissionRequested
            ) {
                permissionRequested = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            ZoneTaskNotificationManager.ensureChannel(context)

            val token = FirebaseMessagingTokenProvider.getToken()
            val userId = AuthSessionStore.currentUser?.userId
            if (!token.isNullOrBlank() && userId != null && userId > 0) {
                AppContainer.userRepository.updatePushToken(userId, token)
            }
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
