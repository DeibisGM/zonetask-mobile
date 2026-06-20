package com.app.zonetask.ui.screens.chat

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.repository.ChatGroupRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatGroupRepository: ChatGroupRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _scrollToBottomEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToBottomEvent: SharedFlow<Unit> = _scrollToBottomEvent.asSharedFlow()

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
                    _scrollToBottomEvent.tryEmit(Unit)
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

    fun openImageViewer(url: String) {
        _uiState.value = _uiState.value.copy(viewingImageUrl = url)
    }

    fun closeImageViewer() {
        _uiState.value = _uiState.value.copy(viewingImageUrl = null)
    }

    fun selectImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(pendingImageUri = uri)
    }

    fun clearPendingImage() {
        _uiState.value = _uiState.value.copy(pendingImageUri = null)
    }

    fun sendImageMessage(contentResolver: ContentResolver) {
        val uri = _uiState.value.pendingImageUri ?: return
        if (_uiState.value.isUploadingImage) return
        _uiState.value = _uiState.value.copy(isUploadingImage = true)
        viewModelScope.launch {
            when (val result = chatGroupRepository.uploadMessageImage(spaceId, userId, uri, contentResolver)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        pendingImageUri  = null,
                        messages         = _uiState.value.messages + result.data
                    )
                    _scrollToBottomEvent.tryEmit(Unit)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isUploadingImage = false)
                }
            }
        }
    }

    fun loadMoreMessages() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        _uiState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            val nextPage = state.currentPage + 1
            when (val result = chatGroupRepository.getMessages(spaceId, userId, page = nextPage)) {
                is ApiResult.Success -> {
                    val older = result.data.items.reversed()
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        messages      = older + _uiState.value.messages,
                        currentPage   = nextPage,
                        hasMore       = result.data.hasNextPage
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
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
        _uiState.value = _uiState.value.copy(
            isMessagesLoading = true,
            currentPage       = 1,
            hasMore           = false
        )
        viewModelScope.launch {
            when (val result = chatGroupRepository.getMessages(spaceId, userId, page = 1)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isMessagesLoading = false,
                        messages          = result.data.items.reversed(),
                        currentPage       = 1,
                        hasMore           = result.data.hasNextPage
                    )
                    _scrollToBottomEvent.tryEmit(Unit)
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