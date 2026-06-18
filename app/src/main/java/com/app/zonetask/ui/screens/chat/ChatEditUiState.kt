package com.app.zonetask.ui.screens.chat

import android.net.Uri
import com.app.zonetask.data.remote.dto.ChatMemberDto

data class ChatEditUiState(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val selectedImageUri: Uri? = null,
    val isLoadingData: Boolean = false,
    val isLoading: Boolean = false,
    val errorBanner: String? = null,
    val isSuccess: Boolean = false,
    val members: List<ChatMemberDto> = emptyList(),
    val isMembersLoading: Boolean = false
)