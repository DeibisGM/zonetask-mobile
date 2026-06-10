package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.SpaceStatisticsResponse
import com.app.zonetask.data.remote.dto.UserStatisticsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StatisticsApiService {

    @GET(AppConstants.Api.Paths.USER_STATISTICS)
    suspend fun getUserStatistics(
        @Path("spaceId") spaceId: Int,
        @Path("userId") userId: Int,
        @Query("period") period: String?,
        @Query("date_from") dateFrom: String?,
        @Query("date_to") dateTo: String?
    ): Response<UserStatisticsResponse>

    @GET(AppConstants.Api.Paths.SPACE_STATISTICS)
    suspend fun getSpaceStatistics(
        @Path("spaceId") spaceId: Int,
        @Query("period") period: String?,
        @Query("date_from") dateFrom: String?,
        @Query("date_to") dateTo: String?
    ): Response<SpaceStatisticsResponse>
}
