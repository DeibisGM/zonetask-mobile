package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SpacePermissionsResponse(
    @SerializedName("roleActions")
    val roleActions: Map<String, List<String>>,

    @SerializedName("requestingUserRole")
    val requestingUserRole: String
)
