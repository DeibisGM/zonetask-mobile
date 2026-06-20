package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiErrorHandler
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.FloorPlanTemplateApiService
import com.app.zonetask.domain.model.FloorPlanTemplate

class FloorPlanTemplateRepository(
    private val apiService: FloorPlanTemplateApiService
) {
    suspend fun getAll(): ApiResult<List<FloorPlanTemplate>> {
        return try {
            val response = apiService.getAll()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.map { it.toDomain() }.orEmpty())
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.bodyMessage(response.errorBody())
                        ?: ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun getById(id: Int): ApiResult<FloorPlanTemplate> {
        return try {
            val response = apiService.getById(id)
            if (response.isSuccessful) {
                val template = response.body()?.toDomain()
                    ?: return ApiResult.Error("Plantilla no encontrada")
                ApiResult.Success(template)
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.bodyMessage(response.errorBody())
                        ?: ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }
}
