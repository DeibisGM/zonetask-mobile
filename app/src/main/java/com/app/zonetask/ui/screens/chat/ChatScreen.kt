package com.app.zonetask.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.zonetask.BuildConfig
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppTopBar

@Composable
fun ChatScreen(
    spaceId: Int,
    onBack: () -> Unit,
    onNavigateToEdit: () -> Unit = {},
    reloadTrigger: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            chatGroupRepository = AppContainer.chatGroupRepository,
            spaceId             = spaceId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger) viewModel.reload()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        ChatToolbar(
            spaceName        = uiState.spaceName.ifBlank { "..." },
            imageUrl         = uiState.imageUrl,
            isLoading        = uiState.isLoading,
            onBack           = onBack,
            onNavigateToEdit = onNavigateToEdit
        )

        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTopBar)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = messageText,
                onValueChange = { messageText = it },
                placeholder   = {
                    Text(
                        text  = "Message...",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                shape  = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = AppCardElevated,
                    unfocusedContainerColor = AppCardElevated,
                    focusedBorderColor      = AppBorder,
                    unfocusedBorderColor    = AppBorder,
                    cursorColor             = AppPrimary,
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines  = 4,
                modifier  = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ChatToolbar(
    spaceName: String,
    imageUrl: String?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val absoluteImageUrl = imageUrl?.trim()?.let { url ->
        if (url.isBlank()) null
        else BuildConfig.API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTopBar)
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onNavigateToEdit),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        color       = AppPrimary,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(20.dp)
                    )
                    absoluteImageUrl != null -> AsyncImage(
                        model              = absoluteImageUrl,
                        contentDescription = "Chat image",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                    else -> Text(
                        text       = spaceName.take(2).uppercase(),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = AppPrimary,
                        fontSize   = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text       = spaceName,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1
            )
        }
    }
}