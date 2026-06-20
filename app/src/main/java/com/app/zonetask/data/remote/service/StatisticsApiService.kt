package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.OverdueTrendsResponse
import com.app.zonetask.data.remote.dto.SpaceStatisticsResponse
import com.app.zonetask.data.remote.dto.SpaceUserReportsResponse
import com.app.zonetask.data.remote.dto.UserSpaceReportsResponse
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

    @GET(AppConstants.Api.Paths.USER_REPORTS)
    suspend fun getUserReports(
        @Path("spaceId") spaceId: Int,
        @Query("period") period: String?,
        @Query("date_from") dateFrom: String?,
        @Query("date_to") dateTo: String?,
        @Query("sort_by") sortBy: String?
    ): Response<SpaceUserReportsResponse>

    @GET(AppConstants.Api.Paths.SPACE_REPORTS)
    suspend fun getSpaceReports(
        @Path("userId") userId: Int,
        @Query("period") period: String?,
        @Query("date_from") dateFrom: String?,
        @Query("date_to") dateTo: String?,
        @Query("sort_by") sortBy: String?
    ): Response<UserSpaceReportsResponse>

    @GET(AppConstants.Api.Paths.OVERDUE_TRENDS)
    suspend fun getOverdueTrends(
        @Path("spaceId") spaceId: Int,
        @Query("period") period: String?,
        @Query("date_from") dateFrom: String?,
        @Query("date_to") dateTo: String?,
        @Query("interval") interval: String?
    ): Response<OverdueTrendsResponse>
}
