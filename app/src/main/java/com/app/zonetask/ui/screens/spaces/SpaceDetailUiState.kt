package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.domain.model.Space
import com.app.zonetask.domain.model.SpaceMember

data class SpaceDetailUiState(
    val isLoading: Boolean         = false,
    val space: Space?              = null,
    val members: List<SpaceMember> = emptyList(),
    val userRole: String           = "",
    val errorBanner: String?       = null
)
