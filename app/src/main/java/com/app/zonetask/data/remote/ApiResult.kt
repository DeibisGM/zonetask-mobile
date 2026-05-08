package com.app.zonetask.data.remote

sealed class ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>()

    data class Error(
        val message: String,
        val statusCode: Int? = null
    ) : ApiResult<Nothing>()
}