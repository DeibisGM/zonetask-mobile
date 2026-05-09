package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateMemberRoleRequest(
    @SerializedName("newRole")
    val newRole: String,

    @SerializedName("requestingUserId")
    val requestingUserId: Int
)
