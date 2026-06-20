package com.app.zonetask.core

import android.content.Context

data class WorkspaceSnapshot(
    val spaceId: Int = 0,
    val planId: Int? = null
)

object WorkspaceStore {

    private const val PREFS_NAME = "workspace_store"
    private const val KEY_LAST_SPACE_PREFIX = "last_space_"
    private const val KEY_LAST_PLAN_PREFIX = "last_plan_"

    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun rememberSpace(userId: Int, spaceId: Int) {
        if (userId <= 0 || spaceId <= 0) return
        prefs().edit()
            .putInt(spaceKey(userId), spaceId)
            .apply()
    }

    fun rememberPlan(userId: Int, spaceId: Int, planId: Int) {
        if (userId <= 0 || spaceId <= 0 || planId <= 0) return
        prefs().edit()
            .putInt(spaceKey(userId), spaceId)
            .putInt(planKey(userId, spaceId), planId)
            .apply()
    }

    fun getLastSpaceId(userId: Int): Int {
        if (userId <= 0) return 0
        return prefs().getInt(spaceKey(userId), 0)
    }

    fun getLastPlanId(userId: Int, spaceId: Int): Int? {
        if (userId <= 0 || spaceId <= 0) return null
        val key = planKey(userId, spaceId)
        val prefs = prefs()
        return if (prefs.contains(key)) prefs.getInt(key, 0).takeIf { it > 0 } else null
    }

    fun clear(userId: Int) {
        if (userId <= 0) return
        prefs().edit()
            .remove(spaceKey(userId))
            .apply()
    }

    private fun prefs() = appContext
        ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ?: throw IllegalStateException("WorkspaceStore is not initialized")

    private fun spaceKey(userId: Int) = "${KEY_LAST_SPACE_PREFIX}$userId"

    private fun planKey(userId: Int, spaceId: Int) = "${KEY_LAST_PLAN_PREFIX}${userId}_$spaceId"
}
