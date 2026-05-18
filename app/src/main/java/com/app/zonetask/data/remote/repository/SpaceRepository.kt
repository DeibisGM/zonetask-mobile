package com.app.zonetask.data.repository

import com.app.zonetask.data.remote.ApiErrorHandler
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateSpaceRequest
import com.app.zonetask.data.remote.dto.EditSpaceRequest
import com.app.zonetask.data.remote.dto.SpacePermissionsResponse
import com.app.zonetask.data.remote.dto.UpdateMemberRoleRequest
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.SpaceApiService
import com.app.zonetask.domain.model.Space
import com.app.zonetask.domain.model.SpaceMember

class SpaceRepository(
    private val apiService: SpaceApiService
) {
    suspend fun getSpacesByUser(userId: Int): ApiResult<List<Space>> {
        return try {
            val response = apiService.getSpacesByUser(userId)
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

    suspend fun getSpaceById(spaceId: Int): ApiResult<Space> {
        return try {
            val response = apiService.getSpaceById(spaceId)
            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error("Espacio no encontrado")
                ApiResult.Success(space)
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

    suspend fun createSpace(request: CreateSpaceRequest): ApiResult<Space> {
        return try {
            val response = apiService.createSpace(request)
            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error("Error al crear el espacio")
                ApiResult.Success(space)
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

    suspend fun deleteSpace(spaceId: Int, userId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteSpace(spaceId, userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
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

    suspend fun getSpacePermissions(spaceId: Int, userId: Int): ApiResult<SpacePermissionsResponse> {
        return try {
            val response = apiService.getSpacePermissions(spaceId, userId)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(body)
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

    suspend fun getSpaceMembers(spaceId: Int, userId: Int): ApiResult<List<SpaceMember>> {
        return try {
            val response = apiService.getSpaceMembers(spaceId, userId)
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

    suspend fun updateMemberRole(
        spaceId: Int,
        memberId: Int,
        newRole: String,
        requestingUserId: Int
    ): ApiResult<SpaceMember> {
        return try {
            val response = apiService.updateMemberRole(
                spaceId = spaceId,
                memberId = memberId,
                request = UpdateMemberRoleRequest(
                    newRole = newRole,
                    requestingUserId = requestingUserId
                )
            )
            if (response.isSuccessful) {
                val member = response.body()?.toDomain()
                    ?: return ApiResult.Error("Respuesta vacía del servidor")
                ApiResult.Success(member)
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

    suspend fun updateSpace(spaceId: Int, request: EditSpaceRequest): ApiResult<Space> {
        return try {
            val response = apiService.updateSpace(spaceId, request)
            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error("Error al actualizar el espacio")
                ApiResult.Success(space)
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