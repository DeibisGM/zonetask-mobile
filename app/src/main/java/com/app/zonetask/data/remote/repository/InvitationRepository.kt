package com.app.zonetask.data.repository

import com.app.zonetask.data.remote.ApiErrorHandler
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateInvitationRequest
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.InvitationApiService
import com.app.zonetask.domain.model.SpaceInvitation
import com.app.zonetask.data.remote.dto.RespondToInvitationRequest
import com.app.zonetask.domain.model.InvitationStatus

class InvitationRepository(
    private val apiService: InvitationApiService
) {

    suspend fun createInvitation(
        spaceId: Int,
        invitedBy: Int,
        email: String,
        message: String?
    ): ApiResult<SpaceInvitation> {
        return try {
            val response = apiService.createInvitation(
                CreateInvitationRequest(
                    spaceId      = spaceId,
                    invitedBy    = invitedBy,
                    emailInvited = email,
                    message      = message
                )
            )
            if (response.isSuccessful) {
                val invitation = response.body()?.toDomain()
                    ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(invitation)
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.bodyMessage(response.errorBody())
                        ?: ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun getInvitationsByUser(
        userId: Int,
        email: String,
        status: String? = null
    ): ApiResult<List<SpaceInvitation>> {
        return try {
            val response = apiService.getInvitationsByUser(userId, email, status)
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.map { it.toDomain() }.orEmpty())
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun respondToInvitation(
        invitationId: Int,
        accepted: Boolean,
        userId: Int,
        email: String
    ): ApiResult<SpaceInvitation> {
        return try {
            val response = apiService.respondToInvitation(
                invitationId = invitationId,
                request = RespondToInvitationRequest(
                    status = if (accepted) {
                        InvitationStatus.ACCEPTED.wireValue
                    } else {
                        InvitationStatus.REJECTED.wireValue
                    },
                    userId = userId,
                    email  = email
                )
            )
            if (response.isSuccessful) {
                val invitation = response.body()?.toDomain()
                    ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(invitation)
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }
}