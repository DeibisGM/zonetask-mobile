package com.app.zonetask.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.UserMessages
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppIconTint
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

private const val ROLE_OWNER  = "owner"
private const val ROLE_ADMIN  = "admin"

@Composable
fun SpaceCard(
    space: Space,
    userRole: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDeleting: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onDeleteNotAllowed: (() -> Unit)? = null
) {
    val isOwner    = userRole == ROLE_OWNER
    val canDelete  = isOwner   

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    if (showConfirmDialog) {
        DeleteSpaceDialog(
            spaceName = space.name,
            onConfirm = {
                showConfirmDialog = false
                onDelete?.invoke()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        border    = BorderStroke(1.dp, AppBorder),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = space.name,
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier    = Modifier
                            .size(36.dp)
                            .padding(8.dp),
                        color       = MaterialTheme.colorScheme.error,
                        strokeWidth = 2.dp
                    )
                } else if (canDelete) {
                    IconButton(
                        onClick  = { showConfirmDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.DeleteOutline,
                            contentDescription = UserMessages.Spaces.DELETE_CONFIRM,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick  = { onDeleteNotAllowed?.invoke() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Block,
                            contentDescription = UserMessages.Spaces.DELETE_NOT_OWNER,
                            tint               = AppSecondaryText,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }
            }

            space.description?.let { desc ->
                Text(
                    text  = desc,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpaceInfoRow(
                    icon = {
                        Icon(
                            imageVector        = Icons.Outlined.Category,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = AppIconTint
                        )
                    },
                    text = UserMessages.Spaces.TYPE_PREFIX + space.spaceType
                )

                val (chipLabel, chipIcon, chipColor) = when (userRole) {
                    ROLE_OWNER -> Triple(
                        UserMessages.Spaces.ROLE_OWNER,
                        Icons.Outlined.Star,
                        AppPrimary
                    )
                    ROLE_ADMIN -> Triple(
                        UserMessages.Spaces.ROLE_ADMIN,
                        Icons.Outlined.Shield,
                        AppPrimary.copy(alpha = 0.75f)
                    )
                    else -> Triple(
                        UserMessages.Spaces.ROLE_MEMBER,
                        Icons.Outlined.Person,
                        AppSecondaryText
                    )
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text  = chipLabel,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector        = chipIcon,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor          = chipColor.copy(alpha = 0.1f),
                        labelColor              = chipColor,
                        leadingIconContentColor = chipColor
                    ),
                    border = null
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
                text  = UserMessages.Spaces.DELETE_CONFIRM,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text  = "\"$spaceName\" ${UserMessages.Spaces.DELETE_CONFIRM_BODY}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppSecondaryText
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text  = UserMessages.Spaces.DELETE_CONFIRM_ACTION,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = UserMessages.Spaces.DELETE_CANCEL_ACTION,
                    color = AppPrimary
                )
            }
        }
    )
}

@Composable
private fun SpaceInfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text  = text,
            color = AppSecondaryText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
