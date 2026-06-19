package com.app.zonetask.messaging

import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.PushTokenStore
import com.app.zonetask.di.AppContainer
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ZoneTaskFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushTokenStore.save(token)
        syncTokenWithBackend(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["title"]
            ?: message.notification?.title
            ?: "ZoneTask"
        val body = data["body"]
            ?: message.notification?.body
            ?: "You have a new task update."
        val spaceId = data["space_id"]?.toIntOrNull() ?: return
        val taskId = data["task_id"]?.toIntOrNull() ?: return
        val type = data["notification_type"] ?: "task_event"

        ZoneTaskNotificationManager.showTaskNotification(
            context = applicationContext,
            title = title,
            body = body,
            spaceId = spaceId,
            taskId = taskId,
            notificationType = type
        )
    }

    private fun syncTokenWithBackend(token: String) {
        val userId = AuthSessionStore.currentUser?.userId ?: return

        serviceScope.launch {
            AppContainer.userRepository.updatePushToken(userId, token)
        }
    }
}
