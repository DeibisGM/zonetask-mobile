package com.app.zonetask.navigation.spaces

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.spaces.CreateSpaceScreen
import com.app.zonetask.ui.screens.spaces.EditSpaceScreen
import com.app.zonetask.ui.screens.spaces.SpaceDetailScreen
import com.app.zonetask.ui.screens.spaces.SpacePermissionsScreen
import com.app.zonetask.ui.screens.invitations.InviteMemberScreen
import com.app.zonetask.ui.screens.spaces.SpacesScreen

fun NavGraphBuilder.spacesNavGraph(
    currentUserId: Int,
    rootSnackbarHostState: SnackbarHostState,
    actions: SpacesNavActions,
    onTabSelected: (NavDestination) -> Unit
) {

    // Spaces list
    composable(
        route = SpacesDestinations.LIST,
        arguments = listOf(navArgument(SpacesDestinations.ARG_USER_ID) {
            type = NavType.IntType
        })
    ) { backStackEntry ->
        val userId = backStackEntry.arguments
            ?.getInt(SpacesDestinations.ARG_USER_ID)
            ?: currentUserId

        val successMessage by backStackEntry.savedStateHandle
            .getStateFlow<String?>(SpacesNavKeys.SUCCESS_MESSAGE, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(successMessage) {
            successMessage?.let { message ->
                rootSnackbarHostState.showSnackbar(message)
                backStackEntry.savedStateHandle[SpacesNavKeys.SUCCESS_MESSAGE] = null
            }
        }

        ZoneTaskScaffold(
            title = UserMessages.Screens.SPACES_TITLE,
            showBack = false,
            onBackClick = {},
            currentDestination = NavDestination.SPACES,
            onDestinationSelected = onTabSelected,
            snackbarHostState = rootSnackbarHostState,
            onAddClick = actions.onOpenCreate
        ) { padding ->
            SpacesScreen(
                snackbarHostState = rootSnackbarHostState,
                userId = userId,
                modifier = Modifier.padding(padding),
                reloadTrigger = successMessage != null,
                onSuccessMessageShown = {
                    backStackEntry.savedStateHandle[SpacesNavKeys.SUCCESS_MESSAGE] = null
                },
                onSpaceClick = { space -> actions.onOpenDetail(space.spaceId) },
                onOpenInvitations = actions.onOpenInvitations
            )
        }
    }

    // Create space
    composable(route = SpacesDestinations.CREATE) {
        ZoneTaskScaffold(
            title = "Crear un nuevo espacio",
            showBack = true,
            onBackClick = actions.onBack,
            snackbarHostState = rootSnackbarHostState
        ) { padding ->
            CreateSpaceScreen(
                ownerId = currentUserId,
                modifier = Modifier.padding(padding),
                onSaved = { message -> actions.onSpaceCreated(message) }
            )
        }
    }

    // Space detail
    composable(
        route = SpacesDestinations.DETAIL,
        arguments = listOf(navArgument(SpacesDestinations.ARG_SPACE_ID) {
            type = NavType.IntType
        })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(SpacesDestinations.ARG_SPACE_ID)
            ?: return@composable

        val refreshDetail by backStackEntry.savedStateHandle
            .getStateFlow(SpacesNavKeys.REFRESH_DETAIL, false)
            .collectAsStateWithLifecycle()
        val taskChanged by backStackEntry.savedStateHandle
            .getStateFlow("taskChanged", false)
            .collectAsStateWithLifecycle()

        val detailSnackbarHostState = remember { SnackbarHostState() }

        ZoneTaskScaffold(
            title = UserMessages.SpaceDetail.TITLE,
            showBack = true,
            onBackClick = actions.onBack,
            snackbarHostState = detailSnackbarHostState
        ) { padding ->
            SpaceDetailScreen(
                spaceId = spaceId,
                userId = currentUserId,
                modifier = Modifier.padding(padding),
                refreshTrigger = refreshDetail || taskChanged,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle[SpacesNavKeys.REFRESH_DETAIL] = false
                    backStackEntry.savedStateHandle["taskChanged"] = false
                },
                onNavigateToPermissions = actions.onOpenPermissions,
                onCreateTaskClick = { actions.onCreateTaskForSpace(spaceId) },
                onEditClick = actions.onOpenEdit,
                onDeleteSuccess = { actions.onSpaceDeleted("Espacio eliminado") } // navigate to list with message
            )
        }
    }

    // Edit space
    composable(
        route = SpacesDestinations.EDIT,
        arguments = listOf(navArgument(SpacesDestinations.ARG_SPACE_ID) {
            type = NavType.IntType
        })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(SpacesDestinations.ARG_SPACE_ID)
            ?: return@composable

        val editSnackbarHostState = remember { SnackbarHostState() }

        ZoneTaskScaffold(
            title = "Editar espacio",
            showBack = true,
            onBackClick = actions.onBack,
            snackbarHostState = editSnackbarHostState
        ) { padding ->
            EditSpaceScreen(
                spaceId = spaceId,
                modifier = Modifier.padding(padding),
                onSaved = { message -> actions.onSpaceEdited(message) }
            )
        }
    }

    // Space permissions
    composable(
        route = SpacesDestinations.PERMISSIONS,
        arguments = listOf(navArgument(SpacesDestinations.ARG_SPACE_ID) {
            type = NavType.IntType
        })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(SpacesDestinations.ARG_SPACE_ID)
            ?: return@composable

        val permissionsSnackbarHostState = remember { SnackbarHostState() }

        ZoneTaskScaffold(
            title = UserMessages.SpacePermissions.TITLE,
            showBack = true,
            onBackClick = actions.onBack,
            snackbarHostState = permissionsSnackbarHostState
        ) { padding ->
            SpacePermissionsScreen(
                spaceId = spaceId,
                userId = currentUserId,
                snackbarHostState = permissionsSnackbarHostState,
                onInviteClick = { actions.onOpenInvite(spaceId) },
                modifier = Modifier.padding(padding)
            )
        }
    }

    // Invite member
    composable(
        route = SpacesDestinations.INVITE,
        arguments = listOf(navArgument(SpacesDestinations.ARG_SPACE_ID) {
            type = NavType.IntType
        })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getInt(SpacesDestinations.ARG_SPACE_ID)
            ?: return@composable

        val inviteSnackbarHostState = remember { SnackbarHostState() }

        ZoneTaskScaffold(
            title = UserMessages.Invitations.TITLE,
            showBack = true,
            onBackClick = actions.onBack,
            snackbarHostState = inviteSnackbarHostState
        ) { padding ->
            InviteMemberScreen(
                spaceId = spaceId,
                invitedBy = currentUserId,
                snackbarHostState = inviteSnackbarHostState,
                modifier = Modifier.padding(padding)
            )
        }
    }
}