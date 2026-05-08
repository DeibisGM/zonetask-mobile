package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import com.app.zonetask.data.remote.service.TaskApiService
import java.io.IOException

class TaskRepository(
    private val apiService: TaskApiService
) {

    suspend fun createTask(request: CreateTaskRequestDto): ApiResult<Unit> {
        return try {
            val response = apiService.createTask(request)

            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (_: IOException) {
                    null
                }

                ApiResult.Error(
                    message = errorBody ?: "Error ${response.code()}",
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Error desconocido")
        }
    }
}
