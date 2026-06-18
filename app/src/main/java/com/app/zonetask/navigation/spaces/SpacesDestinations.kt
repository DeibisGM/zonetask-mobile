package com.app.zonetask.navigation.spaces

// Routes and argument keys owned by the spaces feature.
object SpacesDestinations {
    const val ARG_USER_ID  = "userId"
    const val ARG_SPACE_ID = "spaceId"

    const val LIST             = "spaces/{$ARG_USER_ID}"
    const val CREATE           = "create_space"
    const val DETAIL           = "space_detail/{$ARG_SPACE_ID}"
    const val EDIT             = "edit_space/{$ARG_SPACE_ID}"
    const val PERMISSIONS      = "space_permissions/{$ARG_SPACE_ID}"
    const val INVITE           = "space_invite/{$ARG_SPACE_ID}"
    const val COMPLETED_TASKS  = "completed_tasks/{$ARG_SPACE_ID}"
    const val STATISTICS       = "statistics/{$ARG_SPACE_ID}/{$ARG_USER_ID}"
    const val SPACE_STATISTICS = "space_statistics/{$ARG_SPACE_ID}"
    const val USER_REPORTS     = "user_reports/{$ARG_SPACE_ID}"

    // Type-safe builders — prefer over string concatenation at call sites.
    fun list(userId: Int): String                     = "spaces/$userId"
    fun detail(spaceId: Int): String                  = "space_detail/$spaceId"
    fun edit(spaceId: Int): String                    = "edit_space/$spaceId"
    fun permissions(spaceId: Int): String             = "space_permissions/$spaceId"
    fun invite(spaceId: Int): String                  = "space_invite/$spaceId"
    fun completedTasks(spaceId: Int): String          = "completed_tasks/$spaceId"
    fun statistics(spaceId: Int, userId: Int): String = "statistics/$spaceId/$userId"
    fun spaceStatistics(spaceId: Int): String         = "space_statistics/$spaceId"
    fun userReports(spaceId: Int): String             = "user_reports/$spaceId"
}

// Keys for results passed between spaces screens via savedStateHandle.
object SpacesNavKeys {
    const val SUCCESS_MESSAGE = "spaces_success_message"
    const val REFRESH_DETAIL  = "spaces_refresh_detail"
}
