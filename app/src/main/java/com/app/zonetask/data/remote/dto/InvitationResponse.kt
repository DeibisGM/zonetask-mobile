package com.app.zonetask.data.remote.dto

import com.app.zonetask.domain.model.InvitationStatus
import com.app.zonetask.domain.model.InvitationType
import com.app.zonetask.domain.model.SpaceInvitation

data class InvitationResponse(
    val invitationId: Int,
    val invitationType: String?,
    val status: String?,
    val emailInvited: String?,
    val message: String?,
    val expiresAt: String?,
    val respondedAt: String?,
    val invitedBy: Int,
    val invitedUserId: Int?,
    val spaceId: Int,
    val spaceName: String?
)

fun InvitationResponse.toDomain(): SpaceInvitation = SpaceInvitation(
    invitationId   = invitationId,
    spaceId        = spaceId,
    spaceName      = spaceName,
    invitedBy      = invitedBy,
    invitedUserId  = invitedUserId,
    email          = emailInvited,
    invitationType = InvitationType.from(invitationType),
    status         = InvitationStatus.from(status),
    message        = message,
    expiresAt      = expiresAt,
    respondedAt    = respondedAt
)