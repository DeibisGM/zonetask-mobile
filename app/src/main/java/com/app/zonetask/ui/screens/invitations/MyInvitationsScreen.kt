package com.app.zonetask.ui.screens.invitations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.InvitationStatus
import com.app.zonetask.domain.model.SpaceInvitation
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun MyInvitationsScreen(
    userId: Int,
    email: String,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: MyInvitationsViewModel = viewModel(
        factory = MyInvitationsViewModelFactory(
            invitationRepository = AppContainer.invitationRepository,
            userId               = userId,
            email                = email
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyInvitationsEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    when {
        uiState.isLoading && uiState.invitations.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text  = UserMessages.Invitations.MY_LOADING,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        uiState.errorBanner != null && uiState.invitations.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    TextButton(onClick = { viewModel.loadInvitations() }) {
                        Text(
                            text  = UserMessages.TAP_TO_RETRY_SUFFIX.trim(),
                            color = AppPrimary
                        )
                    }
                }
            }
        }

        uiState.invitations.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text  = UserMessages.Invitations.MY_EMPTY,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            LazyColumn(
                modifier       = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.invitations,
                    key   = { it.invitationId }
                ) { invitation ->
                    InvitationCard(
                        invitation     = invitation,
                        isResponding   = uiState.respondingInvitationId == invitation.invitationId,
                        actionsEnabled = uiState.respondingInvitationId == null,
                        onAccept       = { viewModel.respond(invitation.invitationId, accepted = true) },
                        onReject       = { viewModel.respond(invitation.invitationId, accepted = false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: SpaceInvitation,
    isResponding: Boolean,
    actionsEnabled: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border   = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = UserMessages.Invitations.CARD_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status = invitation.status)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text  = "${UserMessages.Invitations.SPACE_REF_PREFIX}${invitation.spaceName ?: invitation.spaceId}",
                style = MaterialTheme.typography.labelLarge,
                color = AppPrimary
            )

            invitation.message?.takeIf { it.isNotBlank() }?.let { message ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppPrimary.copy(alpha = 0.06f)
                ) {
                    Text(
                        text     = "${UserMessages.Invitations.MESSAGE_PREFIX}$message",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Only pending invitations are respondable. Any other status is
            // terminal (the backend rejects a second response), so no actions.
            if (invitation.status == InvitationStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = AppBorder)
                Spacer(Modifier.height(4.dp))
                if (isResponding) {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = AppPrimary,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick  = onReject,
                            enabled  = actionsEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text  = UserMessages.Invitations.REJECT_BUTTON,
                                color = AppError
                            )
                        }
                        Button(
                            onClick  = onAccept,
                            enabled  = actionsEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text  = UserMessages.Invitations.ACCEPT_BUTTON,
                                color = AppOnPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: InvitationStatus) {
    val (label, tint) = statusLabelAndColor(status)
    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.3f))
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelMedium,
            color    = tint,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// Status -> localized label + color, mapped in the UI so the domain enum stays
// presentation-free (same approach as RoleBadge in SpacePermissionsScreen).
private fun statusLabelAndColor(status: InvitationStatus): Pair<String, Color> = when (status) {
    InvitationStatus.PENDING   -> UserMessages.Invitations.STATUS_PENDING   to AppPrimary
    InvitationStatus.ACCEPTED  -> UserMessages.Invitations.STATUS_ACCEPTED  to AppPrimary
    InvitationStatus.REJECTED  -> UserMessages.Invitations.STATUS_REJECTED  to AppError
    InvitationStatus.EXPIRED   -> UserMessages.Invitations.STATUS_EXPIRED   to AppSecondaryText
    InvitationStatus.CANCELLED -> UserMessages.Invitations.STATUS_CANCELLED to AppSecondaryText
}
