package com.app.zonetask.core

import android.content.Context
import com.app.zonetask.data.remote.dto.UserResponse

object AuthSessionStore {

    private const val PREFS_NAME = "auth_session_store"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_DISPLAY_NAME = "display_name"

    @Volatile
    private var appContext: Context? = null

    @Volatile
    var sessionToken: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    @Volatile
    var currentUser: UserResponse? = null
        private set

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            restoreFromStorage()
        }
    }

    fun save(
        sessionToken: String?,
        refreshToken: String?,
        user: UserResponse?
    ) {
        this.sessionToken = sessionToken
        this.refreshToken = refreshToken
        this.currentUser = user
        persist()
    }

    fun clear() {
        sessionToken = null
        refreshToken = null
        currentUser = null

        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.clear()
            ?.apply()
    }

    private fun restoreFromStorage() {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: return

        val userId = prefs.getInt(KEY_USER_ID, 0)
        val user = if (userId > 0) {
            UserResponse(
                userId = userId,
                username = prefs.getString(KEY_USERNAME, "").orEmpty(),
                email = prefs.getString(KEY_EMAIL, "").orEmpty(),
                firstName = prefs.getString(KEY_FIRST_NAME, "").orEmpty(),
                lastName = prefs.getString(KEY_LAST_NAME, null),
                displayName = prefs.getString(KEY_DISPLAY_NAME, "").orEmpty()
            )
        } else {
            null
        }

        sessionToken = prefs.getString(KEY_SESSION_TOKEN, null)
        refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        currentUser = user
    }

    private fun persist() {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: return

        prefs.edit()
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply {
                if (currentUser != null) {
                    putInt(KEY_USER_ID, currentUser!!.userId)
                    putString(KEY_USERNAME, currentUser!!.username)
                    putString(KEY_EMAIL, currentUser!!.email)
                    putString(KEY_FIRST_NAME, currentUser!!.firstName)
                    putString(KEY_LAST_NAME, currentUser!!.lastName)
                    putString(KEY_DISPLAY_NAME, currentUser!!.displayName)
                } else {
                    remove(KEY_USER_ID)
                    remove(KEY_USERNAME)
                    remove(KEY_EMAIL)
                    remove(KEY_FIRST_NAME)
                    remove(KEY_LAST_NAME)
                    remove(KEY_DISPLAY_NAME)
                }
            }
            .apply()
    }
}
