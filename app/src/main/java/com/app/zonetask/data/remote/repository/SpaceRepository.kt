package com.app.zonetask.data.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateSpaceRequest
import com.app.zonetask.data.remote.dto.EditSpaceRequest
import com.app.zonetask.data.remote.dto.SpacePermissionsResponse
import com.app.zonetask.data.remote.dto.UpdateMemberRoleRequest
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.SpaceApiService
import com.app.zonetask.domain.model.Space
import com.app.zonetask.domain.model.SpaceMember
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SpaceRepository(
    private val apiService: SpaceApiService
) {
    suspend fun getSpacesByUser(userId: Int): ApiResult<List<Space>> {
        return try {
            val response = apiService.getSpacesByUser(userId)
            if (response.isSuccessful) {
                val spaces = response.body()?.map { it.toDomain() }.orEmpty()
                ApiResult.Success(spaces)
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
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
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun createSpace(request: CreateSpaceRequest): ApiResult<Space> {
        return try {
            val response = apiService.createSpace(request)
            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error(message = "Error al crear el espacio")
                ApiResult.Success(space)
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    suspend fun deleteSpace(spaceId: Int, userId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteSpace(spaceId, userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
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
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun getSpaceMembers(spaceId: Int, userId: Int): ApiResult<List<SpaceMember>> {
        return try {
            val response = apiService.getSpaceMembers(spaceId, userId)
            if (response.isSuccessful) {
                val members = response.body()?.map { it.toDomain() }.orEmpty()
                ApiResult.Success(members)
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
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
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(networkErrorMessage(e))
        }
    }

    suspend fun updateSpace(spaceId: Int, request: EditSpaceRequest): ApiResult<Space> {
        return try {
            val response = apiService.updateSpace(spaceId, request)
            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error(message = "Error al actualizar el espacio")
                ApiResult.Success(space)
            } else {
                ApiResult.Error(message = httpErrorMessage(response.code()), statusCode = response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }
    private fun httpErrorMessage(code: Int): String = when (code) {
        401 -> "Sesión expirada, vuelve a iniciar sesión"
        403 -> "No tienes permisos para esto"
        404 -> "Recurso no encontrado"
        500 -> "Error interno del servidor"
        502, 503 -> "Servicio no disponible, intenta más tarde"
        else -> "Error del servidor ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException -> "Sin conexión a internet"
        is SocketTimeoutException -> "El servidor tardó demasiado en responder"
        is IOException -> "Error de red, verifica tu conexión"
        else -> "Error inesperado"
    }
}
