package com.app.zonetask.data.remote.service

import com.app.zonetask.data.remote.dto.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {

    @GET("api/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: Int
    ): Response<UserResponse>
}
