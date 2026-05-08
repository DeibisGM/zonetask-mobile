package com.app.zonetask.core

object AppConstants {

    object Api {
        // Local backend base URL for the phone or emulator.
        const val BASE_URL = "http://192.168.0.143:5248/"

        object Paths {
            // API routes used by the task creation flow.
            const val USER_SPACES = "users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{id}"
            const val TASK_FORM_OPTIONS = "api/lookups/task-form-options"
            const val ZONE_OBJECTS = "api/lookups/zones/{zoneId}/objects"
            const val TASKS = "api/tasks"
        }
    }
}
