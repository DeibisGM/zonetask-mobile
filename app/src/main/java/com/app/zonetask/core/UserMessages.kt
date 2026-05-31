package com.app.zonetask.core

object UserMessages {

    const val TAP_TO_RETRY_SUFFIX = " Toca para reintentar."

    object Spaces {
        const val LOADING               = "Cargando espacios..."
        const val EMPTY                 = "No tenés espacios aún"
        const val REFRESH               = "Refrescar"
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
        const val ROLE_ADMIN            = "Administrador"
        const val ROLE_MEMBER           = "Miembro"
    }

    object SpaceDetail {
        const val TITLE          = "Detalle del espacio"
        const val TYPE_LABEL     = "Tipo"
        const val OWNER_LABEL    = "Propietario ID"
        const val DESC_LABEL     = "Descripción"
        const val NO_DESCRIPTION = "Sin descripción"
        const val PERMISSIONS_BUTTON = "Roles y permisos"
    }

    object SpacePermissions {
        const val TITLE                  = "Roles y permisos"
        const val LOADING                = "Cargando permisos..."
        const val SECTION_ROLES          = "Roles disponibles"
        const val SECTION_ACTIONS        = "Acciones por rol"
        const val YOUR_ROLE_LABEL        = "Tu rol en este espacio"
        const val MEMBERS_SECTION        = "Miembros del espacio"
        const val MEMBER_ROLE_LABEL      = "Rol actual"
        const val CHANGE_ROLE_TITLE      = "Cambiar rol"
        const val CHANGE_ROLE_CONFIRM    = "Guardar"
        const val CHANGE_ROLE_CANCEL     = "Cancelar"
        const val ROLE_UPDATE_SUCCESS    = "Rol actualizado correctamente"
        const val ROLE_UPDATE_ERROR      = "No se pudo actualizar el rol"
        const val ROLE_UPDATE_FORBIDDEN  = "No tienes permiso para cambiar este rol"
        const val CANNOT_CHANGE_OWNER    = "El rol del propietario no puede cambiarse"
        const val CANNOT_CHANGE_ADMIN    = "Un administrador no puede cambiar el rol de otro administrador"
        const val SELECT_NEW_ROLE        = "Selecciona el nuevo rol"
        const val ACTION_VIEW_SPACE         = "Ver el espacio"
        const val ACTION_EDIT_SPACE         = "Editar información del espacio"
        const val ACTION_DELETE_SPACE       = "Eliminar el espacio"
        const val ACTION_INVITE_MEMBERS     = "Invitar miembros"
        const val ACTION_REMOVE_MEMBERS     = "Remover miembros"
        const val ACTION_CHANGE_MEMBER_ROLE = "Cambiar rol de miembros"
        const val ACTION_VIEW_PERMISSIONS   = "Ver configuración de permisos"
        const val ACTION_MANAGE_ZONES       = "Gestionar zonas"
        const val ACTION_MANAGE_TASKS       = "Gestionar tareas"
    }

    object Screens {
        const val SPACES_TITLE = "Mis espacios"
        const val CREATE_TASK_TITLE = "Crear tarea"
        const val TASKS_TITLE = "Tareas"
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
        const val ESTIMATED_MINUTES_LABEL = "Tiempo estimado"
        const val TARGET_LEVEL_LABEL = "Nivel de objetivo"
        const val SPACE_ID_LABEL = "ID de espacio"
        const val CREATED_BY_LABEL = "Creado por"
        const val CATEGORY_ID_LABEL = "ID de categoria"
        const val ZONE_ID_LABEL = "ID de zona"
        const val CATEGORY_LABEL = "Categoria"
        const val ZONE_LABEL = "Zona"
        const val OBJECT_ID_LABEL = "ID de objeto"
        const val REMINDER_MINUTES_LABEL = "Recordar"
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

    object Invitations {
        const val TITLE         = "Invitar miembro"
        const val INTRO         = "Ingresa el correo de la persona que quieres invitar a este espacio."
        const val EMAIL_LABEL   = "Correo electrónico"
        const val MESSAGE_LABEL  = "Mensaje (opcional)"
        const val SEND_BUTTON    = "Enviar invitación"
        const val SEND_SUCCESS   = "Invitación enviada correctamente"
        const val INVALID_EMAIL  = "Ingresa un correo electrónico válido"
        const val NOT_REGISTERED = "Este correo no está registrado en ZoneTask"
        const val FORBIDDEN      = "No tienes permiso para invitar miembros a este espacio"
        const val CONFLICT       = "Esa persona ya fue invitada o ya es miembro del espacio"

        const val MY_TITLE        = "Mis invitaciones"
        const val MY_LOADING      = "Cargando invitaciones..."
        const val MY_EMPTY        = "No tienes invitaciones"
        const val ENTRY_LABEL     = "Mis invitaciones"
        const val CARD_TITLE      = "Invitación a un espacio"
        const val SPACE_REF_PREFIX   = "Espacio: "
        const val MESSAGE_PREFIX     = "Mensaje: "
        const val MISSING_IDENTITY = "No se pudo determinar tu identidad. Vuelve a iniciar sesión."

        const val STATUS_PENDING   = "Pendiente"
        const val STATUS_ACCEPTED  = "Aceptada"
        const val STATUS_REJECTED  = "Rechazada"
        const val STATUS_EXPIRED   = "Expirada"
        const val STATUS_CANCELLED = "Cancelada"

         const val ACCEPT_BUTTON       = "Aceptar"
        const val REJECT_BUTTON       = "Rechazar"
        const val ACCEPT_SUCCESS      = "Invitación aceptada. Ya eres miembro del espacio."
        const val REJECT_SUCCESS      = "Invitación rechazada"
        const val RESPOND_UNAVAILABLE = "La invitación ya no está disponible. Puede haber expirado o ya fue respondida."
        const val RESPOND_FORBIDDEN   = "No puedes responder esta invitación"
        const val RESPOND_NOT_FOUND   = "La invitación ya no existe"
    }
}
