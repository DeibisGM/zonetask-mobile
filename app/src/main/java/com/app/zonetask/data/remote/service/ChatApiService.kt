package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.ChatGroupResponse
import com.app.zonetask.data.remote.dto.ChatMemberDto
import com.app.zonetask.data.remote.dto.ChatMessageDto
import com.app.zonetask.data.remote.dto.PagedMessagesDto
import com.app.zonetask.data.remote.dto.SendMessageRequest
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
import retrofit2.http.Query

interface ChatApiService {

    @GET(AppConstants.Api.Paths.CHAT_BY_SPACE)
    suspend fun getChat(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int
    ): Response<ChatGroupResponse>

    @GET(AppConstants.Api.Paths.CHAT_MEMBERS)
    suspend fun getChatMembers(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int
    ): Response<List<ChatMemberDto>>

    @PATCH(AppConstants.Api.Paths.CHAT_BY_SPACE)
    suspend fun updateChat(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int,
        @Body request: UpdateChatGroupRequest
    ): Response<ChatGroupResponse>

    @Multipart
    @POST(AppConstants.Api.Paths.CHAT_IMAGE)
    suspend fun uploadChatImage(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int,
        @Part image: MultipartBody.Part
    ): Response<ChatGroupResponse>

    @GET(AppConstants.Api.Paths.SPACE_MESSAGES)
    suspend fun getMessages(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<PagedMessagesDto>

    @POST(AppConstants.Api.Paths.SPACE_MESSAGES)
    suspend fun sendMessage(
        @Path("spaceId") spaceId: Int,
        @Body request: SendMessageRequest
    ): Response<ChatMessageDto>

    @Multipart
    @POST(AppConstants.Api.Paths.SPACE_MESSAGES_UPLOAD)
    suspend fun uploadMessageImage(
        @Path("spaceId") spaceId: Int,
        @Query("senderId") senderId: Int,
        @Part image: MultipartBody.Part
    ): Response<ChatMessageDto>
}