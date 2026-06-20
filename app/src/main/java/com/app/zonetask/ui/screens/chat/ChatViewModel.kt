package com.app.zonetask.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.repository.ChatGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatGroupRepository: ChatGroupRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChat()
        loadMessages()
    }

    fun reload() {
        loadChat()
        loadMessages()
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSending = true, sendError = null)
        viewModelScope.launch {
            when (val result = chatGroupRepository.sendMessage(spaceId, trimmed, userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages  = _uiState.value.messages + result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        sendError = result.message
                    )
                }
            }
        }
    }

    private fun loadChat() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = chatGroupRepository.getChat(spaceId, userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading   = false,
                        chatId      = result.data.chatId,
                        spaceName   = result.data.name,
                        description = result.data.description,
                        imageUrl    = result.data.imageUrl
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun loadMessages() {
        _uiState.value = _uiState.value.copy(isMessagesLoading = true)
        viewModelScope.launch {
            when (val result = chatGroupRepository.getMessages(spaceId, userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isMessagesLoading = false,
                        messages          = result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isMessagesLoading = false)
                }
            }
        }
    }
}

class ChatViewModelFactory(
    private val chatGroupRepository: ChatGroupRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(chatGroupRepository = chatGroupRepository, spaceId = spaceId, userId = userId) as T
}