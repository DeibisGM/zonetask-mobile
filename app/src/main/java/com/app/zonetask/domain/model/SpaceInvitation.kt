package com.app.zonetask.domain.model

data class SpaceInvitation(
    val invitationId: Int,
    val spaceId: Int,
    val spaceName: String?,
    val invitedBy: Int,
    val invitedUserId: Int?,
    val email: String?,
    val invitationType: InvitationType,
    val status: InvitationStatus,
    val message: String?,
    val expiresAt: String?,
    val respondedAt: String?
)

enum class InvitationType(val wireValue: String) {
    DIRECT("direct"),
    EMAIL("email"),
    LINK("link");

    companion object {
        fun from(value: String?): InvitationType =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: DIRECT
    }
}

enum class InvitationStatus(val wireValue: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    EXPIRED("expired"),
    CANCELLED("cancelled");

    companion object {
        fun from(value: String?): InvitationStatus =
            entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: PENDING
    }
}