package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.domain.model.Space

data class SpacesUiState(
    val isLoading: Boolean           = false,
    val spaces: List<Space>          = emptyList(),
    // spaceId -> rol real ("owner" | "admin" | "member")
    val spaceRoles: Map<Int, String> = emptyMap(),
    val errorBanner: String?         = null,
    val deletingSpaceId: Int?        = null
)
