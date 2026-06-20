package com.app.zonetask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.zonetask.messaging.NotificationNavigationStore
import com.app.zonetask.messaging.ZoneTaskNotificationManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        handleNotificationIntent(intent)

        setContent {
            ZoneTaskApp()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        ZoneTaskNotificationManager.extractRoute(intent)?.let(NotificationNavigationStore::postRoute)
    }
}
