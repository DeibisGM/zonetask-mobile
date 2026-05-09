package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.app.zonetask.domain.model.SpaceMember

data class SpaceMemberResponse(
    @SerializedName("memberId")
    val memberId: Int,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("rotationOrder")
    val rotationOrder: Int?,

    @SerializedName("joinedAt")
    val joinedAt: String?,

    @SerializedName("invitedBy")
    val invitedBy: Int?,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("spaceId")
    val spaceId: Int
)

fun SpaceMemberResponse.toDomain(): SpaceMember = SpaceMember(
    memberId     = memberId,
    role         = role,
    status       = status,
    rotationOrder = rotationOrder,
    joinedAt     = joinedAt,
    invitedBy    = invitedBy,
    userId       = userId,
    spaceId      = spaceId
)
