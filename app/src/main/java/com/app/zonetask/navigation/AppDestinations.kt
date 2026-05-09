package com.app.zonetask.navigation

object AppDestinations {
    const val LOGIN                   = "login"
    const val TASK_CREATE             = "task_create"
    const val TASK_CREATE_WITH_SPACE  = "task_create/{spaceId}"
    const val SPACES                  = "spaces/{userId}"
    const val CREATE_SPACE            = "create_space"
    const val SPACE_DETAIL_ROUTE      = "space_detail"
    const val SPACE_DETAIL            = "space_detail/{spaceId}"
    const val SPACE_PERMISSIONS_ROUTE = "space_permissions"
    const val SPACE_PERMISSIONS       = "space_permissions/{spaceId}"

    fun spacesRoute(userId: Int): String    = "spaces/$userId"
    fun taskCreateRoute(spaceId: Int): String = "task_create/$spaceId"
}
