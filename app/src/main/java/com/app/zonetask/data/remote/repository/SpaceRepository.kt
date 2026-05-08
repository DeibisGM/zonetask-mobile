package com.app.zonetask.data.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.toDomain
import com.app.zonetask.data.remote.service.SpaceApiService
import com.app.zonetask.domain.model.Space

class SpaceRepository(
    private val apiService: SpaceApiService
) {

    suspend fun getSpacesByUser(userId: Int): ApiResult<List<Space>> {
        return try {
            val response = apiService.getSpacesByUser(userId)

            if (response.isSuccessful) {
                val spaces = response.body()
                    ?.map { it.toDomain() }
                    ?: emptyList()

                ApiResult.Success(spaces)
            } else {
                ApiResult.Error(
                    message    = "Error ${response.code()}",
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Error desconocido")
        }
    }

    suspend fun getSpaceById(spaceId: Int): ApiResult<Space> {
        return try {
            val response = apiService.getSpaceById(spaceId)

            if (response.isSuccessful) {
                val space = response.body()?.toDomain()
                    ?: return ApiResult.Error(message = "Espacio no encontrado")

                ApiResult.Success(space)
            } else {
                ApiResult.Error(
                    message    = "Error ${response.code()}",
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Error desconocido")
        }
    }
}
