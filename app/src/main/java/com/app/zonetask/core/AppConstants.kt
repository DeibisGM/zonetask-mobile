package com.app.zonetask.core

import com.app.zonetask.BuildConfig

object AppConstants {

    object Api {

        // Base URL configured from Gradle to avoid hardcoding the local IP.
        val BASE_URL: String = BuildConfig.API_BASE_URL

        object Paths {

            // Space endpoints .
            const val USER_SPACES = "api/spaces/users/{userId}"
            const val USER_PUSH_TOKEN = "api/users/{userId}/push-token"
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
            const val SPACE_MEMBERS            = "api/spaces/{spaceId}/members"
            const val SPACE_MEMBER_DIRECTORY   = "api/spaces/{spaceId}/members/directory"
            const val SPACE_PENDING_INVITATIONS = "api/spaces/{spaceId}/invitations/pending"
            const val UPDATE_MEMBER_ROLE =
                "api/spaces/{spaceId}/members/{memberId}/role"

            // Invitation endpoints
            const val CREATE_INVITATION = "api/invitations"
            const val USER_INVITATIONS  = "api/users/{userId}/invitations"
            const val RESPOND_INVITATION = "api/invitations/{invitationId}/respond"
            // Floor plan endpoints
            const val SPACE_PLANS = "api/spaces/{spaceId}/plans"
            const val PLAN_BY_ID  = "api/plans/{planId}"
            const val CREATE_PLAN = "api/plans"
            const val UPDATE_PLAN = "api/plans/{planId}"

            // Zone endpoints
            const val PLAN_ZONES = "api/plans/{planId}/zones"
            const val ZONE_BY_ID = "api/zones/{zoneId}"

            // Chat endpoints
            const val CHAT_BY_SPACE  = "api/spaces/{spaceId}/chat"
            const val CHAT_IMAGE     = "api/spaces/{spaceId}/chat/image"
            const val CHAT_MEMBERS   = "api/spaces/{spaceId}/chat/members"
            const val SPACE_MESSAGES        = "api/spaces/{spaceId}/messages"
            const val SPACE_MESSAGES_UPLOAD = "api/spaces/{spaceId}/messages/upload"
            const val USER_CHATS            = "api/users/{userId}/chats"

            // Completion history endpoint
            const val COMPLETED_TASKS = "api/spaces/{spaceId}/completed-tasks"

            // Rotation history endpoint
            const val TASK_ROTATION_HISTORY = "api/tasks/{taskId}/rotation-log"
            const val SPACE_ROTATION_HISTORY = "api/spaces/{spaceId}/rotation-log"

            // Statistics endpoints
            const val USER_STATISTICS   = "api/spaces/{spaceId}/members/{userId}/statistics"
            const val SPACE_STATISTICS  = "api/spaces/{spaceId}/statistics"
            const val USER_REPORTS      = "api/spaces/{spaceId}/reports/users"
            const val SPACE_REPORTS     = "api/users/{userId}/reports/spaces"
            const val OVERDUE_TRENDS    = "api/spaces/{spaceId}/reports/overdue-trends"
        }
    }
}
