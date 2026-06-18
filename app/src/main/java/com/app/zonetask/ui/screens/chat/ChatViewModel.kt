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
    private val spaceId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChat()
    }

    fun reload() {
        loadChat()
    }

    private fun loadChat() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = chatGroupRepository.getChat(spaceId)) {
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
}

class ChatViewModelFactory(
    private val chatGroupRepository: ChatGroupRepository,
    private val spaceId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(chatGroupRepository = chatGroupRepository, spaceId = spaceId) as T
}