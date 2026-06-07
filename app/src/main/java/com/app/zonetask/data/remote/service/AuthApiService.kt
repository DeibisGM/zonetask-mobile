package com.app.zonetask.data.remote.service

import com.app.zonetask.data.remote.dto.AuthResponse
import com.app.zonetask.data.remote.dto.ForgotPasswordRequest
import com.app.zonetask.data.remote.dto.ForgotPasswordResponse
import com.app.zonetask.data.remote.dto.LoginRequest
import com.app.zonetask.data.remote.dto.RegisterRequest
import com.app.zonetask.data.remote.dto.RegisterResponse
import com.app.zonetask.data.remote.dto.ResetPasswordRequest
import com.app.zonetask.data.remote.dto.ResetPasswordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // Backend auth endpoint used by the mobile login flow.
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    // Requests the Firebase password reset email for the provided address.
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>

    // Confirms the reset code and applies the new password in Firebase.
    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>
}
