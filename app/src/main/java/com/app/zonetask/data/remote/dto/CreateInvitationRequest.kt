package com.app.zonetask.data.remote.dto

import com.app.zonetask.domain.model.InvitationType

data class CreateInvitationRequest(
    val spaceId: Int,
    val invitedBy: Int,
    // This screen always invites through the email channel, so the type is
    // fixed to EMAIL and emailInvited is always populated (required by the
    // backend for email-type invitations). DIRECT (target by invitedUserId)
    // and LINK (no target) are separate entry points, not variants of this flow.
    val emailInvited: String,
    val invitationType: String = InvitationType.EMAIL.wireValue,
    val message: String? = null
)