package com.app.zonetask.core

object UserMessages {

    const val TAP_TO_RETRY_SUFFIX = " Toca para reintentar."

    object Spaces {
        const val LOADING               = "Cargando espacios..."
        const val EMPTY                 = "No tenés espacios aún"
        const val TYPE_PREFIX           = "Tipo: "
        const val OWNER_PREFIX          = "Propietario ID: "
        const val DELETE_SUCCESS        = "Espacio eliminado correctamente"
        const val DELETE_ERROR          = "No se pudo eliminar el espacio"
        const val DELETE_CONFIRM        = "¿Eliminar este espacio?"
        const val DELETE_CONFIRM_BODY   = "será eliminado permanentemente."
        const val DELETE_CONFIRM_ACTION = "Eliminar"
        const val DELETE_CANCEL_ACTION  = "Cancelar"
        const val DELETE_FORBIDDEN      = "No tienes permiso para eliminar este espacio"
        const val DELETE_NOT_OWNER      = "Solo el propietario puede eliminar este espacio"
        const val ROLE_OWNER            = "Propietario"
        const val ROLE_MEMBER           = "Miembro"
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