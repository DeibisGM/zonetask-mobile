package com.app.zonetask.core

import com.app.zonetask.BuildConfig

object AppConstants {
    object Api {
        // Base URL configured from Gradle to avoid hardcoding the local IP.
        val BASE_URL: String = BuildConfig.API_BASE_URL

        object Paths {
            // API routes used by the task creation flow.
            const val USER_SPACES = "users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{spaceId}"
            const val USER_SPACES = "api/users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{spaceId}"
            const val SPACE_TASKS = "api/spaces/{spaceId}/tasks"
            const val ZONE_TASKS = "api/zones/{zoneId}/tasks"

            const val CREATE_SPACE = "spaces"

            const val UPDATE_SPACE = "spaces/{spaceId}"
            const val DELETE_SPACE = "spaces/{spaceId}"
            const val TASK_FORM_OPTIONS = "api/lookups/task-form-options"
            const val ZONE_OBJECTS = "api/lookups/zones/{zoneId}/objects"
            const val TASKS = "api/tasks"
            const val SPACE_PERMISSIONS = "api/spaces/{spaceId}/permissions"
            const val SPACE_MEMBERS = "api/spaces/{spaceId}/members"
            const val UPDATE_MEMBER_ROLE = "api/spaces/{spaceId}/members/{memberId}/role"
        }
    }
}
