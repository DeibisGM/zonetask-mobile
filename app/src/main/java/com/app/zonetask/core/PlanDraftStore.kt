package com.app.zonetask.core

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.app.zonetask.ui.screens.plan.PlanZoneDraftSnapshot
import com.google.gson.Gson

object PlanDraftStore {

    private const val DB_NAME = "plan_drafts.db"
    private const val DB_VERSION = 1
    private const val TABLE_DRAFTS = "plan_editor_drafts"
    private const val COL_KEY = "draft_key"
    private const val COL_PAYLOAD = "payload"
    private const val COL_UPDATED_AT = "updated_at"
    private const val PREFS_NAME = "plan_draft_store"

    @Volatile
    private var appContext: Context? = null
    @Volatile
    private var helper: DraftDatabaseHelper? = null

    private val gson = Gson()

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
        if (helper == null && appContext != null) {
            helper = DraftDatabaseHelper(appContext!!)
        }
    }

    fun saveDraft(
        spaceId: Int,
        planId: Int?,
        name: String,
        canvasWidth: String,
        canvasHeight: String,
        setupComplete: Boolean,
        selectedZoneId: String?,
        zones: List<PlanZoneDraftSnapshot>
    ) {
        val dbHelper = helper ?: return
        val payload = PlanEditorDraftSnapshot(
            name = name,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            setupComplete = setupComplete,
            selectedZoneId = selectedZoneId,
            zones = zones
        )
        val key = storageKey(spaceId, planId)
        val json = gson.toJson(payload)

        val values = android.content.ContentValues().apply {
            put(COL_KEY, key)
            put(COL_PAYLOAD, json)
            put(COL_UPDATED_AT, System.currentTimeMillis())
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.insertWithOnConflict(TABLE_DRAFTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        clearLegacyPrefs(spaceId, planId)
    }

    fun loadDraft(spaceId: Int, planId: Int?): PlanEditorDraftSnapshot? {
        val key = storageKey(spaceId, planId)
        helper?.readDraft(key)?.let { return it }
        return loadLegacyPrefsDraft(spaceId, planId)?.also {
            saveDraft(
                spaceId = spaceId,
                planId = planId,
                name = it.name,
                canvasWidth = it.canvasWidth,
                canvasHeight = it.canvasHeight,
                setupComplete = it.setupComplete,
                selectedZoneId = it.selectedZoneId,
                zones = it.zones
            )
        }
    }

    fun loadZones(spaceId: Int, planId: Int?): List<PlanZoneDraftSnapshot> {
        return loadDraft(spaceId, planId)?.zones.orEmpty()
    }

    fun clearDraft(spaceId: Int, planId: Int? = null) {
        val dbHelper = helper ?: return
        val key = storageKey(spaceId, planId)
        dbHelper.writableDatabase.delete(
            TABLE_DRAFTS,
            "$COL_KEY = ?",
            arrayOf(key)
        )
        clearLegacyPrefs(spaceId, planId)
    }

    private fun loadLegacyPrefsDraft(spaceId: Int, planId: Int?): PlanEditorDraftSnapshot? {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return null
        val raw = prefs.getString(storageKey(spaceId, planId), null) ?: return null
        return runCatching {
            gson.fromJson(raw, PlanEditorDraftSnapshot::class.java)
        }.getOrNull()
    }

    private fun clearLegacyPrefs(spaceId: Int, planId: Int?) {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        prefs.edit().remove(storageKey(spaceId, planId)).apply()
    }

    private fun storageKey(spaceId: Int, planId: Int?): String {
        val userId = AuthSessionStore.currentUser?.userId ?: 0
        return if (planId != null) {
            "user_${userId}_plan_$planId"
        } else {
            "user_${userId}_space_$spaceId"
        }
    }

    private class DraftDatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_DRAFTS (
                    $COL_KEY TEXT PRIMARY KEY NOT NULL,
                    $COL_PAYLOAD TEXT NOT NULL,
                    $COL_UPDATED_AT INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion != newVersion) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_DRAFTS")
                onCreate(db)
            }
        }

        fun readDraft(key: String): PlanEditorDraftSnapshot? {
            val db = readableDatabase
            db.rawQuery(
                "SELECT $COL_PAYLOAD FROM $TABLE_DRAFTS WHERE $COL_KEY = ? LIMIT 1",
                arrayOf(key)
            ).use { cursor ->
                if (!cursor.moveToFirst()) return null
                val payload = cursor.getString(0) ?: return null
                return runCatching {
                    Gson().fromJson(payload, PlanEditorDraftSnapshot::class.java)
                }.getOrNull()
            }
        }
    }
}
