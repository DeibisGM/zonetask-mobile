package com.app.zonetask.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.BuildConfig
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.UpdateUserProfileRequest
import com.app.zonetask.data.remote.repository.UserRepository
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.ProfileAvatarCard
import com.app.zonetask.ui.components.TaskActionButtonsRow
import com.app.zonetask.ui.components.TaskCreateScaffold
import com.app.zonetask.ui.components.TaskDropdown
import com.app.zonetask.ui.components.TaskSectionCard
import com.app.zonetask.ui.components.TaskTextField
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppTopBar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileEditScreen(
    userId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileEditViewModel = viewModel(
        factory = ProfileEditViewModelFactory(AppContainer.userRepository, userId)
    )
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    // Uses the system file picker so the user can choose a new profile image without a custom permission flow.
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoPreviewSelected(uri.toString())
            viewModel.uploadProfilePicture(context, uri)
        }
    }

    LaunchedEffect(userId) {
        viewModel.load()
    }

    TaskCreateScaffold(
        title = "Editar perfil",
        showBack = true,
        onBackClick = onBack,
        onNavigate = {},
        onLogout = {},
        topBarColor = AppTopBar,
        bottomBar = {
            if (!uiState.isLoading && !uiState.isUploadingPhoto) {
                TaskActionButtonsRow(
                    cancelText = "Cancelar",
                    saveText = "Guardar",
                    onCancelClick = onBack,
                    onSaveClick = {
                        viewModel.save {
                            onSaved()
                            onBack()
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppBackground)
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TaskSectionCard(
                            title = null
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    ProfileAvatarCard(
                                        imageUrl = displayProfileImageUrl(
                                            previewUrl = uiState.selectedPhotoPreviewUrl,
                                            storedUrl = uiState.profilePictureUrl
                                        ),
                                        displayName = uiState.displayName,
                                        onClick = { pickImageLauncher.launch("image/*") }
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ProfileEditReadOnly(label = "Usuario", value = uiState.username)
                                        ProfileEditReadOnly(label = "Nombre completo", value = uiState.displayName)
                                        ProfileEditReadOnly(label = "Correo", value = uiState.email)
                                    }
                                }

                                TaskTextField(
                                    label = "Biografía",
                                    value = uiState.bio.orEmpty(),
                                    onValueChange = viewModel::onBioChanged,
                                    placeholder = "Cuéntanos sobre ti",
                                    singleLine = false
                                )
                            }
                        }

                        TaskSectionCard(title = null) {
                            TaskTextField(
                                label = "Nombre",
                                value = uiState.firstName,
                                onValueChange = viewModel::onFirstNameChanged,
                                placeholder = "Escribe tu nombre"
                            )
                            TaskTextField(
                                label = "Apellido",
                                value = uiState.lastName.orEmpty(),
                                onValueChange = viewModel::onLastNameChanged,
                                placeholder = "Escribe tu apellido"
                            )
                            TaskTextField(
                                label = "Teléfono",
                                value = uiState.phone.orEmpty(),
                                onValueChange = viewModel::onPhoneChanged,
                                placeholder = "Opcional"
                            )
                            TaskTextField(
                                label = "Fecha de nacimiento",
                                value = uiState.birthDateInput,
                                onValueChange = viewModel::onBirthDateChanged,
                                placeholder = "DD/MM/AAAA"
                            )
                            TaskDropdown(
                                label = "Género",
                                value = uiState.genderLabel,
                                options = uiState.genderOptions,
                                onOptionSelected = viewModel::onGenderChanged
                            )
                        }

                        uiState.errorMessage?.let { message ->
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (uiState.isUploadingPhoto) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = AppPrimary, modifier = Modifier.height(22.dp))
                                Text(
                                    text = "Subiendo foto...",
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
}

@Composable
private fun ProfileEditReadOnly(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppSecondaryText
        )
        Text(
            text = value.ifBlank { "No registrado" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

data class ProfileEditUiState(
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String? = null,
    val phone: String? = null,
    val birthDateInput: String = "",
    val gender: String? = null,
    val bio: String? = null,
    val profilePictureUrl: String? = null,
    val selectedPhotoPreviewUrl: String? = null,
    val isLoading: Boolean = true,
    val isUploadingPhoto: Boolean = false,
    val errorMessage: String? = null
) {
    val displayName: String
        get() = listOfNotNull(
            firstName.trim().takeIf { it.isNotBlank() },
            lastName?.trim()?.takeIf { it.isNotBlank() }
        ).joinToString(" ").ifBlank { username }

    val genderOptions = listOf(
        "Masculino" to "male",
        "Femenino" to "female",
        "No binario" to "non_binary",
        "Prefiero no decirlo" to "prefer_not_to_say"
    )

    val genderLabel: String
        get() = genderOptions.firstOrNull { it.second == gender }?.first ?: "Selecciona tu género"
}

class ProfileEditViewModel(
    private val userRepository: UserRepository,
    private val userId: Int
) : ViewModel() {

    var uiState by mutableStateOf(ProfileEditUiState())
        private set

    // Loads the editable profile state and resets the screen into a clean loading state first.
    fun load() {
        if (userId <= 0) {
            uiState = ProfileEditUiState(
                isLoading = false,
                errorMessage = "No se encontró una sesión válida."
            )
            return
        }

        uiState = ProfileEditUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = userRepository.getUserById(userId)) {
                is ApiResult.Success -> {
                    val user = result.data
                    uiState = ProfileEditUiState(
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        phone = user.phone,
                        birthDateInput = formatBirthDateForEdit(user.birthDate),
                        gender = user.gender,
                        bio = user.bio,
                        profilePictureUrl = user.profilePictureUrl,
                        isLoading = false
                    )
                }

                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // Keeps the selected local image visible while the upload is still running.
    fun onPhotoPreviewSelected(url: String?) {
        uiState = uiState.copy(selectedPhotoPreviewUrl = url)
    }

    fun onFirstNameChanged(value: String) {
        uiState = uiState.copy(firstName = value)
    }

    fun onLastNameChanged(value: String) {
        uiState = uiState.copy(lastName = value)
    }

    fun onPhoneChanged(value: String) {
        uiState = uiState.copy(phone = value)
    }

    fun onBirthDateChanged(value: String) {
        uiState = uiState.copy(birthDateInput = value)
    }

    fun onGenderChanged(value: String) {
        uiState = uiState.copy(gender = value)
    }

    fun onBioChanged(value: String) {
        uiState = uiState.copy(bio = value)
    }

    // Copies the selected image to a temporary file and sends it to the backend upload endpoint.
    fun uploadProfilePicture(context: Context, uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isUploadingPhoto = true, errorMessage = null)
            val tempFile = uriToTempFile(context, uri)
            if (tempFile == null) {
                uiState = uiState.copy(
                    isUploadingPhoto = false,
                    selectedPhotoPreviewUrl = uiState.profilePictureUrl,
                    errorMessage = "No se pudo leer la imagen."
                )
                return@launch
            }

            val mimeType = context.contentResolver.getType(uri)
            when (val result = userRepository.uploadProfilePicture(userId, tempFile, mimeType)) {
                is ApiResult.Success -> {
                    val updatedUser = result.data
                    AuthSessionStore.save(
                        sessionToken = AuthSessionStore.sessionToken,
                        refreshToken = AuthSessionStore.refreshToken,
                        user = updatedUser
                    )
                    uiState = uiState.copy(
                        isUploadingPhoto = false,
                        profilePictureUrl = updatedUser.profilePictureUrl,
                        username = updatedUser.username,
                        email = updatedUser.email,
                        firstName = updatedUser.firstName,
                        lastName = updatedUser.lastName,
                        gender = updatedUser.gender,
                        phone = updatedUser.phone,
                        bio = updatedUser.bio,
                        birthDateInput = formatBirthDateForEdit(updatedUser.birthDate),
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isUploadingPhoto = false,
                        selectedPhotoPreviewUrl = uiState.profilePictureUrl,
                        errorMessage = result.message
                    )
                }
            }

            tempFile.delete()
        }
    }

    // Persists the editable fields and recalculates onboarding completion from the required profile data.
    fun save(onSuccess: () -> Unit) {
        if (uiState.firstName.isBlank()) {
            uiState = uiState.copy(errorMessage = "El nombre es obligatorio.")
            return
        }

        val birthDate = parseBirthDateForBackend(uiState.birthDateInput)
        if (uiState.birthDateInput.isNotBlank() && birthDate == null) {
            uiState = uiState.copy(errorMessage = "La fecha debe tener el formato dd/MM/aaaa.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val request = UpdateUserProfileRequest(
                firstName = uiState.firstName,
                lastName = uiState.lastName,
                phone = uiState.phone,
                birthDate = birthDate,
                gender = uiState.gender,
                bio = uiState.bio,
                onboardingCompleted = isProfileComplete(
                    phone = uiState.phone,
                    birthDateInput = uiState.birthDateInput,
                    gender = uiState.gender,
                    bio = uiState.bio
                )
            )

            when (val result = userRepository.updateUserProfile(userId, request)) {
                is ApiResult.Success -> {
                    AuthSessionStore.save(
                        sessionToken = AuthSessionStore.sessionToken,
                        refreshToken = AuthSessionStore.refreshToken,
                        user = result.data
                    )
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                }

                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // Determines whether the required profile fields are complete, excluding the optional photo.
    private fun isProfileComplete(
        phone: String?,
        birthDateInput: String,
        gender: String?,
        bio: String?
    ): Boolean {
        return !phone.isNullOrBlank() &&
            birthDateInput.isNotBlank() &&
            !gender.isNullOrBlank() &&
            !bio.isNullOrBlank()
    }
}

class ProfileEditViewModelFactory(
    private val userRepository: UserRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileEditViewModel(userRepository, userId) as T
    }
}

private fun formatBirthDateForEdit(value: String?): String {
    val raw = value?.trim().orEmpty()
    if (raw.isBlank()) return ""
    val datePart = raw.substringBefore('T')
    val parsed = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart)
    }.getOrNull() ?: return raw
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed)
}

private fun parseBirthDateForBackend(value: String): String? {
    val normalized = value.trim()
    if (normalized.isBlank()) return null
    val parsed = runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(normalized)
    }.getOrNull() ?: return null
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsed)
}

private fun uriToTempFile(context: Context, uri: Uri): File? {
    val resolver = context.contentResolver
    val fileName = runCatching {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    }.getOrNull() ?: "profile_${System.currentTimeMillis()}.jpg"

    val tempFile = File(context.cacheDir, fileName)
    return try {
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (_: Exception) {
        null
    }
}

// Prefers the local preview while editing and falls back to the stored backend image URL.
private fun displayProfileImageUrl(previewUrl: String?, storedUrl: String?): String? {
    val localPreview = previewUrl?.trim().orEmpty()
    if (localPreview.isNotBlank()) return localPreview

    val normalized = storedUrl?.trim().orEmpty()
    if (normalized.isBlank()) return null
    return if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
        normalized
    } else {
        BuildConfig.API_BASE_URL.trimEnd('/') + "/" + normalized.trimStart('/')
    }
}
