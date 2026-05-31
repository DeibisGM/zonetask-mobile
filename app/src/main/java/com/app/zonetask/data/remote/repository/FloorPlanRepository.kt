package com.app.zonetask.data.repository

import com.app.zonetask.data.remote.ApiErrorHandler
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateFloorPlanRequest
import com.app.zonetask.data.remote.dto.UpdateFloorPlanRequest
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.FloorPlanApiService
import com.app.zonetask.domain.model.FloorPlan

class FloorPlanRepository(
    private val apiService: FloorPlanApiService
) {
    suspend fun getPlansBySpace(spaceId: Int): ApiResult<List<FloorPlan>> {
        return try {
            val response = apiService.getPlansBySpace(spaceId)
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.map { it.toDomain() }.orEmpty())
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun getPlanById(planId: Int): ApiResult<FloorPlan> {
        return try {
            val response = apiService.getPlanById(planId)
            if (response.isSuccessful) {
                val plan = response.body()?.toDomain()
                    ?: return ApiResult.Error("Plano no encontrado")
                ApiResult.Success(plan)
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun createPlan(request: CreateFloorPlanRequest): ApiResult<FloorPlan> {
        return try {
            val response = apiService.createPlan(request)
            if (response.isSuccessful) {
                val plan = response.body()?.toDomain()
                    ?: return ApiResult.Error("Error al crear el plano")
                ApiResult.Success(plan)
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }

    suspend fun updatePlan(planId: Int, request: UpdateFloorPlanRequest): ApiResult<FloorPlan> {
        return try {
            val response = apiService.updatePlan(planId, request)
            if (response.isSuccessful) {
                val plan = response.body()?.toDomain()
                    ?: return ApiResult.Error("Error al actualizar el plano")
                ApiResult.Success(plan)
            } else {
                ApiResult.Error(
                    message = ApiErrorHandler.fromHttpCode(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(ApiErrorHandler.fromException(e))
        }
    }
}
