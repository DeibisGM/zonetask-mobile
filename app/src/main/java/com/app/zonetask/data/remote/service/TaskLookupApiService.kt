package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.TaskFormOptionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TaskLookupApiService {

    @GET(AppConstants.Api.Paths.TASK_FORM_OPTIONS)
    suspend fun getTaskFormOptions(
        @Query("spaceId") spaceId: Int = 1
    ): Response<TaskFormOptionsResponse>
}
