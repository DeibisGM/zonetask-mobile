package com.app.zonetask.ui.screens.spacemembers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.zonetask.BuildConfig
import com.app.zonetask.data.remote.dto.SpaceMemberWithUserDto
import com.app.zonetask.data.remote.dto.SpacePendingInvitationDto
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppTopBar

@Composable
fun SpaceMembersScreen(
    spaceId: Int,
    userId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SpaceMembersViewModel = viewModel(
        factory = SpaceMembersViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId         = spaceId,
            userId          = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTopBar)
                .statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint               = MaterialTheme.colorScheme.onSurface,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Text(
                text       = "Members",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)

        // Search bar
        OutlinedTextField(
            value         = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder   = {
                Text("Search by name or role…", color = AppSecondaryText, style = MaterialTheme.typography.bodyMedium)
            },
            leadingIcon   = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = AppSecondaryText, modifier = Modifier.size(20.dp))
            },
            shape   = RoundedCornerShape(12.dp),
            colors  = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = AppCardElevated,
                unfocusedContainerColor = AppCardElevated,
                focusedBorderColor      = AppBorder,
                unfocusedBorderColor    = AppBorder,
                cursorColor             = AppPrimary,
                focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            modifier   = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when {
            uiState.isLoading -> Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(32.dp))
            }

            uiState.errorMessage != null -> Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = uiState.errorMessage!!,
                    color    = AppSecondaryText,
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(24.dp)
                )
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {

                // Active members section
                val activeMembers = uiState.filteredMembers.filter { it.status == "active" }
                if (activeMembers.isNotEmpty()) {
                    item(key = "header_active") {
                        SectionHeader(
                            label = "Members (${activeMembers.size})",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                    items(activeMembers, key = { "member_${it.memberId}" }) { member ->
                        MemberItem(member = member)
                        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)
                    }
                }

                // Inactive members section
                val inactiveMembers = uiState.filteredMembers.filter { it.status != "active" }
                if (inactiveMembers.isNotEmpty()) {
                    item(key = "header_inactive") {
                        SectionHeader(
                            label = "Inactive (${inactiveMembers.size})",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                        )
                    }
                    items(inactiveMembers, key = { "inactive_${it.memberId}" }) { member ->
                        MemberItem(member = member)
                        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)
                    }
                }

                // Pending invitations section (only visible for owner/admin)
                if (uiState.pendingInvitations.isNotEmpty()) {
                    item(key = "header_pending") {
                        SectionHeader(
                            label = "Pending Invitations (${uiState.pendingInvitations.size})",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                        )
                    }
                    items(uiState.pendingInvitations, key = { "inv_${it.invitationId}" }) { inv ->
                        PendingInvitationItem(invitation = inv)
                        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)
                    }
                }

                if (uiState.filteredMembers.isEmpty() && uiState.pendingInvitations.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "No members found.",
                                color = AppSecondaryText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text       = label,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = AppSecondaryText,
        fontSize   = 11.sp,
        modifier   = modifier
    )
}

@Composable
private fun MemberItem(member: SpaceMemberWithUserDto) {
    val absoluteImageUrl = member.profilePictureUrl?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
        BuildConfig.API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (absoluteImageUrl != null) {
                AsyncImage(
                    model              = absoluteImageUrl,
                    contentDescription = member.displayName,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    text       = member.initials,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = AppPrimary,
                    fontSize   = 14.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = member.displayName,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleChip(role = member.role)
                if (member.status != "active") {
                    StatusChip(status = member.status)
                }
            }
        }
    }
}

@Composable
private fun PendingInvitationItem(invitation: SpacePendingInvitationDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "?",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFE65100)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = invitation.emailInvited ?: "Unknown",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            StatusChip(status = "pending")
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    val (bgColor, textColor) = when (role.lowercase()) {
        "owner" -> Color(0xFF1A237E) to Color.White
        "admin" -> AppPrimary.copy(alpha = 0.15f) to AppPrimary
        else    -> AppCardElevated to AppSecondaryText
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text     = role.replaceFirstChar { it.uppercaseChar() },
            style    = MaterialTheme.typography.labelSmall,
            color    = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "active"  -> Color(0xFF1B5E20).copy(alpha = 0.12f) to Color(0xFF2E7D32)
        "pending" -> Color(0xFFE65100).copy(alpha = 0.12f) to Color(0xFFE65100)
        else      -> AppCardElevated to AppSecondaryText
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text     = status.replaceFirstChar { it.uppercaseChar() },
            style    = MaterialTheme.typography.labelSmall,
            color    = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp
        )
    }
}