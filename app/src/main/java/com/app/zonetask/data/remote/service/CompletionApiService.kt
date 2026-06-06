package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.CompletedTaskHistoryResponse
import com.app.zonetask.data.remote.dto.PagedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CompletionApiService {

    @GET(AppConstants.Api.Paths.COMPLETED_TASKS)
    suspend fun getCompletedTasks(
        @Path("spaceId") spaceId: Int,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("date_from") dateFrom: String?,
        @Query("date_to") dateTo: String?,
        @Query("user_id") userId: Int?,
        @Query("zone_id") zoneId: Int?
    ): Response<PagedResponse<CompletedTaskHistoryResponse>>
}
