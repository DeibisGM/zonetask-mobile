package com.app.zonetask.data.auth

import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.AuthResponse
import com.app.zonetask.data.remote.dto.LoginRequest
import com.app.zonetask.data.remote.service.AuthApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class BackendAuthRepository(
    private val authApiService: AuthApiService
) {

    // Bridges the login screen with the backend and persists the returned session locally.
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        return try {
            val response = authApiService.login(
                LoginRequest(
                    email = email,
                    password = password,
                    deviceName = "ZoneTask Android",
                    deviceType = "mobile"
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.user == null || body.user.userId <= 0) {
                    ApiResult.Error(message = "El servidor no devolvió el usuario autenticado.")
                } else {
                    AuthSessionStore.save(
                        sessionToken = body.sessionToken,
                        refreshToken = body.refreshToken,
                        user = body.user
                    )
                    ApiResult.Success(body)
                }
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

    fun clearSession() {
        AuthSessionStore.clear()
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        400 -> "Correo o contraseña inválidos."
        401 -> "Credenciales incorrectas."
        404 -> "Usuario no encontrado."
        500 -> "Error interno del servidor."
        502, 503 -> "Servicio no disponible, intenta más tarde."
        else -> "Error del servidor ($code)."
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException -> "Sin conexión a internet."
        is SocketTimeoutException -> "El servidor tardó demasiado en responder."
        is IOException -> "Error de red, verifica tu conexión."
        else -> "Error inesperado."
    }
}
