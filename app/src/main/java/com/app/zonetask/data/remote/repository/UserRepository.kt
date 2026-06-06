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

    private fun httpErrorMessage(code: Int): String = when (code) {
        401 -> "Session expired, please sign in again"
        404 -> "Resource not found"
        500 -> "Internal server error"
        502, 503 -> "Service unavailable, try again later"
        else -> "Server error ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException -> "No internet connection"
        is SocketTimeoutException -> "The server took too long to respond"
        is IOException -> "Network error, check your connection"
        else -> "Unexpected error"
    }
}
