package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.domain.model.Space
import com.app.zonetask.domain.model.SpaceRole

data class SpacesUiState(
    val isLoading: Boolean                  = false,
    val spaces: List<Space>                 = emptyList(),
    val spaceRoles: Map<Int, SpaceRole>     = emptyMap(),

    val errorBanner: String?                = null,
    val deletingSpaceId: Int?               = null
) {
    val deletableSpaceIds: Set<Int>
        get() = spaceRoles
            .filter { (_, role) -> role == SpaceRole.OWNER }
            .keys
}