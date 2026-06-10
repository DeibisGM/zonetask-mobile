package com.app.zonetask.data.remote.dto

data class RespondToInvitationRequest(
    val status: String,
    // Temporary identity fields. The backend reads the responder identity from
    // the body only while Firebase Auth is not connected; once the JWT
    // middleware is in place these come from the token claims and are removed.
    val userId: Int,
    val email: String
)