package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.CreateInvitationRequest
import com.app.zonetask.data.remote.dto.InvitationResponse
import com.app.zonetask.data.remote.dto.RespondToInvitationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InvitationApiService {

    @POST(AppConstants.Api.Paths.CREATE_INVITATION)
    suspend fun createInvitation(
        @Body request: CreateInvitationRequest
    ): Response<InvitationResponse>

    @GET(AppConstants.Api.Paths.USER_INVITATIONS)
    suspend fun getInvitationsByUser(
        @Path("userId") userId: Int,
        @Query("email") email: String,
        @Query("status") status: String? = null
    ): Response<List<InvitationResponse>>

    @POST(AppConstants.Api.Paths.RESPOND_INVITATION)
    suspend fun respondToInvitation(
        @Path("invitationId") invitationId: Int,
        @Body request: RespondToInvitationRequest
    ): Response<InvitationResponse>
}