package com.app.zonetask.domain.model

data class SpaceMember(
    val memberId: Int,
    val role: String,
    val status: String,
    val rotationOrder: Int?,
    val joinedAt: String?,
    val invitedBy: Int?,
    val userId: Int,
    val spaceId: Int
)
