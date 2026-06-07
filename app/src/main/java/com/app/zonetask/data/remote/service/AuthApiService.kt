package com.app.zonetask.data.remote.service

import com.app.zonetask.data.remote.dto.AuthResponse
import com.app.zonetask.data.remote.dto.LoginRequest
import com.app.zonetask.data.remote.dto.RegisterRequest
import com.app.zonetask.data.remote.dto.RegisterResponse
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
}
