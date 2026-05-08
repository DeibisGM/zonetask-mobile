package com.app.zonetask.domain.model

data class Space(
    val spaceId: Int,
    val name: String,
    val description: String?,
    val spaceType: String,
    val coverImageUrl: String?,
    val isActive: Boolean,
    val ownerId: Int
)