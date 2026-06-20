package com.app.zonetask.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachFile
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.zonetask.BuildConfig
import com.app.zonetask.data.remote.dto.ChatMessageDto
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppTopBar
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    spaceId: Int,
    userId: Int,
    onBack: () -> Unit,
    onNavigateToEdit: () -> Unit = {},
    reloadTrigger: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            chatGroupRepository = AppContainer.chatGroupRepository,
            spaceId             = spaceId,
            userId              = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.selectImage(it) } }

    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger) viewModel.reload()
    }

    LaunchedEffect(viewModel) {
        viewModel.scrollToBottomEvent.collectLatest {
            val total = listState.layoutInfo.totalItemsCount
            if (total > 0) listState.animateScrollToItem(total - 1)
        }
    }

    val groupedMessages: List<Pair<String, List<ChatMessageDto>>> = remember(uiState.messages) {
        uiState.messages
            .groupBy { msg -> getLocalDateKey(msg.createdAt) }
            .entries
            .sortedBy { it.key }
            .map { (key, msgs) -> key to msgs }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val total        = listState.layoutInfo.totalItemsCount
            total > 0 && firstVisible <= 2
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.hasMore && !uiState.isLoadingMore && !uiState.isMessagesLoading) {
            viewModel.loadMoreMessages()
        }
    }

    // Full-screen image viewer
    if (uiState.viewingImageUrl != null) {
        Dialog(
            onDismissRequest = { viewModel.closeImageViewer() },
            properties       = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model              = uiState.viewingImageUrl,
                    contentDescription = "Full screen image",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick  = { viewModel.closeImageViewer() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Close,
                        contentDescription = "Close viewer",
                        tint               = Color.White,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }
        }
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

        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (uiState.isMessagesLoading) {
                item(key = "messages_loading") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            if (uiState.isLoadingMore) {
                item(key = "load_more_indicator") {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = AppPrimary,
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            groupedMessages.forEach { (dateKey, msgs) ->
                item(key = "header_$dateKey") {
                    DateHeader(label = getDateLabel(dateKey))
                }
                items(msgs, key = { it.chatMessageId }) { msg ->
                    MessageBubble(
                        msg               = msg,
                        isFromCurrentUser = msg.senderId == userId,
                        onImageClick      = { viewModel.openImageViewer(it) }
                    )
                }
            }
        }

        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)

        // Image preview area (shown when an image is selected but not yet sent)
        if (uiState.pendingImageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTopBar)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model              = uiState.pendingImageUri,
                    contentDescription = "Image preview",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.Center)
                )
                if (uiState.isUploadingImage) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                } else {
                    IconButton(
                        onClick  = { viewModel.clearPendingImage() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Close,
                            contentDescription = "Cancel image",
                            tint               = Color.White,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTopBar)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Attachment button
            IconButton(
                onClick  = { imagePickerLauncher.launch("image/*") },
                enabled  = !uiState.isSending && !uiState.isUploadingImage,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppCardElevated)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.AttachFile,
                    contentDescription = "Attach image",
                    tint               = if (!uiState.isSending && !uiState.isUploadingImage) AppPrimary
                                         else AppPrimary.copy(alpha = 0.3f),
                    modifier           = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(6.dp))

            OutlinedTextField(
                value         = messageText,
                onValueChange = { messageText = it },
                placeholder   = {
                    Text(
                        text  = "Write a message...",
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

            Spacer(Modifier.width(6.dp))

            val canSend = (uiState.pendingImageUri != null || messageText.isNotBlank())
                && !uiState.isSending && !uiState.isUploadingImage

            IconButton(
                onClick = {
                    if (uiState.pendingImageUri != null) {
                        viewModel.sendImageMessage(context.contentResolver)
                    } else {
                        val content = messageText.trim()
                        if (content.isNotEmpty()) {
                            viewModel.sendMessage(content)
                            messageText = ""
                        }
                    }
                },
                enabled  = canSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canSend) AppPrimary else AppPrimary.copy(alpha = 0.3f))
            ) {
                if (uiState.isSending || uiState.isUploadingImage) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateHeader(label: String) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppBorder.copy(alpha = 0.4f)
        ) {
            Text(
                text     = label,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                style    = MaterialTheme.typography.labelSmall,
                color    = AppSecondaryText,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MessageBubble(
    msg: ChatMessageDto,
    isFromCurrentUser: Boolean,
    onImageClick: (String) -> Unit = {}
) {
    val bubbleColor = if (isFromCurrentUser) AppPrimary else AppCardElevated
    val textColor   = if (isFromCurrentUser) Color.White else MaterialTheme.colorScheme.onSurface
    val timeColor   = if (isFromCurrentUser) Color.White.copy(alpha = 0.7f) else AppSecondaryText
    val bubbleShape = if (isFromCurrentUser)
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    else
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (!isFromCurrentUser) {
            SenderAvatar(initials = msg.initials)
            Spacer(Modifier.width(6.dp))
        }

        Column(
            modifier            = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            if (!isFromCurrentUser) {
                Text(
                    text       = msg.senderDisplayName,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = AppPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 11.sp,
                    modifier   = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            if (msg.mediaUrl != null) {
                // Image message
                val absoluteMediaUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/" + msg.mediaUrl.trimStart('/')
                Column(
                    horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
                ) {
                    AsyncImage(
                        model              = absoluteMediaUrl,
                        contentDescription = "Image",
                        contentScale       = ContentScale.Fit,
                        modifier           = Modifier
                            .widthIn(max = 220.dp)
                            .clip(bubbleShape)
                            .clickable { onImageClick(absoluteMediaUrl) }
                    )
                    Text(
                        text     = formatMessageTime(msg.createdAt),
                        color    = AppSecondaryText,
                        style    = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp, end = if (isFromCurrentUser) 2.dp else 0.dp)
                    )
                }
            } else {
                // Text message
                Surface(shape = bubbleShape, color = bubbleColor) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text  = msg.content,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text     = formatMessageTime(msg.createdAt),
                            color    = timeColor,
                            style    = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 3.dp)
                        )
                    }
                }
            }
        }

        if (isFromCurrentUser) {
            Spacer(Modifier.width(6.dp))
        }
    }
}

@Composable
private fun SenderAvatar(initials: String) {
    Box(
        modifier         = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(AppPrimary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            style      = MaterialTheme.typography.labelSmall,
            color      = AppPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 11.sp
        )
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

private fun getLocalDateKey(isoString: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = sdfIn.parse(isoString.take(19)) ?: return isoString.take(10)
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = java.util.TimeZone.getDefault()
        }.format(date)
    } catch (e: Exception) {
        isoString.take(10)
    }
}

private fun getDateLabel(dateKey: String): String {
    val fmt       = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today     = fmt.format(Date())
    val cal       = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterday = fmt.format(cal.time)
    return when (dateKey) {
        today     -> "Today"
        yesterday -> "Yesterday"
        else -> try {
            val date = fmt.parse(dateKey)!!
            SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(date)
        } catch (e: Exception) { dateKey }
    }
}

private fun formatMessageTime(isoString: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = sdfIn.parse(isoString.take(19)) ?: return ""
        SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getDefault()
        }.format(date)
    } catch (e: Exception) {
        if (isoString.length >= 16) isoString.substring(11, 16) else ""
    }
}