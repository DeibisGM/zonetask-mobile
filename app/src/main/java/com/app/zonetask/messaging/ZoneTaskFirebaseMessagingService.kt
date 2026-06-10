package com.app.zonetask.messaging

import com.app.zonetask.core.PushTokenStore
import com.google.firebase.messaging.FirebaseMessagingService

class ZoneTaskFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushTokenStore.save(token)
    }
}
