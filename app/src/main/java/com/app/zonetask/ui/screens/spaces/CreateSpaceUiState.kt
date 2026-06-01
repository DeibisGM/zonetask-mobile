package com.app.zonetask.ui.screens.spaces

data class CreateSpaceUiState(
    val name         : String  = "",
    val description  : String  = "",
    val spaceType    : String  = "",
    val coverImageUrl: String  = "",
    val rotationType : String  = "",
    val requireProof : Boolean = false,
    val isLoading    : Boolean = false,
    val errorBanner  : String? = null,
    val isSuccess    : Boolean = false
)