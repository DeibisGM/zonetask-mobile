package com.app.zonetask.ui.screens.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.app.zonetask.data.remote.dto.ChatMemberDto
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppTopBar

@Composable
fun ChatEditScreen(
    spaceId: Int,
    userId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatEditViewModel = viewModel(
        factory = ChatEditViewModelFactory(
            chatGroupRepository = AppContainer.chatGroupRepository,
            spaceId             = spaceId,
            userId              = userId
        )
    )
) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val context         = LocalContext.current
    val contentResolver = context.contentResolver

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onImageSelected(it) } }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSaved()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        ChatEditToolbar(onBack = onBack)
        HorizontalDivider(color = AppBorder, thickness = 0.5.dp)

        if (uiState.isLoadingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppPrimary)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatAvatarPicker(
                name             = uiState.name,
                existingImageUrl = uiState.imageUrl,
                selectedUri      = uiState.selectedImageUri,
                onClick          = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.errorBanner != null) {
                Text(
                    text  = uiState.errorBanner!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            ChatFormField(label = "Chat name") {
                OutlinedTextField(
                    value         = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = chatEditTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ChatFormField(label = "Description") {
                OutlinedTextField(
                    value         = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines      = 4,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = chatEditTextFieldColors(),
                    placeholder   = { Text(text = "Optional", color = AppSecondaryText) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick  = { viewModel.saveChanges(contentResolver) },
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "Save",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ParticipantsSection(
                members   = uiState.members,
                isLoading = uiState.isMembersLoading
            )
        }
    }
}

@Composable
private fun ChatAvatarPicker(
    name: String,
    existingImageUrl: String,
    selectedUri: android.net.Uri?,
    onClick: () -> Unit
) {
    val absoluteUrl = existingImageUrl.trim().let { url ->
        if (url.isBlank()) null
        else BuildConfig.API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
    }

    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(AppCardElevated)
            .border(2.dp, AppPrimary.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            selectedUri != null -> AsyncImage(
                model              = selectedUri,
                contentDescription = "Chat image",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            absoluteUrl != null -> AsyncImage(
                model              = absoluteUrl,
                contentDescription = "Chat image",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            name.isNotBlank() -> Text(
                text       = name.take(2).uppercase(),
                color      = AppPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 24.sp
            )
            else -> Icon(
                imageVector        = Icons.Default.AddAPhoto,
                contentDescription = "Add photo",
                tint               = AppSecondaryText,
                modifier           = Modifier.size(28.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text  = "Tap to change photo",
        style = MaterialTheme.typography.bodySmall,
        color = AppSecondaryText
    )
}

@Composable
private fun ChatEditToolbar(onBack: () -> Unit) {
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
        Text(
            text       = "Edit chat",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChatFormField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun chatEditTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = AppPrimary,
    unfocusedBorderColor    = AppBorder,
    focusedContainerColor   = AppCardElevated,
    unfocusedContainerColor = AppCardElevated,
    focusedTextColor        = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor      = MaterialTheme.colorScheme.onBackground,
    cursorColor             = AppPrimary
)

@Composable
private fun ParticipantsSection(
    members: List<ChatMemberDto>,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "Lista de participantes",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.weight(1f)
            )
            if (!isLoading) {
                Box(
                    modifier          = Modifier
                        .background(AppPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment  = Alignment.Center
                ) {
                    Text(
                        text       = "${members.size}",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = AppPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = AppPrimary, strokeWidth = 2.dp)
            }
            return@Column
        }

        members.forEach { member ->
            MemberCard(member = member)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MemberCard(member: ChatMemberDto) {
    val alpha = if (member.isActive) 1f else 0.45f

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(AppCardElevated, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberAvatar(initials = member.initials, isActive = member.isActive)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = member.fullName,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
            )
            Text(
                text  = member.role.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = AppSecondaryText.copy(alpha = alpha)
            )
        }

        if (!member.isActive) {
            Box(
                modifier         = Modifier
                    .background(AppBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "Inactivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppSecondaryText
                )
            }
        }
    }
}

@Composable
private fun MemberAvatar(initials: String, isActive: Boolean) {
    val bgColor     = if (isActive) AppPrimary else AppBorder
    val textColor   = if (isActive) Color.White else AppSecondaryText

    Box(
        modifier         = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials.ifBlank { "?" },
            color      = textColor,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp
        )
    }
}