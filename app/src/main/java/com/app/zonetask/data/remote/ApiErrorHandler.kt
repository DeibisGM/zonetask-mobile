package com.app.zonetask.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorHandler {

    fun fromHttpCode(code: Int): String = when (code) {
        401  -> "Sesión expirada, vuelve a iniciar sesión"
        403  -> "No tienes permisos para esto"
        404  -> "Recurso no encontrado"
        500  -> "Error interno del servidor"
        502,
        503  -> "Servicio no disponible, intenta más tarde"
        else -> "Error del servidor ($code)"
    }

    fun fromException(e: Exception): String = when (e) {
        is UnknownHostException   -> "Sin conexión a internet"
        is SocketTimeoutException -> "El servidor tardó demasiado en responder"
        is IOException            -> "Error de red, verifica tu conexión"
        else                      -> "Error inesperado"
    }
}
