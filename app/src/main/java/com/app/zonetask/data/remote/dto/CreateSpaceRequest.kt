package com.app.zonetask.data.remote.dto

data class CreateSpaceRequest(
    val name         : String,
    val description  : String? = null,
    val spaceType    : String,
    val coverImageUrl: String? = null,
    val ownerId      : Int,
    val rotationType : String? = null,
    val requireProof : Boolean = false
)

