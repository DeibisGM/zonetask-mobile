package com.app.zonetask.core

import android.content.Context
import com.app.zonetask.ui.screens.plan.PlanZoneDraftSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PlanDraftStore {

    private const val PREFS_NAME = "plan_draft_store"
    private const val KEY_PREFIX = "user_"

    @Volatile
    private var appContext: Context? = null

    private val gson = Gson()

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun saveZones(spaceId: Int, planId: Int?, zones: List<PlanZoneDraftSnapshot>) {
        val prefs = prefs() ?: return
        val key = storageKey(spaceId, planId)
        prefs.edit()
            .putString(key, gson.toJson(zones))
            .apply()
    }

    fun loadZones(spaceId: Int, planId: Int?): List<PlanZoneDraftSnapshot> {
        val prefs = prefs() ?: return emptyList()
        val raw = prefs.getString(storageKey(spaceId, planId), null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<PlanZoneDraftSnapshot>>() {}.type
            gson.fromJson<List<PlanZoneDraftSnapshot>>(raw, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    fun clearDraft(spaceId: Int) {
        val prefs = prefs() ?: return
        prefs.edit()
            .remove(storageKey(spaceId, null))
            .apply()
    }

    private fun storageKey(spaceId: Int, planId: Int?): String {
        val userId = AuthSessionStore.currentUser?.userId ?: 0
        return if (planId != null) {
            "${KEY_PREFIX}${userId}_plan_$planId"
        } else {
            "${KEY_PREFIX}${userId}_space_$spaceId"
        }
    }

    private fun prefs() = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
