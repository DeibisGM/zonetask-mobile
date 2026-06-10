package com.app.zonetask.navigation

// Top-level destinations not owned by a feature module.
// Feature routes (e.g. spaces) live in their own navigation package.
object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile_edit"

    const val TASK_CREATE             = "task_create"
    const val TASK_CREATE_WITH_SPACE  = "task_create/{spaceId}"
    const val TASK_EDIT_WITH_SPACE    = "task_edit/{spaceId}/{taskId}"
    const val TASKS                   = "tasks/{userId}"
    const val MY_INVITATIONS          = "my_invitations/{userId}"
    
    const val HOME                     = "home/{spaceId}"
    const val TASK_CREATE              = "task_create"
    const val TASK_CREATE_WITH_SPACE   = "task_create/{spaceId}"
    const val TASK_EDIT_WITH_SPACE     = "task_edit/{spaceId}/{taskId}"
    const val TASK_DETAIL              = "task_detail/{spaceId}/{taskId}"
    const val TASKS                    = "tasks/{userId}"

    fun homeRoute(spaceId: Int): String = "home/$spaceId"
    fun tasksRoute(userId: Int): String = "tasks/$userId"
    fun myInvitationsRoute(userId: Int): String = "my_invitations/$userId"
    fun taskCreateRoute(spaceId: Int): String = "task_create/$spaceId"
    fun taskEditRoute(spaceId: Int, taskId: Int): String =
        "task_edit/$spaceId/$taskId"
    fun taskDetailRoute(spaceId: Int, taskId: Int): String =
        "task_detail/$spaceId/$taskId"
}
