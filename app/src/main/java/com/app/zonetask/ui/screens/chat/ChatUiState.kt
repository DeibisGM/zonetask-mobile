package com.app.zonetask.ui.screens.chat

data class ChatUiState(
    val chatId: Int = 0,
    val spaceName: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)