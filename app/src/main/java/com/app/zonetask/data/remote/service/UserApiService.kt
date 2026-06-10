package com.app.zonetask.data.remote.service

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import com.app.zonetask.data.remote.dto.UserResponse
import com.app.zonetask.data.remote.dto.UpdateUserProfileRequest
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApiService {

    @GET("api/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: Int
    ): Response<UserResponse>

    @PUT("api/users/{userId}")
    suspend fun updateUserById(
        @Path("userId") userId: Int,
        @Body body: UpdateUserProfileRequest
    ): Response<UserResponse>

    @Multipart
    @retrofit2.http.POST("api/users/{userId}/profile-picture")
    suspend fun uploadProfilePicture(
        @Path("userId") userId: Int,
        @Part file: MultipartBody.Part
    ): Response<UserResponse>
}
