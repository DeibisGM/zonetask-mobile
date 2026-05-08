package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskApiService {

    @POST(AppConstants.Api.Paths.TASKS)
    suspend fun createTask(
        @Body request: CreateTaskRequestDto
    ): Response<Void>
}
