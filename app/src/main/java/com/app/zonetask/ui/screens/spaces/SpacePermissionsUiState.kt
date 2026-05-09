package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.domain.model.SpaceMember

data class SpacePermissionsUiState(
    val isLoading: Boolean                     = false,
    val requestingUserRole: String             = "",
    val roleActions: Map<String, List<String>> = emptyMap(),

    val members: List<SpaceMember>             = emptyList(),

    val errorBanner: String?                   = null,

    val memberPendingRoleChange: SpaceMember?  = null,
    val updatingMemberId: Int?                 = null,
    val roleUpdateError: String?               = null
)