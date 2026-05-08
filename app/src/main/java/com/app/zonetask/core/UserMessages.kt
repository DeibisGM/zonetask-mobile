package com.app.zonetask.core

object UserMessages {

    const val TAP_TO_RETRY_SUFFIX = " Toca para reintentar."

    object Spaces {
        const val LOADING      = "Cargando espacios..."
        const val EMPTY        = "No tenés espacios aún"
        const val TYPE_PREFIX  = "Tipo: "
        const val OWNER_PREFIX = "Owner ID: "
    }

    object SpaceDetail {
        const val TITLE          = "Detalle del espacio"
        const val TYPE_LABEL     = "Tipo"
        const val OWNER_LABEL    = "Owner ID"
        const val DESC_LABEL     = "Descripción"
        const val NO_DESCRIPTION = "Sin descripción"
    }

    object Screens {
        const val SPACES_TITLE = "Mis espacios"
        const val CREATE_TASK_TITLE = "Crear tarea"
    }

    object TaskCreate {
        const val INTRO = "Completa el formulario para definir la tarea."
        const val GENERAL_SECTION = "Datos generales"
        const val SCHEDULE_SECTION = "Programacion"
        const val RULES_SECTION = "Reglas"
        const val RELATIONS_SECTION = "Relaciones opcionales"
        const val TITLE_LABEL = "Nombre de la tarea"
        const val DESCRIPTION_LABEL = "Descripcion"
        const val FREQUENCY_LABEL = "Frecuencia"
        const val TIME_LABEL = "Hora"
        const val START_DATE_LABEL = "Fecha de inicio"
        const val END_DATE_LABEL = "Fecha de fin"
        const val ESTIMATED_MINUTES_LABEL = "Minutos estimados"
        const val TARGET_LEVEL_LABEL = "Nivel de objetivo"
        const val SPACE_ID_LABEL = "ID de espacio"
        const val CREATED_BY_LABEL = "Creado por"
        const val CATEGORY_ID_LABEL = "ID de categoria"
        const val ZONE_ID_LABEL = "ID de zona"
        const val OBJECT_ID_LABEL = "ID de objeto"
        const val REMINDER_MINUTES_LABEL = "Minutos antes del recordatorio"
        const val SAVE_BUTTON = "GUARDAR"
        const val SAVE_SNACKBAR = "Vista de formulario lista para guardar."
        const val ACTIVE_LABEL = "Tarea activa"
        const val REMINDER_LABEL = "Recordatorio"
        const val REQUIRE_DESCRIPTION_LABEL = "Requerir descripcion"
        const val REQUIRE_PROOF_LABEL = "Requerir evidencia"
        const val ROTATING_LABEL = "Rotativa"
    }

    object Accessibility {
        const val BACK   = "Volver"
        const val LOGOUT = "Cerrar sesión"
    }

    object Navigation {
        const val LOGIN_SUCCESS_SNACKBAR = "Sesión iniciada correctamente"
    }
}
