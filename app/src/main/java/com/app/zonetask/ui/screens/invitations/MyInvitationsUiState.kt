package com.app.zonetask.ui.screens.invitations

import com.app.zonetask.domain.model.SpaceInvitation

data class MyInvitationsUiState(
    val isLoading: Boolean                  = false,
    val invitations: List<SpaceInvitation>  = emptyList(),
    val errorBanner: String?                = null,
    val respondingInvitationId: Int?        = null
)