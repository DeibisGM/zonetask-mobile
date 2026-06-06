package com.app.zonetask.data.remote.repository

import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateTaskRequestDto
import com.app.zonetask.data.remote.dto.MarkTaskCompletionRequestDto
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

    suspend fun updateTask(taskId: Int, request: CreateTaskRequestDto): ApiResult<Unit> {
        return try {
            val response = apiService.updateTask(taskId, request)

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

    suspend fun deleteTask(taskId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteTask(taskId)

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

    suspend fun getTaskById(taskId: Int): ApiResult<TaskResponse> {
        return try {
            val response = apiService.getTaskById(taskId)

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return ApiResult.Error(message = "Couldn't load the task")

                ApiResult.Success(body)
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

    suspend fun completeTaskAssignment(
        assignmentId: Int,
        request: MarkTaskCompletionRequestDto
    ): ApiResult<Unit> {
        return try {
            // The endpoint returns 204 when the completion row is stored for this assignment.
            val response = apiService.completeTaskAssignment(assignmentId, request)

            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                // Keep the backend message when available; ownership and duplicate errors are useful to show.
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
            ApiResult.Error(message = networkErrorMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        401 -> "Session expired, please sign in again"
        404 -> "Resource not found"
        500 -> "Internal server error"
        502, 503 -> "Service unavailable, try again later"
        else -> "Server error ($code)"
    }

    private fun networkErrorMessage(e: Exception): String = when (e) {
        is UnknownHostException -> "No internet connection"
        is SocketTimeoutException -> "The server took too long to respond"
        is IOException -> "Network error, check your connection"
        else -> e.message ?: "Unknown error"
    }
}
