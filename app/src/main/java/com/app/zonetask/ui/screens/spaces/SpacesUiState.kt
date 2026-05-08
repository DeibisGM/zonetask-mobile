package com.app.zonetask.ui.screens.spaces

import com.app.zonetask.domain.model.Space

data class SpacesUiState(
    val isLoading: Boolean = false,
    val spaces: List<Space> = emptyList(),
    val errorBanner: String? = null
)