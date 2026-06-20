package com.app.zonetask.ui.screens.chat

import com.app.zonetask.data.remote.dto.ChatMessageDto

data class ChatUiState(
    val chatId: Int = 0,
    val spaceName: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<ChatMessageDto> = emptyList(),
    val isMessagesLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val isSending: Boolean = false,
    val sendError: String? = null
)