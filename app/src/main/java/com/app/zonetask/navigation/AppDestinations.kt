package com.app.zonetask.navigation

object AppDestinations {
    const val LOGIN = "login"
    const val TASK_CREATE = "task_create"
    const val TASK_CREATE_WITH_SPACE = "task_create/{spaceId}"
    const val TASK_EDIT_WITH_SPACE = "task_edit/{spaceId}/{taskId}"
    const val SPACES = "spaces/{userId}"
    const val TASKS = "tasks/{userId}"
    const val CREATE_SPACE = "create_space"
    const val SPACE_DETAIL_ROUTE = "space_detail"
    const val SPACE_DETAIL = "space_detail/{spaceId}"
    const val SPACE_PERMISSIONS_ROUTE = "space_permissions"
    const val SPACE_PERMISSIONS = "space_permissions/{spaceId}"
    const val EDIT_SPACE_ROUTE = "edit_space"
    const val EDIT_SPACE = "edit_space/{spaceId}"

    fun spacesRoute(userId: Int): String = "spaces/$userId"
    fun tasksRoute(userId: Int): String = "tasks/$userId"
    fun taskCreateRoute(spaceId: Int): String = "task_create/$spaceId"
    fun taskEditRoute(spaceId: Int, taskId: Int): String = "task_edit/$spaceId/$taskId"
}
