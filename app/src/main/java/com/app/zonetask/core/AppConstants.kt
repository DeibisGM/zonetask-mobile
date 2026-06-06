package com.app.zonetask.core

import com.app.zonetask.BuildConfig

object AppConstants {

    object Api {

        // Base URL configured from Gradle to avoid hardcoding the local IP.
        val BASE_URL: String = BuildConfig.API_BASE_URL

        object Paths {

            // Space endpoints .
            const val USER_SPACES = "api/spaces/users/{userId}"
            const val SPACE_BY_ID = "api/spaces/{spaceId}"
            const val CREATE_SPACE = "api/spaces"
            const val UPDATE_SPACE = "api/spaces/{spaceId}"
            const val DELETE_SPACE = "api/spaces/{spaceId}"

            // Task endpoints
            const val SPACE_TASKS = "api/spaces/{spaceId}/tasks"
            const val ZONE_TASKS = "api/zones/{zoneId}/tasks"
            const val TASK_BY_ID = "api/tasks/{taskId}"
            const val TASKS = "api/tasks"

            // Lookup endpoints
            const val TASK_FORM_OPTIONS = "api/lookups/task-form-options"
            const val ZONE_OBJECTS = "api/lookups/zones/{zoneId}/objects"

            // Member & permissions endpoints
            const val SPACE_PERMISSIONS = "api/spaces/{spaceId}/permissions"
            const val SPACE_MEMBERS = "api/spaces/{spaceId}/members"
            const val UPDATE_MEMBER_ROLE =
                "api/spaces/{spaceId}/members/{memberId}/role"

            // Floor plan endpoints
            const val SPACE_PLANS = "api/spaces/{spaceId}/plans"
            const val PLAN_BY_ID  = "api/plans/{planId}"
            const val CREATE_PLAN = "api/plans"
            const val UPDATE_PLAN = "api/plans/{planId}"

            // Completion history endpoint
            const val COMPLETED_TASKS = "api/spaces/{spaceId}/completed-tasks"

            // Statistics endpoint
            const val USER_STATISTICS = "api/spaces/{spaceId}/members/{userId}/statistics"
        }
    }
}