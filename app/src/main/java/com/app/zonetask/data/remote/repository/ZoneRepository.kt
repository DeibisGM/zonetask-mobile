package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiErrorHandler
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.ZoneRequest
import com.app.zonetask.data.remote.dto.ZoneResponse
import com.app.zonetask.data.remote.dto.toDraft
import com.app.zonetask.data.remote.dto.toRequest
import com.app.zonetask.data.remote.service.ZoneApiService
import com.app.zonetask.ui.screens.plan.PlanZoneDraft

class ZoneRepository(
    private val apiService: ZoneApiService
) {
    suspend fun getZonesByPlan(planId: Int): ApiResult<List<PlanZoneDraft>> {
        return try {
            val response = apiService.getZonesByPlan(planId)
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.map { it.toDraft() }.orEmpty())
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

    suspend fun createZone(planId: Int, draft: PlanZoneDraft, displayOrder: Int): ApiResult<PlanZoneDraft> {
        return createZone(planId, draft.toRequest(displayOrder))
    }

    suspend fun updateZone(zoneId: Int, draft: PlanZoneDraft, displayOrder: Int): ApiResult<PlanZoneDraft> {
        return updateZone(zoneId, draft.toRequest(displayOrder))
    }

    suspend fun deleteZone(zoneId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteZone(zoneId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
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

    suspend fun syncZones(planId: Int, drafts: List<PlanZoneDraft>): ApiResult<List<PlanZoneDraft>> {
        val currentResult = getZonesByPlan(planId)
        if (currentResult is ApiResult.Error) {
            return currentResult
        }

        val currentZones = (currentResult as ApiResult.Success).data
        val currentById = currentZones.mapNotNull { zone ->
            zone.backendId?.let { backendId -> backendId to zone }
        }.toMap()
        val keptIds = mutableSetOf<Int>()
        val savedZones = mutableListOf<PlanZoneDraft>()

        drafts.forEachIndexed { index, draft ->
            val displayOrder = index
            val result = when (val backendId = draft.backendId) {
                null -> createZone(planId, draft.toRequest(displayOrder))
                else -> updateZone(backendId, draft.toRequest(displayOrder))
            }

            when (result) {
                is ApiResult.Success -> {
                    val saved = result.data
                    if (saved.backendId != null) {
                        keptIds += saved.backendId
                    }
                    savedZones += saved.copy(id = draft.id)
                }
                is ApiResult.Error -> return result
            }
        }

        currentById.keys
            .filterNot { keptIds.contains(it) }
            .forEach { staleId ->
                when (val deleteResult = deleteZone(staleId)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Error -> return deleteResult
                }
            }

        return ApiResult.Success(savedZones)
    }

    private suspend fun createZone(planId: Int, request: ZoneRequest): ApiResult<PlanZoneDraft> {
        return try {
            val response = apiService.createZone(planId, request)
            if (response.isSuccessful) {
                val zone = response.body()?.toDraft()
                    ?: return ApiResult.Error("No se pudo guardar una zona")
                ApiResult.Success(zone)
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

    private suspend fun updateZone(zoneId: Int, request: ZoneRequest): ApiResult<PlanZoneDraft> {
        return try {
            val response = apiService.updateZone(zoneId, request)
            if (response.isSuccessful) {
                val zone = response.body()?.toDraft()
                    ?: return ApiResult.Error("No se pudo actualizar una zona")
                ApiResult.Success(zone)
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
