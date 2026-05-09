package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import com.app.zonetask.data.remote.dto.TaskAssignmentResponse
import com.app.zonetask.data.remote.dto.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
  import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApiService {

    // Sends the create-task payload to the backend.
    @POST(AppConstants.Api.Paths.TASKS)
    suspend fun createTask(
        @Body request: CreateTaskRequestDto
    ): Response<Void>

    @GET(AppConstants.Api.Paths.TASK_BY_ID)
    suspend fun getTaskById(
        @Path("taskId") taskId: Int
    ): Response<TaskResponse>

    @PUT(AppConstants.Api.Paths.TASK_BY_ID)
    suspend fun updateTask(
        @Path("taskId") taskId: Int,
        @Body request: CreateTaskRequestDto
    ): Response<Void>

    @DELETE(AppConstants.Api.Paths.TASK_BY_ID)
    suspend fun deleteTask(
        @Path("taskId") taskId: Int
    ): Response<Void>

    @GET(AppConstants.Api.Paths.SPACE_TASKS)
    suspend fun getTasksBySpace(
        @Path("spaceId") spaceId: Int
    ): Response<List<TaskResponse>>

    @GET(AppConstants.Api.Paths.ZONE_TASKS)
    suspend fun getTasksByZone(
        @Path("zoneId") zoneId: Int
    ): Response<List<TaskResponse>>

    @GET("api/tasks/{taskId}/assignments")
    suspend fun getTaskAssignments(
        @Path("taskId") taskId: Int
    ): Response<List<TaskAssignmentResponse>>
}
