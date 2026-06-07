package com.app.zonetask.data.remote.service

import com.app.zonetask.data.remote.dto.AuthResponse
import com.app.zonetask.data.remote.dto.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
}
