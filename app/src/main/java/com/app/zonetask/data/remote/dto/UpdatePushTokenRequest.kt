package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdatePushTokenRequest(
    @SerializedName("token_cfm")
    val tokenCfm: String
)
