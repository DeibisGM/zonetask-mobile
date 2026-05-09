package com.app.zonetask.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.app.zonetask.domain.model.Space

data class SpaceResponse(
    @SerializedName("spaceId")
    val spaceId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("spaceType")
    val spaceType: String,

    @SerializedName("coverImageUrl")
    val coverImageUrl: String?,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("ownerId")
    val ownerId: Int
)

fun SpaceResponse.toDomain(): Space = Space(
    spaceId       = spaceId,
    name          = name,
    description   = description,
    spaceType     = spaceType,
    coverImageUrl = coverImageUrl,
    isActive      = isActive,
    ownerId       = ownerId
)