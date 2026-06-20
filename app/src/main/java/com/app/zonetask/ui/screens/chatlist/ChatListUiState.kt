package com.app.zonetask.ui.screens.chatlist

import com.app.zonetask.data.remote.dto.ChatGroupResponse

data class ChatListUiState(
    val chats: List<ChatGroupResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)