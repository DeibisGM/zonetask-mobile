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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppIconTint
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

private const val ROLE_OWNER = "owner"
private const val ROLE_ADMIN = "admin"

@Composable
fun SpaceDetailScreen(
    spaceId: Int,
    userId: Int,
    modifier: Modifier = Modifier,
    onNavigateToPermissions: (Int) -> Unit = {},
    viewModel: SpaceDetailViewModel = viewModel(
        factory = SpaceDetailViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId         = spaceId,
            userId          = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppPrimary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text  = UserMessages.Spaces.LOADING,
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
                    TextButton(onClick = { viewModel.loadSpace() }) {
                        Text(
                            text  = UserMessages.TAP_TO_RETRY_SUFFIX.trim(),
                            color = AppPrimary
                        )
                    }
                }
            }
        }

        uiState.space != null -> {
            SpaceDetailContent(
                space                   = uiState.space!!,
                userRole                = uiState.userRole,
                onNavigateToPermissions = { onNavigateToPermissions(spaceId) },
                modifier                = modifier
            )
        }
    }
}

@Composable
private fun SpaceDetailContent(
    space: Space,
    userRole: String,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canViewPermissions = userRole == ROLE_OWNER || userRole == ROLE_ADMIN

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text  = space.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, AppBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                DetailRow(
                    icon  = Icons.Outlined.Category,
                    label = UserMessages.SpaceDetail.TYPE_LABEL,
                    value = space.spaceType
                )
                HorizontalDivider(color = AppBorder)
                DetailRow(
                    icon  = Icons.Outlined.Info,
                    label = UserMessages.SpaceDetail.DESC_LABEL,
                    value = space.description ?: UserMessages.SpaceDetail.NO_DESCRIPTION
                )
            }
        }

        if (canViewPermissions) {
            Surface(
                onClick = onNavigateToPermissions,
                shape   = RoundedCornerShape(16.dp),
                color   = MaterialTheme.colorScheme.surface,
                border  = BorderStroke(1.dp, AppPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.AdminPanelSettings,
                        contentDescription = null,
                        tint               = AppPrimary,
                        modifier           = Modifier.size(20.dp)
                    )
                    Text(
                        text     = UserMessages.SpaceDetail.PERMISSIONS_BUTTON,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = AppPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector        = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint               = AppPrimary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(18.dp),
            tint               = AppIconTint
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppSecondaryText
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
