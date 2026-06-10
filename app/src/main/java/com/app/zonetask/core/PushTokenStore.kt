package com.app.zonetask.core

object PushTokenStore {

    @Volatile
    var fcmToken: String? = null
        private set

    fun save(token: String?) {
        fcmToken = token
    }
}
