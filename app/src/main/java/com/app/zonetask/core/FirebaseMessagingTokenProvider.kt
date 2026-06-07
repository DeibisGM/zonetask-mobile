package com.app.zonetask.core

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FirebaseMessagingTokenProvider {

    suspend fun getToken(): String? {
        PushTokenStore.fcmToken?.let { return it }

        val token = runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrNull()

        if (!token.isNullOrBlank()) {
            PushTokenStore.save(token)
        }

        return token
    }
}
