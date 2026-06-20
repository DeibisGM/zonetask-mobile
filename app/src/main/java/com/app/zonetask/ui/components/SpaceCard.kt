package com.app.zonetask.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.UserMessages
import com.app.zonetask.domain.model.Space
import com.app.zonetask.domain.model.SpaceRole
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SpaceCard(
    space: Space,
    // SpaceRole tipado — sin strings sueltos, el compilador verifica exhaustividad
    userRole: SpaceRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // canDelete viene calculado desde UiState.deletableSpaceIds — la Card no deriva lógica
    canDelete: Boolean = false,
    isDeleting: Boolean = false,
    onDeleteConfirmed: (() -> Unit)? = null,
    onDeleteNotAllowed: (() -> Unit)? = null
) {
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    if (showConfirmDialog) {
        DeleteSpaceDialog(
            spaceName = space.name,
            onConfirm = {
                showConfirmDialog = false
                onDeleteConfirmed?.invoke()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = AppPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = space.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppPrimary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = space.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = space.description?.takeIf { it.isNotBlank() } ?: "Tap to open this space",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }

                when {
                    isDeleting -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp
                        )
                    }

                    canDelete -> {
                        IconButton(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = UserMessages.Spaces.DELETE_CONFIRM,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = "Open space",
                                tint = AppSecondaryText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = listOf(
                        space.spaceType,
                        when (userRole) {
                            SpaceRole.OWNER -> UserMessages.Spaces.ROLE_OWNER
                            SpaceRole.ADMIN -> UserMessages.Spaces.ROLE_ADMIN
                            SpaceRole.MEMBER -> UserMessages.Spaces.ROLE_MEMBER
                        }
                    ).joinToString(" · "),
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Open",
                    color = AppPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteSpaceDialog(
    spaceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = UserMessages.Spaces.DELETE_CONFIRM,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "\"$spaceName\" ${UserMessages.Spaces.DELETE_CONFIRM_BODY}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppSecondaryText
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = UserMessages.Spaces.DELETE_CONFIRM_ACTION,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = UserMessages.Spaces.DELETE_CANCEL_ACTION,
                    color = AppPrimary
                )
            }
        }
    )
}
