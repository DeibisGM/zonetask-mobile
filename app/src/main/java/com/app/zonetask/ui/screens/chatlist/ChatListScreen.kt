package com.app.zonetask.ui.screens.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.zonetask.BuildConfig
import com.app.zonetask.data.remote.dto.ChatGroupResponse
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun ChatListScreen(
    userId: Int,
    onNavigateToChat: (spaceId: Int) -> Unit,
    onTabSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(
            chatGroupRepository = AppContainer.chatGroupRepository,
            userId              = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ZoneTaskScaffold(
        title                 = "Chats",
        showBack              = false,
        onBackClick           = {},
        showTopBar            = true,
        currentDestination    = NavDestination.CHAT,
        onDestinationSelected = onTabSelected
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    color    = AppPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )

                uiState.errorMessage != null -> Text(
                    text     = uiState.errorMessage!!,
                    color    = AppSecondaryText,
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                uiState.chats.isEmpty() -> Text(
                    text     = "No perteneces a ningún chat aún.",
                    color    = AppSecondaryText,
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.chats, key = { it.chatId }) { chat ->
                        ChatListItem(
                            chat    = chat,
                            onClick = { onNavigateToChat(chat.spaceId) }
                        )
                        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatGroupResponse,
    onClick: () -> Unit
) {
    val absoluteImageUrl = chat.imageUrl?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
        BuildConfig.API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (absoluteImageUrl != null) {
                AsyncImage(
                    model              = absoluteImageUrl,
                    contentDescription = chat.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text       = chat.name.take(2).uppercase(),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = AppPrimary,
                    fontSize   = 14.sp
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier     = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text       = chat.name,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (!chat.description.isNullOrBlank()) {
                Text(
                    text     = chat.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = AppSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}