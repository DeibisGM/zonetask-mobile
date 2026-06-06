package com.app.zonetask.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorHandler {

    fun fromHttpCode(code: Int): String = when (code) {
        401  -> "Session expired, please sign in again"
        403  -> "You don't have permission to do this"
        404  -> "Resource not found"
        500  -> "Internal server error"
        502,
        503  -> "Service unavailable, try again later"
        else -> "Server error ($code)"
    }

    fun fromException(e: Exception): String = when (e) {
        is UnknownHostException   -> "No internet connection"
        is SocketTimeoutException -> "The server took too long to respond"
        is IOException            -> "Network error, check your connection"
        else                      -> "Unexpected error"
    }
}
