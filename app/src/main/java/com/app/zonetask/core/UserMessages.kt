package com.app.zonetask.core

object UserMessages {

    const val TAP_TO_RETRY_SUFFIX = " Toca para reintentar."

    object Spaces {
        const val LOADING               = "Loading spaces..."
        const val EMPTY                 = "No spaces yet"
        const val TYPE_PREFIX           = "Type: "
        const val OWNER_PREFIX          = "Owner ID: "
        const val DELETE_SUCCESS        = "Space deleted"
        const val DELETE_ERROR          = "Could not delete space"
        const val DELETE_CONFIRM        = "Delete this space?"
        const val DELETE_CONFIRM_BODY   = "will be permanently deleted."
        const val DELETE_CONFIRM_ACTION = "Delete"
        const val DELETE_CANCEL_ACTION  = "Cancel"
        const val DELETE_FORBIDDEN      = "You don't have permission to delete this space"
        const val DELETE_NOT_OWNER      = "Only the owner can delete this space"
        const val ROLE_OWNER            = "Owner"
        const val ROLE_ADMIN            = "Admin"
        const val ROLE_MEMBER           = "Member"
    }

    object SpaceDetail {
        const val TITLE          = "Space detail"
        const val TYPE_LABEL     = "Type"
        const val OWNER_LABEL    = "Owner ID"
        const val DESC_LABEL     = "Description"
        const val NO_DESCRIPTION = "No description"
        const val PERMISSIONS_BUTTON = "Roles & permissions"
    }

    object SpacePermissions {
        const val TITLE                  = "Roles & permissions"
        const val LOADING                = "Loading permissions..."
        const val SECTION_ROLES          = "Available roles"
        const val SECTION_ACTIONS        = "Actions by role"
        const val YOUR_ROLE_LABEL        = "Your role in this space"
        const val MEMBERS_SECTION        = "Space members"
        const val MEMBER_ROLE_LABEL      = "Current role"
        const val CHANGE_ROLE_TITLE      = "Change role"
        const val CHANGE_ROLE_CONFIRM    = "Save"
        const val CHANGE_ROLE_CANCEL     = "Cancel"
        const val ROLE_UPDATE_SUCCESS    = "Role updated"
        const val ROLE_UPDATE_ERROR      = "Could not update role"
        const val ROLE_UPDATE_FORBIDDEN  = "You don't have permission to change this role"
        const val CANNOT_CHANGE_OWNER    = "Owner role cannot be changed"
        const val CANNOT_CHANGE_ADMIN    = "An admin cannot change another admin's role"
        const val SELECT_NEW_ROLE        = "Select new role"
        const val ACTION_VIEW_SPACE         = "View space"
        const val ACTION_EDIT_SPACE         = "Edit space info"
        const val ACTION_DELETE_SPACE       = "Delete space"
        const val ACTION_INVITE_MEMBERS     = "Invite members"
        const val ACTION_REMOVE_MEMBERS     = "Remove members"
        const val ACTION_CHANGE_MEMBER_ROLE = "Change member role"
        const val ACTION_VIEW_PERMISSIONS   = "View permissions"
        const val ACTION_MANAGE_ZONES       = "Manage zones"
        const val ACTION_MANAGE_TASKS       = "Manage tasks"
    }

    object Screens {
        const val SPACES_TITLE = "My spaces"
        const val CREATE_TASK_TITLE = "Create task"
        const val TASKS_TITLE = "Tasks"
    }

    object TaskCreate {
        const val INTRO = "Fill in the form to define the task."
        const val GENERAL_SECTION = "General"
        const val SCHEDULE_SECTION = "Schedule"
        const val RULES_SECTION = "Rules"
        const val RELATIONS_SECTION = "Optional relations"
        const val TITLE_LABEL = "Task name"
        const val DESCRIPTION_LABEL = "Description"
        const val FREQUENCY_LABEL = "Frequency"
        const val TIME_LABEL = "Time"
        const val START_DATE_LABEL = "Start date"
        const val END_DATE_LABEL = "End date"
        const val ESTIMATED_MINUTES_LABEL = "Estimated time"
        const val TARGET_LEVEL_LABEL = "Target level"
        const val SPACE_ID_LABEL = "Space ID"
        const val CREATED_BY_LABEL = "Created by"
        const val CATEGORY_ID_LABEL = "Category ID"
        const val ZONE_ID_LABEL = "Zone ID"
        const val CATEGORY_LABEL = "Category"
        const val ZONE_LABEL = "Zone"
        const val OBJECT_ID_LABEL = "Object ID"
        const val REMINDER_MINUTES_LABEL = "Reminder"
        const val SAVE_BUTTON = "Save"
        const val SAVE_SNACKBAR = "Form ready to save."
        const val ACTIVE_LABEL = "Active task"
        const val REMINDER_LABEL = "Reminder"
        const val REQUIRE_DESCRIPTION_LABEL = "Require description"
        const val REQUIRE_PROOF_LABEL = "Require proof"
        const val ROTATING_LABEL = "Rotating"
    }

    object Accessibility {
        const val BACK   = "Back"
        const val LOGOUT = "Log out"
    }

    object Navigation {
        const val LOGIN_SUCCESS_SNACKBAR = "Logged in successfully"
    }

    object Login {
        const val TITLE = "ZoneTask"
        const val SUBTITLE = "Inicia sesión con tu correo y contraseña."
        const val EMAIL_LABEL = "Correo"
        const val EMAIL_PLACEHOLDER = "correo@ejemplo.com"
        const val PASSWORD_LABEL = "Contraseña"
        const val PASSWORD_PLACEHOLDER = "Escribe tu contraseña"
        const val SUBMIT = "Ingresar"
        const val EMAIL_REQUIRED = "Ingresa tu correo."
        const val EMAIL_INVALID = "Correo no válido."
        const val PASSWORD_REQUIRED = "Ingresa tu contraseña."
    }

    object Register {
        const val TITLE = "Crear cuenta"
        const val SUBTITLE = "Regístrate para usar ZoneTask y recibir el correo de verificación."
        const val USERNAME_LABEL = "Usuario"
        const val USERNAME_PLACEHOLDER = "tu_usuario"
        const val FIRST_NAME_LABEL = "Nombre"
        const val FIRST_NAME_PLACEHOLDER = "Tu nombre"
        const val LAST_NAME_LABEL = "Apellido"
        const val LAST_NAME_PLACEHOLDER = "Tu apellido"
        const val GENDER_LABEL = "Género"
        const val GENDER_PLACEHOLDER = "male, female, non_binary, prefer_not_to_say"
        const val EMAIL_LABEL = "Correo"
        const val EMAIL_PLACEHOLDER = "correo@ejemplo.com"
        const val PASSWORD_LABEL = "Contraseña"
        const val PASSWORD_PLACEHOLDER = "Crea una contraseña"
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
    }
}
