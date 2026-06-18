package com.app.zonetask.ui.screens.chat

import android.net.Uri

data class ChatEditUiState(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val selectedImageUri: Uri? = null,
    val isLoadingData: Boolean = false,
    val isLoading: Boolean = false,
    val errorBanner: String? = null,
    val isSuccess: Boolean = false
)