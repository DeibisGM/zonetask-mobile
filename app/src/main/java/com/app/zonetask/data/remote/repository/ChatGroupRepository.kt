package com.app.zonetask.data.remote.repository

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import com.app.zonetask.data.remote.ApiErrorHandler
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.ChatGroupResponse
import com.app.zonetask.data.remote.dto.ChatMemberDto
import com.app.zonetask.data.remote.dto.UpdateChatGroupRequest
import com.app.zonetask.data.remote.service.ChatApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatGroupRepository(private val apiService: ChatApiService) {

    suspend fun getChat(spaceId: Int, userId: Int): ApiResult<ChatGroupResponse> {
        return try {
            val response = apiService.getChat(spaceId, userId)
            if (response.isSuccessful) {
                val body = response.body() ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message    = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun getChatMembers(spaceId: Int, userId: Int): ApiResult<List<ChatMemberDto>> {
        return try {
            val response = apiService.getChatMembers(spaceId, userId)
            if (response.isSuccessful) {
                val body = response.body() ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message    = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun updateChat(spaceId: Int, userId: Int, request: UpdateChatGroupRequest): ApiResult<ChatGroupResponse> {
        return try {
            val response = apiService.updateChat(spaceId, userId, request)
            if (response.isSuccessful) {
                val body = response.body() ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message    = ApiErrorHandler.bodyMessage(response.errorBody()) ?: ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun uploadChatImage(
        spaceId: Int,
        userId: Int,
        imageUri: Uri,
        contentResolver: ContentResolver
    ): ApiResult<ChatGroupResponse> {
        return try {
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val ext      = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val bytes    = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return ApiResult.Error("No se pudo leer la imagen seleccionada")

            val requestBody = bytes.toRequestBody(mimeType.toMediaType())
            val part        = MultipartBody.Part.createFormData("file", "chat_image.$ext", requestBody)

            val response = apiService.uploadChatImage(spaceId, userId, part)
            if (response.isSuccessful) {
                val body = response.body() ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message    = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }
}