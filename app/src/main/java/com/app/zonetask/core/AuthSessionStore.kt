package com.app.zonetask.core

import com.app.zonetask.data.remote.dto.UserResponse

object AuthSessionStore {

    @Volatile
    var sessionToken: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    @Volatile
    var currentUser: UserResponse? = null
        private set

    fun save(
        sessionToken: String?,
        refreshToken: String?,
        user: UserResponse?
    ) {
        this.sessionToken = sessionToken
        this.refreshToken = refreshToken
        this.currentUser = user
    }

    fun clear() {
        sessionToken = null
        refreshToken = null
        currentUser = null
    }
}
