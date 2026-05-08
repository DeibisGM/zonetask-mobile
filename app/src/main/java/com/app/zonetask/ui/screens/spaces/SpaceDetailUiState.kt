package com.app.zonetask.ui.screens.spacedetail

import com.app.zonetask.domain.model.Space

data class SpaceDetailUiState(
    val isLoading: Boolean = false,
    val space: Space? = null,
    val errorBanner: String? = null
)
