package com.app.zonetask.ui.screens.invitations

data class InviteMemberUiState(
    val email: String       = "",
    val message: String     = "",
    val isSending: Boolean   = false,
    val emailError: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && !isSending
}