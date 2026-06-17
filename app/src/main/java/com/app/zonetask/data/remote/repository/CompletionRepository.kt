package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CompletedTaskHistoryResponse
import com.app.zonetask.data.remote.dto.PagedResponse
import com.app.zonetask.data.remote.service.CompletionApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CompletionRepository(
    private val apiService: CompletionApiService
) {

    suspend fun getCompletedTasks(
        spaceId: Int,
        page: Int = 1,
        limit: Int = 20,
        dateFrom: String? = null,
        dateTo: String? = null,
        userId: Int? = null,
        zoneId: Int? = null
    ): ApiResult<PagedResponse<CompletedTaskHistoryResponse>> {
        return try {
            val response = apiService.getCompletedTasks(
                spaceId = spaceId,
                page = page,
                limit = limit,
                dateFrom = dateFrom,
                dateTo = dateTo,
                userId = userId,
                zoneId = zoneId
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return ApiResult.Error("Empty response from server")
                ApiResult.Success(body)
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
        401 -> "Session expired, please log in again"
        404 -> "Space not found"
        500 -> "Internal server error"
        502, 503 -> "Service unavailable, try again later"
        else -> "Server error ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException -> "No internet connection"
        is SocketTimeoutException -> "Server took too long to respond"
        is IOException -> "Network error, check your connection"
        else -> e.message ?: "Unknown error"
    }
}
