package com.app.zonetask.core

import com.app.zonetask.BuildConfig

object AppConstants {

    object Api {
        // Base URL injected from the Android build so the IP can change without touching code.
        val BASE_URL: String = BuildConfig.API_BASE_URL

        object Paths {
            // API routes used by the task creation flow.
            const val USER_SPACES = "users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{spaceId}"
            const val USER_SPACES = "api/users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{spaceId}"
            const val DELETE_SPACE = "spaces/{spaceId}"
            const val TASK_FORM_OPTIONS = "api/lookups/task-form-options"
            const val ZONE_OBJECTS = "api/lookups/zones/{zoneId}/objects"
            const val TASKS = "api/tasks"
        }
    }
}
