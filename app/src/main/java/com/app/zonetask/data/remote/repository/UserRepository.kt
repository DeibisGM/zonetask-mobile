package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.UserResponse
import com.app.zonetask.data.remote.service.UserApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UserRepository(
    private val apiService: UserApiService
) {

    suspend fun getUsers(): ApiResult<List<UserResponse>> {
        return try {
            val response = apiService.getUsers()

            if (response.isSuccessful) {
                ApiResult.Success(response.body().orEmpty())
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

    suspend fun resolveUserIdByEmail(email: String): ApiResult<Int> {
        return when (val result = getUsers()) {
            is ApiResult.Success -> {
                val normalizedEmail = email.trim().lowercase()
                val matchedUser = result.data.firstOrNull { user ->
                    user.email.trim().lowercase() == normalizedEmail
                } ?: result.data.firstOrNull { user ->
                    user.username.trim().lowercase() == normalizedEmail
                }

                if (matchedUser != null) {
                    ApiResult.Success(matchedUser.userId)
                } else {
                    ApiResult.Error(message = "No se encontró un usuario asociado a ese correo.")
                }
            }

            is ApiResult.Error -> result
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        401 -> "Sesión expirada, vuelve a iniciar sesión"
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
