package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.domain.model.SpaceRole
import com.app.zonetask.ui.components.SpaceCard
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SpacesScreen(
    snackbarHostState: SnackbarHostState,
    userId: Int,
    modifier: Modifier = Modifier,
    reloadTrigger        : Boolean  = false,
    onSuccessMessageShown: () -> Unit = {},
    onSpaceClick: (Space) -> Unit = {},
    onOpenInvitations: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: SpacesViewModel = viewModel(
        factory = SpacesViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            userId = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger) viewModel.fetchSpaces()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SpacesEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        InvitationsEntry(onClick = onOpenInvitations)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading && uiState.spaces.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = UserMessages.Spaces.LOADING,
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                uiState.errorBanner != null && uiState.spaces.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text      = uiState.errorBanner!!,
                                color     = AppSecondaryText,
                                style     = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            TextButton(onClick = { viewModel.fetchSpaces() }) {
                                Text(
                                    text  = UserMessages.TAP_TO_RETRY_SUFFIX.trim(),
                                    color = AppPrimary
                                )
                            }
                        }
                    }
                }

                uiState.spaces.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text  = UserMessages.Spaces.EMPTY,
                                color = AppSecondaryText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            TextButton(onClick = { viewModel.fetchSpaces() }) {
                                Text(
                                    text  = UserMessages.Spaces.REFRESH,
                                    color = AppPrimary
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.spaces,
                            key   = { it.spaceId }
                        ) { space ->
                            val userRole  = uiState.spaceRoles[space.spaceId] ?: SpaceRole.MEMBER
                            val canDelete = space.spaceId in uiState.deletableSpaceIds

                            SpaceCard(
                                space             = space,
                                userRole          = userRole,
                                canDelete         = canDelete,
                                isDeleting        = uiState.deletingSpaceId == space.spaceId,
                                onDeleteConfirmed  = { viewModel.deleteSpace(space.spaceId) },
                                onDeleteNotAllowed = { viewModel.notifyDeleteNotAllowed() },
                                onClick            = { onSpaceClick(space) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationsEntry(onClick: () -> Unit) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surface,
        border   = BorderStroke(1.dp, AppBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.MailOutline,
                contentDescription = null,
                tint               = AppPrimary,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text     = UserMessages.Invitations.ENTRY_LABEL,
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint               = AppSecondaryText,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}
