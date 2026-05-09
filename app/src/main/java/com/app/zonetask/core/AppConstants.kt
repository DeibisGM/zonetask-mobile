package com.app.zonetask.core

object AppConstants {

    object Api {
        // Local backend base URL for the phone or emulator.
        const val BASE_URL = "http://192.168.100.130:5248/"

        object Paths {
            const val USER_SPACES        = "api/spaces/users/{userId}/spaces"
            const val SPACE_BY_ID        = "api/spaces/spaces/{spaceId}"
            const val DELETE_SPACE       = "api/spaces/spaces/{spaceId}"

            const val TASK_FORM_OPTIONS  = "api/lookups/task-form-options"
            const val ZONE_OBJECTS       = "api/lookups/zones/{zoneId}/objects"
            const val TASKS              = "api/tasks"

            const val SPACE_PERMISSIONS  = "api/spaces/spaces/{spaceId}/permissions"
            const val SPACE_MEMBERS      = "api/spaces/spaces/{spaceId}/members"
            const val UPDATE_MEMBER_ROLE = "api/spaces/spaces/{spaceId}/members/{memberId}/role"
        }
    }
}