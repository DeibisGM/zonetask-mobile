package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.LookupOptionResponse
import com.app.zonetask.data.remote.dto.TaskFormOptionsResponse
import com.app.zonetask.data.remote.service.TaskLookupApiService

class TaskLookupRepository(
    private val apiService: TaskLookupApiService
) {

    // Wrap the lookup response in a simple success/error result.
    suspend fun getTaskFormOptions(spaceId: Int = 1): ApiResult<TaskFormOptionsResponse> {
        return try {
            val response = apiService.getTaskFormOptions(spaceId)

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return ApiResult.Error(message = "No se pudieron cargar las opciones")

                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message = "Error ${response.code()}",
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Error desconocido")
        }
    }

    // Return only the objects for the selected zone.
    suspend fun getZoneObjects(zoneId: Int): ApiResult<List<LookupOptionResponse>> {
        return try {
            val response = apiService.getZoneObjects(zoneId)

            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    message = "Error ${response.code()}",
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Error desconocido")
        }
    }
}
