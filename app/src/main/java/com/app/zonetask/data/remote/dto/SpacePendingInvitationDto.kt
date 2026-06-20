package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SpacePendingInvitationDto(
    @SerializedName("invitationId")  val invitationId: Int,
    @SerializedName("emailInvited")  val emailInvited: String?,
    @SerializedName("status")        val status: String,
    @SerializedName("expiresAt")     val expiresAt: String?,
    @SerializedName("createdAt")     val createdAt: String
)