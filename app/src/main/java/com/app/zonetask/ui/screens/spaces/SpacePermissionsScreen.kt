package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.SpaceMember
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppIconTint
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import kotlinx.coroutines.flow.collectLatest

private val ASSIGNABLE_ROLES = listOf("admin", "member")

@Composable
fun SpacePermissionsScreen(
    spaceId: Int,
    userId: Int,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: SpacePermissionsViewModel = viewModel(
        factory = SpacePermissionsViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId         = spaceId,
            userId          = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SpacePermissionsEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppPrimary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text  = UserMessages.SpacePermissions.LOADING,
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        uiState.errorBanner != null -> {
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
                    TextButton(onClick = { viewModel.loadPermissions() }) {
                        Text(
                            text  = UserMessages.TAP_TO_RETRY_SUFFIX.trim(),
                            color = AppPrimary
                        )
                    }
                }
            }
        }

        else -> {
            SpacePermissionsContent(
                uiState          = uiState,
                onRequestChange  = { member -> viewModel.requestRoleChange(member) },
                modifier         = modifier
            )

            uiState.memberPendingRoleChange?.let { member ->
                RoleChangeDialog(
                    member           = member,
                    requestingRole   = uiState.requestingUserRole,
                    onConfirm        = { newRole -> viewModel.confirmRoleChange(member.memberId, newRole) },
                    onDismiss        = { viewModel.cancelRoleChange() }
                )
            }
        }
    }
}

