package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import com.app.zonetask.data.remote.dto.TaskAssignmentResponse
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.data.remote.service.TaskApiService
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

class TaskRepository(
    private val apiService: TaskApiService
) {

    // Send the task payload and convert the result to a UI-friendly state.
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

    suspend fun getTasksBySpace(spaceId: Int): ApiResult<List<TaskResponse>> {
        return try {
            val response = apiService.getTasksBySpace(spaceId)

            if (response.isSuccessful) {
                ApiResult.Success(response.body().orEmpty())
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    suspend fun getTasksByZone(zoneId: Int): ApiResult<List<TaskResponse>> {
        return try {
            val response = apiService.getTasksByZone(zoneId)

            if (response.isSuccessful) {
                ApiResult.Success(response.body().orEmpty())
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    suspend fun getTaskAssignments(taskId: Int): ApiResult<List<TaskAssignmentResponse>> {
        return try {
            val response = apiService.getTaskAssignments(taskId)

            if (response.isSuccessful) {
                ApiResult.Success(response.body().orEmpty())
            } else {
                ApiResult.Error(
                    message = httpErrorMessage(response.code()),
                    statusCode = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        401 -> "Sesión expirada, vuelve a iniciar sesión"
        404 -> "Recurso no encontrado"
        500 -> "Error interno del servidor"
        502, 503 -> "Servicio no disponible, intenta más tarde"
        else -> "Error del servidor ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException -> "Sin conexión a internet"
        is SocketTimeoutException -> "El servidor tardó demasiado en responder"
        is IOException -> "Error de red, verifica tu conexión"
        else -> e.message ?: "Error desconocido"
    }
}
