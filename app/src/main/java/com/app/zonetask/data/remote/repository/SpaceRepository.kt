package com.app.zonetask.data.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.SpaceApiService
import com.app.zonetask.domain.model.Space
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
                val spaces = response.body()
                    ?.map { it.toDomain() }
                    ?: emptyList()

                ApiResult.Success(spaces)
            } else {
                ApiResult.Error(
                    message    = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    suspend fun getSpaceById(spaceId: Int): ApiResult<Space> {
        return try {
            val response = apiService.getSpaceById(spaceId)

            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error(message = "Espacio no encontrado")

                ApiResult.Success(space)
            } else {
                ApiResult.Error(
                    message    = httpErrorMessage(response.code()),
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
                    message    = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        401  -> "Sesión expirada, vuelve a iniciar sesión"
        403  -> "No tienes permisos para esto"
        404  -> "Recurso no encontrado"
        500  -> "Error interno del servidor"
        502,
        503  -> "Servicio no disponible, intenta más tarde"
        else -> "Error del servidor ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException    -> "Sin conexión a internet"
        is SocketTimeoutException  -> "El servidor tardó demasiado en responder"
        is IOException             -> "Error de red, verifica tu conexión"
        else                       -> "Error inesperado"
    }
}