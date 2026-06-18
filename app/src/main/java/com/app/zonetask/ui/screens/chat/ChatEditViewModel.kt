package com.app.zonetask.ui.screens.chat

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.UpdateChatGroupRequest
import com.app.zonetask.data.remote.repository.ChatGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatEditViewModel(
    private val chatGroupRepository: ChatGroupRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatEditUiState())
    val uiState: StateFlow<ChatEditUiState> = _uiState.asStateFlow()

    init {
        loadChat()
        loadMembers()
    }

    fun onNameChange(value: String)        { _uiState.value = _uiState.value.copy(name = value, errorBanner = null) }
    fun onDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(description = value, errorBanner = null) }
    fun onImageSelected(uri: Uri)          { _uiState.value = _uiState.value.copy(selectedImageUri = uri, errorBanner = null) }

    fun loadChat() {
        _uiState.value = _uiState.value.copy(isLoadingData = true, errorBanner = null)
        viewModelScope.launch {
            when (val result = chatGroupRepository.getChat(spaceId, userId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingData = false,
                        name          = result.data.name,
                        description   = result.data.description ?: "",
                        imageUrl      = result.data.imageUrl ?: ""
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingData = false,
                        errorBanner   = result.message
                    )
                }
            }
        }
    }

    fun loadMembers() {
        _uiState.value = _uiState.value.copy(isMembersLoading = true)
        viewModelScope.launch {
            when (val result = chatGroupRepository.getChatMembers(spaceId, userId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isMembersLoading = false,
                    members          = result.data
                )
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(isMembersLoading = false)
            }
        }
    }

    fun saveChanges(contentResolver: ContentResolver) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorBanner = "El nombre del chat no puede estar vacío.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorBanner = null)

        viewModelScope.launch {
            // 1. Upload image only if a new one was picked AND hasn't been uploaded yet in a previous attempt.
            if (_uiState.value.selectedImageUri != null) {
                when (val r = chatGroupRepository.uploadChatImage(
                    spaceId, userId, _uiState.value.selectedImageUri!!, contentResolver
                )) {
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorBanner = r.message)
                        return@launch
                    }
                    is ApiResult.Success -> {
                        // Clear the URI so a retry on PATCH failure won't re-upload the same image.
                        _uiState.value = _uiState.value.copy(selectedImageUri = null)
                    }
                }
            }

            // 2. Update name and description.
            val request = UpdateChatGroupRequest(
                name        = _uiState.value.name.trim(),
                description = _uiState.value.description.trim().ifBlank { null },
                imageUrl    = null
            )
            when (val r = chatGroupRepository.updateChat(spaceId, userId, request)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(isLoading = false, errorBanner = r.message)
            }
        }
    }
}

class ChatEditViewModelFactory(
    private val chatGroupRepository: ChatGroupRepository,
    private val spaceId: Int,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatEditViewModel(chatGroupRepository = chatGroupRepository, spaceId = spaceId, userId = userId) as T
}