package com.app.zonetask.ui.screens.spaces

data class EditSpaceUiState(
    val spaceId      : Int     = 0,
    val name         : String  = "",
    val description  : String  = "",
    val spaceType    : String  = "",
    val coverImageUrl: String  = "",
    val isLoading    : Boolean = false,
    val isLoadingData: Boolean = true,
    val errorBanner  : String? = null,
    val isSuccess    : Boolean = false
)