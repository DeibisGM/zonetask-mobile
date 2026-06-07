package com.app.zonetask.data.auth

import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.FirebaseMessagingTokenProvider
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.AuthResponse
import com.app.zonetask.data.remote.dto.LoginRequest
import com.app.zonetask.data.remote.dto.RegisterRequest
import com.app.zonetask.data.remote.dto.RegisterResponse
import com.app.zonetask.data.remote.service.AuthApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.json.JSONObject

class BackendAuthRepository(
    private val authApiService: AuthApiService
) {

    // Bridges the login screen with the backend and persists the returned session locally.
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        return try {
            val tokenCfm = FirebaseMessagingTokenProvider.getToken()
            val response = authApiService.login(
                LoginRequest(
                    email = email,
                    password = password,
                    tokenCfm = tokenCfm,
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
                    message = serverErrorMessage(response) ?: httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    // Creates a Firebase-backed account and returns the local profile for the new user.
    suspend fun register(request: RegisterRequest): ApiResult<RegisterResponse> {
        return try {
            val tokenCfm = FirebaseMessagingTokenProvider.getToken()
            val response = authApiService.register(
                request.copy(tokenCfm = tokenCfm)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.user == null || body.user.userId <= 0) {
                    ApiResult.Error(message = "El servidor no devolvió el usuario registrado.")
                } else {
                    ApiResult.Success(body)
                }
            } else {
                ApiResult.Error(
                    message = serverErrorMessage(response) ?: httpErrorMessage(response.code()),
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

    private fun serverErrorMessage(response: retrofit2.Response<*>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null

        return runCatching {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
