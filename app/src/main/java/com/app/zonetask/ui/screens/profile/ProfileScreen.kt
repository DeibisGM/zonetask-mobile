package com.app.zonetask.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.BuildConfig
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.UserResponse
import com.app.zonetask.data.remote.repository.UserRepository
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ProfileAvatarCard
import com.app.zonetask.ui.components.TaskDangerButton
import com.app.zonetask.ui.components.TaskFilledButton
import com.app.zonetask.ui.components.TaskSectionCard
import com.app.zonetask.ui.components.ZoneTaskHeaderBar
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    userId: Int,
    onTabSelected: (NavDestination) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    refreshTrigger: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(AppContainer.userRepository, userId)
    )
) {
    val uiState = viewModel.uiState
    val currentUser = uiState.user ?: AuthSessionStore.currentUser
    val displayName = currentUser?.displayName.orEmpty().ifBlank {
        listOfNotNull(currentUser?.firstName, currentUser?.lastName).joinToString(" ").trim()
    }
    // The incomplete-profile check intentionally ignores the photo so onboarding stays focused on required fields.
    val missingFields = remember(currentUser) { buildMissingProfileFields(currentUser) }

    LaunchedEffect(userId, refreshTrigger) {
        viewModel.load()
    }

    ZoneTaskScaffold(
        title = "",
        showTopBar = false,
        currentDestination = NavDestination.PROFILE,
        onDestinationSelected = onTabSelected
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppBackground)
        ) {
            ZoneTaskHeaderBar(title = "Mi perfil")

            Box(
                modifier = Modifier
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

                    uiState.errorMessage != null && currentUser == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TaskFilledButton(
                                text = "Reintentar",
                                onClick = viewModel::load,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                            imageUrl = resolveBackendImageUrl(currentUser?.profilePictureUrl),
                                            displayName = displayName
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            ProfileDetailRow(
                                                label = "Usuario",
                                                value = currentUser?.username.orEmpty()
                                            )
                                            ProfileDetailRow(
                                                label = "Nombre completo",
                                                value = displayName
                                            )
                                            ProfileDetailRow(
                                                label = "Correo",
                                                value = currentUser?.email.orEmpty()
                                            )
                                        }
                                    }

                                    ProfileDetailRow(
                                        label = "Biografía",
                                        value = currentUser?.bio.orEmpty(),
                                        emptyLabel = "Por completar"
                                    )
                                }
                            }

                            TaskSectionCard(title = null) {
                                ProfileDetailRow(
                                    label = "Teléfono",
                                    value = currentUser?.phone.orEmpty(),
                                    emptyLabel = "Por completar"
                                )
                                ProfileDetailRow(
                                    label = "Fecha de nacimiento",
                                    value = formatBirthDate(currentUser?.birthDate),
                                    emptyLabel = "Por completar"
                                )
                                ProfileDetailRow(
                                    label = "Género",
                                    value = genderLabel(currentUser?.gender),
                                    emptyLabel = "Por completar"
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (missingFields.isNotEmpty()) {
                                    Text(
                                        text = "Aún no completas tu información",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppSecondaryText
                                    )
                                }

                                TaskFilledButton(
                                    text = "Editar perfil",
                                    onClick = onEditProfile,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                )
                                TaskDangerButton(
                                    text = "Cerrar sesión",
                                    onClick = onLogout,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Returns only the required profile fields that still need to be completed.
private fun buildMissingProfileFields(user: UserResponse?): List<String> {
    if (user == null) return emptyList()
    return buildList {
        if (user.phone.isNullOrBlank()) add("teléfono")
        if (user.birthDate.isNullOrBlank()) add("fecha de nacimiento")
        if (user.gender.isNullOrBlank()) add("género")
        if (user.bio.isNullOrBlank()) add("biografía")
    }
}

// Maps the stored gender key to the Spanish label used in the UI.
private fun genderLabel(key: String?): String = when (key) {
    "male" -> "Masculino"
    "female" -> "Femenino"
    "non_binary" -> "No binario"
    "prefer_not_to_say" -> "Prefiero no decirlo"
    else -> ""
}

// Formats the backend birth date into the locale-friendly dd/MM/yyyy display format.
private fun formatBirthDate(rawValue: String?): String {
    val value = rawValue?.trim().orEmpty()
    if (value.isBlank()) return ""

    val normalized = value.substringBefore('T')
    val parsed = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(normalized)
    }.getOrNull() ?: return value

    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed)
}

// Resolves relative backend image paths into a fully qualified URL for Coil.
private fun resolveBackendImageUrl(path: String?): String? {
    val normalized = path?.trim().orEmpty()
    if (normalized.isBlank()) return null
    return if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
        normalized
    } else {
        BuildConfig.API_BASE_URL.trimEnd('/') + "/" + normalized.trimStart('/')
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    emptyLabel: String = "No registrado"
) {
    val displayValue = value.takeIf { it.isNotBlank() } ?: emptyLabel

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppSecondaryText
        )
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyLarge,
            color = AppOnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

data class ProfileUiState(
    val user: UserResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val userId: Int
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    // Loads the latest profile snapshot and keeps the shared session cache in sync.
    fun load() {
        if (userId <= 0) {
            uiState = ProfileUiState(
                user = null,
                isLoading = false,
                errorMessage = "No se encontró una sesión válida."
            )
            return
        }

        uiState = ProfileUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = userRepository.getUserById(userId)) {
                is ApiResult.Success -> {
                    val user = result.data
                    AuthSessionStore.save(
                        sessionToken = AuthSessionStore.sessionToken,
                        refreshToken = AuthSessionStore.refreshToken,
                        user = user
                    )
                    uiState = ProfileUiState(user = user, isLoading = false)
                }

                is ApiResult.Error -> {
                    uiState = ProfileUiState(
                        user = AuthSessionStore.currentUser,
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userRepository, userId) as T
    }
}
