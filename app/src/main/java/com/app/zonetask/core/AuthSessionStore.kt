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
    private const val KEY_PROFILE_PICTURE_URL = "profile_picture_url"
    private const val KEY_PHONE = "phone"
    private const val KEY_BIRTH_DATE = "birth_date"
    private const val KEY_GENDER = "gender"
    private const val KEY_BIO = "bio"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_EMAIL_VERIFIED = "email_verified"

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

    // Initializes the session cache once and restores the last stored auth state.
    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            restoreFromStorage()
        }
    }

    // Persists the current auth tokens and the latest user snapshot.
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

    // Clears the in-memory state and wipes the persisted session data.
    fun clear() {
        sessionToken = null
        refreshToken = null
        currentUser = null

        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.clear()
            ?.apply()
    }

    // Rehydrates the last known session from shared preferences.
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
                displayName = prefs.getString(KEY_DISPLAY_NAME, "").orEmpty(),
                profilePictureUrl = prefs.getString(KEY_PROFILE_PICTURE_URL, null),
                phone = prefs.getString(KEY_PHONE, null),
                birthDate = prefs.getString(KEY_BIRTH_DATE, null),
                gender = prefs.getString(KEY_GENDER, null),
                bio = prefs.getString(KEY_BIO, null),
                onboardingCompleted = readNullableBoolean(KEY_ONBOARDING_COMPLETED),
                emailVerified = readNullableBoolean(KEY_EMAIL_VERIFIED)
            )
        } else {
            null
        }

        sessionToken = prefs.getString(KEY_SESSION_TOKEN, null)
        refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        currentUser = user
    }

    // Writes the current session snapshot to shared preferences.
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
                    putString(KEY_PROFILE_PICTURE_URL, currentUser!!.profilePictureUrl)
                    putString(KEY_PHONE, currentUser!!.phone)
                    putString(KEY_BIRTH_DATE, currentUser!!.birthDate)
                    putString(KEY_GENDER, currentUser!!.gender)
                    putString(KEY_BIO, currentUser!!.bio)
                    putBoolean(KEY_ONBOARDING_COMPLETED, currentUser!!.onboardingCompleted == true)
                    putBoolean(KEY_EMAIL_VERIFIED, currentUser!!.emailVerified == true)
                } else {
                    remove(KEY_USER_ID)
                    remove(KEY_USERNAME)
                    remove(KEY_EMAIL)
                    remove(KEY_FIRST_NAME)
                    remove(KEY_LAST_NAME)
                    remove(KEY_DISPLAY_NAME)
                    remove(KEY_PROFILE_PICTURE_URL)
                    remove(KEY_PHONE)
                    remove(KEY_BIRTH_DATE)
                    remove(KEY_GENDER)
                    remove(KEY_BIO)
                    remove(KEY_ONBOARDING_COMPLETED)
                    remove(KEY_EMAIL_VERIFIED)
                }
            }
            .apply()
    }

    // Reads nullable booleans without losing the distinction between false and missing values.
    private fun readNullableBoolean(key: String): Boolean? {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: return null
        return if (prefs.contains(key)) prefs.getBoolean(key, false) else null
    }
}
