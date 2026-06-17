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
        const val OWNER_LABEL    = "ID del propietario"
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
        const val ROLE_UPDATE_SUCCESS    = "Rol actualizado"
        const val ROLE_UPDATE_ERROR      = "No se pudo actualizar el rol"
        const val ROLE_UPDATE_FORBIDDEN  = "No tienes permiso para cambiar este rol"
        const val CANNOT_CHANGE_OWNER    = "El rol de propietario no se puede cambiar"
        const val CANNOT_CHANGE_ADMIN    = "Un administrador no puede cambiar el rol de otro administrador"
        const val SELECT_NEW_ROLE        = "Seleccionar nuevo rol"
        const val ACTION_VIEW_SPACE         = "Ver espacio"
        const val ACTION_EDIT_SPACE         = "Editar información del espacio"
        const val ACTION_DELETE_SPACE       = "Eliminar espacio"
        const val ACTION_INVITE_MEMBERS     = "Invitar miembros"
        const val ACTION_REMOVE_MEMBERS     = "Eliminar miembros"
        const val ACTION_CHANGE_MEMBER_ROLE = "Cambiar rol de miembro"
        const val ACTION_VIEW_PERMISSIONS   = "Ver permisos"
        const val ACTION_MANAGE_ZONES       = "Gestionar zonas"
        const val ACTION_MANAGE_TASKS       = "Gestionar tareas"
    }

    object Screens {
        const val SPACES_TITLE      = "Mis espacios"
        const val CREATE_TASK_TITLE = "Crear tarea"
        const val TASKS_TITLE       = "Tareas"
    }

    object TaskCreate {
        const val INTRO                      = "Completá el formulario para definir la tarea."
        const val GENERAL_SECTION            = "General"
        const val SCHEDULE_SECTION           = "Horario"
        const val RULES_SECTION              = "Reglas"
        const val RELATIONS_SECTION          = "Relaciones opcionales"
        const val TITLE_LABEL                = "Nombre de la tarea"
        const val DESCRIPTION_LABEL          = "Descripción"
        const val FREQUENCY_LABEL            = "Frecuencia"
        const val TIME_LABEL                 = "Hora"
        const val START_DATE_LABEL           = "Fecha de inicio"
        const val END_DATE_LABEL             = "Fecha de fin"
        const val ESTIMATED_MINUTES_LABEL    = "Tiempo estimado"
        const val TARGET_LEVEL_LABEL         = "Nivel objetivo"
        const val SPACE_ID_LABEL             = "ID del espacio"
        const val CREATED_BY_LABEL           = "Creado por"
        const val CATEGORY_ID_LABEL          = "ID de categoría"
        const val ZONE_ID_LABEL              = "ID de zona"
        const val CATEGORY_LABEL             = "Categoría"
        const val ZONE_LABEL                 = "Zona"
        const val OBJECT_ID_LABEL            = "ID de objeto"
        const val REMINDER_MINUTES_LABEL     = "Recordatorio"
        const val SAVE_BUTTON                = "Guardar"
        const val SAVE_SNACKBAR              = "Formulario listo para guardar."
        const val ACTIVE_LABEL               = "Tarea activa"
        const val REMINDER_LABEL             = "Recordatorio"
        const val REQUIRE_DESCRIPTION_LABEL  = "Requiere descripción"
        const val REQUIRE_PROOF_LABEL        = "Requiere comprobante"
        const val ROTATING_LABEL             = "Rotativa"
    }

    object Accessibility {
        const val BACK   = "Volver"
        const val LOGOUT = "Cerrar sesión"
    }

    object Navigation {
        const val LOGIN_SUCCESS_SNACKBAR = "Inicio de sesión exitoso"
    }

    object Login {
        const val TITLE = "ZoneTask"
        const val SUBTITLE = "Inicia sesión con tu correo y contraseña."
        const val EMAIL_LABEL = "Correo"
        const val EMAIL_PLACEHOLDER = "correo@ejemplo.com"
        const val PASSWORD_LABEL = "Contraseña"
        const val PASSWORD_PLACEHOLDER = "Escribe tu contraseña"
        const val SUBMIT = "Ingresar"
        const val FORGOT_PASSWORD = "¿Olvidaste tu contraseña?"
        const val EMAIL_REQUIRED = "Ingresa tu correo."
        const val EMAIL_INVALID = "Correo no válido."
        const val PASSWORD_REQUIRED = "Ingresa tu contraseña."
        const val REGISTRATION_NOTICE = "Tu cuenta se registró. Verifica tu correo para activarla."
        const val PASSWORD_RESET_NOTICE = "Tu contraseña se actualizó. Ya puedes iniciar sesión."
    }

    object Register {
        const val TITLE = "Crear cuenta"
        const val SUBTITLE = "Regístrate para usar ZoneTask."
        const val ACCOUNT_SECTION = "Datos de la cuenta"
        const val ACCOUNT_SUBTITLE = "Información básica para identificar tu perfil."
        const val CREDENTIALS_SECTION = "Credenciales"
        const val CREDENTIALS_SUBTITLE = "Datos para iniciar sesión."
        const val OPTIONAL_SECTION = "Información opcional"
        const val OPTIONAL_SUBTITLE = "Puedes completar estos datos después en tu perfil."
        const val FINISH_SECTION = "Finalizar"
        const val FINISH_SUBTITLE = "Crea tu cuenta y verifica el correo para activarla."
        const val USERNAME_LABEL = "Usuario"
        const val USERNAME_PLACEHOLDER = "Escribe tu usuario"
        const val FIRST_NAME_LABEL = "Nombre"
        const val FIRST_NAME_PLACEHOLDER = "Tu nombre"
        const val LAST_NAME_LABEL = "Apellido"
        const val LAST_NAME_PLACEHOLDER = "Tu apellido"
        const val GENDER_LABEL = "Género"
        const val GENDER_SELECT = "Selecciona tu género"
        const val GENDER_MALE = "Masculino"
        const val GENDER_FEMALE = "Femenino"
        const val GENDER_NON_BINARY = "No binario"
        const val GENDER_NOT_SAY = "Prefiero no decirlo"
        const val PHONE_LABEL = "Teléfono"
        const val PHONE_PLACEHOLDER = "Opcional"
        const val BIO_LABEL = "Biografía"
        const val BIO_PLACEHOLDER = "Cuéntanos un poco sobre ti"
        const val CONFIRM_PASSWORD_LABEL = "Confirmar contraseña"
        const val CONFIRM_PASSWORD_PLACEHOLDER = "Repite la contraseña"
        const val SUBMIT = "Registrarme"
        const val BACK_TO_LOGIN = "Ya tengo cuenta"
        const val USERNAME_REQUIRED = "Ingresa un usuario."
        const val FIRST_NAME_REQUIRED = "Ingresa tu nombre."
        const val EMAIL_REQUIRED = "Ingresa tu correo."
        const val EMAIL_INVALID = "Correo no válido."
        const val PASSWORD_REQUIRED = "Ingresa una contraseña."
        const val PASSWORD_MIN_LENGTH = "La contraseña debe tener al menos 8 caracteres."
        const val PASSWORDS_DONT_MATCH = "Las contraseñas no coinciden."
        const val REGISTRATION_PENDING = "Revisa tu correo para verificar tu cuenta."
        const val REGISTRATION_FAILED_VERIFICATION = "Se creó la cuenta, pero no se pudo enviar el correo de verificación."
        const val USERNAME_TOO_SHORT = "El usuario debe tener al menos 3 caracteres."
    }

    object PasswordReset {
        const val REQUEST_TITLE = "Recuperar contraseña"
        const val REQUEST_SUBTITLE = "Te enviaremos un correo de recuperación."
        const val REQUEST_BUTTON = "Enviar correo"
        const val REQUEST_SUCCESS = "Revisa tu correo y sigue el enlace para cambiar tu contraseña."
        const val REQUEST_BACK_TO_LOGIN = "Volver al inicio de sesión"
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
