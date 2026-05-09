package com.app.zonetask.data.remote.dto

data class EditSpaceRequest(
    val name         : String? = null,
    val description  : String? = null,
    val spaceType    : String? = null,
    val coverImageUrl: String? = null,
    val isActive     : Boolean? = null
)