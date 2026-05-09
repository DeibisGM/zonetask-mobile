package com.app.zonetask.data.remote.dto

data class CreateSpaceRequest(
    val name         : String,
    val description  : String? = null,
    val spaceType    : String,
    val coverImageUrl: String? = null,
    val ownerId      : Int
)

