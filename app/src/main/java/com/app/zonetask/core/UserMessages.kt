package com.app.zonetask.core

object UserMessages {

    const val TAP_TO_RETRY_SUFFIX = " Toca para reintentar."

    object Spaces {
        const val LOADING      = "Cargando espacios..."
        const val EMPTY        = "No tenés espacios aún"
        const val TYPE_PREFIX  = "Tipo: "
        const val OWNER_PREFIX = "Propietario ID: "
    }

    object SpaceDetail {
        const val TITLE          = "Detalle del espacio"
        const val TYPE_LABEL     = "Tipo"
        const val OWNER_LABEL    = "Propietario ID"
        const val DESC_LABEL     = "Descripción"
        const val NO_DESCRIPTION = "Sin descripción"
    }

    object Screens {
        const val SPACES_TITLE = "Mis espacios"
    }

    object Accessibility {
        const val BACK   = "Volver"
        const val LOGOUT = "Cerrar sesión"
    }

    object Navigation {
        const val LOGIN_SUCCESS_SNACKBAR = "Sesión iniciada correctamente"
    }
}