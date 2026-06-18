package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.ChatGroupResponse
import com.app.zonetask.data.remote.dto.UpdateChatGroupRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ChatApiService {

    @GET(AppConstants.Api.Paths.CHAT_BY_SPACE)
    suspend fun getChat(
        @Path("spaceId") spaceId: Int
    ): Response<ChatGroupResponse>

    @PATCH(AppConstants.Api.Paths.CHAT_BY_SPACE)
    suspend fun updateChat(
        @Path("spaceId") spaceId: Int,
        @Body request: UpdateChatGroupRequest
    ): Response<ChatGroupResponse>

    @Multipart
    @POST(AppConstants.Api.Paths.CHAT_IMAGE)
    suspend fun uploadChatImage(
        @Path("spaceId") spaceId: Int,
        @Part image: MultipartBody.Part
    ): Response<ChatGroupResponse>
}