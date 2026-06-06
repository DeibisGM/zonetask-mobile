package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.SpaceStatisticsResponse
import com.app.zonetask.data.remote.dto.UserStatisticsResponse
import com.app.zonetask.data.remote.service.StatisticsApiService
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class StatisticsRepository(
    private val apiService: StatisticsApiService
) {

    suspend fun getUserStatistics(
        spaceId: Int,
        userId: Int,
        period: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): ApiResult<UserStatisticsResponse> {
        return try {
            val response = apiService.getUserStatistics(
                spaceId  = spaceId,
                userId   = userId,
                period   = period,
                dateFrom = dateFrom,
                dateTo   = dateTo
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return ApiResult.Error("Empty response from server")
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message    = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    suspend fun getSpaceStatistics(
        spaceId: Int,
        period: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): ApiResult<SpaceStatisticsResponse> {
        return try {
            val response = apiService.getSpaceStatistics(
                spaceId  = spaceId,
                period   = period,
                dateFrom = dateFrom,
                dateTo   = dateTo
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return ApiResult.Error("Empty response from server")
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message    = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        400 -> "Invalid date range"
        401 -> "Session expired, please log in again"
        404 -> "Space not found"
        500 -> "Internal server error"
        502, 503 -> "Service unavailable, try again later"
        else -> "Server error ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException   -> "No internet connection"
        is SocketTimeoutException -> "Server took too long to respond"
        is IOException            -> "Network error, check your connection"
        else                      -> e.message ?: "Unknown error"
    }
}