@Composable
private fun SpacePermissionsContent(
    uiState: SpacePermissionsUiState,
    onRequestChange: (SpaceMember) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SectionHeader(
            icon  = Icons.Outlined.Shield,
            title = UserMessages.SpacePermissions.YOUR_ROLE_LABEL
        )
        RoleBadge(role = uiState.requestingUserRole)

        if (uiState.roleActions.isNotEmpty()) {
            SectionHeader(
                icon  = Icons.Outlined.Check,
                title = UserMessages.SpacePermissions.SECTION_ACTIONS
            )
            RoleActionsCard(roleActions = uiState.roleActions)
        }

        if (uiState.members.isNotEmpty()) {
            SectionHeader(
                icon  = Icons.Outlined.Group,
                title = UserMessages.SpacePermissions.MEMBERS_SECTION
            )
            MembersCard(
                members          = uiState.members,
                requestingRole   = uiState.requestingUserRole,
                updatingMemberId = uiState.updatingMemberId,
                onRequestChange  = onRequestChange
            )
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val label = when (role) {
        "owner"  -> UserMessages.Spaces.ROLE_OWNER
        "admin"  -> UserMessages.Spaces.ROLE_ADMIN
        else     -> UserMessages.Spaces.ROLE_MEMBER
    }
    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = AppPrimary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, AppPrimary.copy(alpha = 0.3f))
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelLarge,
            color    = AppPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RoleActionsCard(roleActions: Map<String, List<String>>) {
    val actionLabels = mapOf(
        "view_space"               to UserMessages.SpacePermissions.ACTION_VIEW_SPACE,
        "edit_space"               to UserMessages.SpacePermissions.ACTION_EDIT_SPACE,
        "delete_space"             to UserMessages.SpacePermissions.ACTION_DELETE_SPACE,
        "invite_members"           to UserMessages.SpacePermissions.ACTION_INVITE_MEMBERS,
        "remove_members"           to UserMessages.SpacePermissions.ACTION_REMOVE_MEMBERS,
        "change_member_role"       to UserMessages.SpacePermissions.ACTION_CHANGE_MEMBER_ROLE,
        "view_permissions_settings" to UserMessages.SpacePermissions.ACTION_VIEW_PERMISSIONS,
        "manage_zones"             to UserMessages.SpacePermissions.ACTION_MANAGE_ZONES,
        "manage_tasks"             to UserMessages.SpacePermissions.ACTION_MANAGE_TASKS
    )

    val allActions = roleActions.values.flatten().distinct()
    val roles      = listOf("owner", "admin", "member")

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = "Acción",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = AppSecondaryText,
                    modifier = Modifier.weight(1f)
                )
                roles.forEach { role ->
                    val roleLabel = when (role) {
                        "owner"  -> "Owner"
                        "admin"  -> "Admin"
                        else     -> "Member"
                    }
                    Text(
                        text      = roleLabel,
                        style     = MaterialTheme.typography.labelSmall,
                        color     = AppSecondaryText,
                        modifier  = Modifier.width(54.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            HorizontalDivider(color = AppBorder)

            allActions.forEachIndexed { index, action ->
                if (index > 0) HorizontalDivider(color = AppBorder.copy(alpha = 0.4f))
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = actionLabels[action] ?: action,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    roles.forEach { role ->
                        val hasAction = roleActions[role]?.contains(action) == true
                        Box(
                            modifier        = Modifier.width(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasAction) {
                                Icon(
                                    imageVector        = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint               = AppPrimary,
                                    modifier           = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text  = "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppSecondaryText.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersCard(
    members: List<SpaceMember>,
    requestingRole: String,
    updatingMemberId: Int?,
    onRequestChange: (SpaceMember) -> Unit
) {
    val canEdit = requestingRole == "owner" || requestingRole == "admin"

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            members.forEachIndexed { index, member ->
                if (index > 0) HorizontalDivider(color = AppBorder.copy(alpha = 0.4f))
                MemberRow(
                    member           = member,
                    requestingRole   = requestingRole,
                    isUpdating       = updatingMemberId == member.memberId,
                    canEdit          = canEdit,
                    onRequestChange  = onRequestChange
                )
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: SpaceMember,
    requestingRole: String,
    isUpdating: Boolean,
    canEdit: Boolean,
    onRequestChange: (SpaceMember) -> Unit
) {
    val roleLabel = when (member.role.lowercase()) {
        "owner"  -> UserMessages.Spaces.ROLE_OWNER
        "admin"  -> UserMessages.Spaces.ROLE_ADMIN
        else     -> UserMessages.Spaces.ROLE_MEMBER
    }

    val isOwner   = member.role.lowercase() == "owner"
    val isAdminEditingAdmin = requestingRole == "admin" && member.role.lowercase() == "admin"
    val showEdit  = canEdit && !isOwner && !isAdminEditingAdmin

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.Person,
            contentDescription = null,
            tint               = AppIconTint,
            modifier           = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "Usuario ${member.userId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = roleLabel,
                style = MaterialTheme.typography.labelSmall,
                color = AppSecondaryText
            )
        }

        when {
            isUpdating -> CircularProgressIndicator(
                modifier  = Modifier.size(20.dp),
                color     = AppPrimary,
                strokeWidth = 2.dp
            )
            showEdit -> IconButton(onClick = { onRequestChange(member) }) {
                Icon(
                    imageVector        = Icons.Outlined.Edit,
                    contentDescription = UserMessages.SpacePermissions.CHANGE_ROLE_TITLE,
                    tint               = AppPrimary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RoleChangeDialog(
    member: SpaceMember,
    requestingRole: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRole by remember(member.memberId) {
        mutableStateOf(member.role.lowercase())
    }

    val availableRoles = ASSIGNABLE_ROLES

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = UserMessages.SpacePermissions.CHANGE_ROLE_TITLE,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = "${UserMessages.SpacePermissions.SELECT_NEW_ROLE} (Usuario ${member.userId})",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppSecondaryText
                )
                Spacer(Modifier.height(8.dp))
                availableRoles.forEach { role ->
                    val label = when (role) {
                        "admin"  -> UserMessages.Spaces.ROLE_ADMIN
                        else     -> UserMessages.Spaces.ROLE_MEMBER
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick  = { selectedRole = role },
                            colors   = RadioButtonDefaults.colors(selectedColor = AppPrimary)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedRole) },
                enabled = selectedRole != member.role.lowercase()
            ) {
                Text(
                    text  = UserMessages.SpacePermissions.CHANGE_ROLE_CONFIRM,
                    color = AppPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = UserMessages.SpacePermissions.CHANGE_ROLE_CANCEL,
                    color = AppSecondaryText
                )
            }
        }
    )
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = AppPrimary,
            modifier           = Modifier.size(18.dp)
        )
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}