package com.app.zonetask.ui.screens.register

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.R
import com.app.zonetask.core.UserMessages
import com.app.zonetask.data.auth.BackendAuthRepository
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.RegisterRequest
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.AuthCard
import com.app.zonetask.ui.components.AuthHeader
import com.app.zonetask.ui.components.AuthPasswordField
import com.app.zonetask.ui.components.AuthPrimaryButton
import com.app.zonetask.ui.components.AuthScreenShell
import com.app.zonetask.ui.components.AuthStatusMessage
import com.app.zonetask.ui.components.AuthTextField
import com.app.zonetask.ui.components.TaskDropdown
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import kotlinx.coroutines.launch

private const val STEP_ACCOUNT = 0
private const val STEP_CREDENTIALS = 1
private const val STEP_OPTIONAL = 2
private const val TOTAL_STEPS = 3

@Composable
fun RegisterScreen(
    onBackToLogin: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(AppContainer.authRepository)
    )
) {
    val uiState = viewModel.uiState
    var currentStep by remember { mutableStateOf(STEP_ACCOUNT) }

    // When sign-up succeeds, the screen hands control back to the login flow
    // with the verification notice that should be shown exactly once.
    LaunchedEffect(uiState.registrationCompleted) {
        if (uiState.registrationCompleted) {
            onBackToLogin(uiState.infoMessage ?: UserMessages.Login.REGISTRATION_NOTICE)
        }
    }

    AuthScreenShell(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 72.dp, bottom = 4.dp)
            ) {
                AuthHeader(
                    title = UserMessages.Register.TITLE,
                    subtitle = ""
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearStepper(
                    currentStep = currentStep,
                    steps = listOf("Cuenta", "Credenciales", "Perfil")
                )
            }

            // Scrollable content area (only the form scrolls, header + stepper stay fixed)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                when (currentStep) {
                    STEP_ACCOUNT -> AccountStep(
                        uiState = uiState,
                        onUsernameChanged = viewModel::onUsernameChanged,
                        onFirstNameChanged = viewModel::onFirstNameChanged,
                        onLastNameChanged = viewModel::onLastNameChanged,
                        onGenderChanged = viewModel::onGenderChanged
                    )
                    STEP_CREDENTIALS -> CredentialsStep(
                        uiState = uiState,
                        onEmailChanged = viewModel::onEmailChanged,
                        onPasswordChanged = viewModel::onPasswordChanged,
                        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
                        onToggleVisibility = viewModel::togglePasswordVisibility
                    )
                    STEP_OPTIONAL -> OptionalStep(
                        uiState = uiState,
                        onPhoneChanged = viewModel::onPhoneChanged,
                        onBioChanged = viewModel::onBioChanged
                    )
                }
            }

            // Bottom button — fixed to the bottom, never gets cut off.
            BottomBar(
                currentStep = currentStep,
                isLastStep = currentStep == TOTAL_STEPS - 1,
                isLoading = uiState.isLoading,
                canSubmit = uiState.canSubmit,
                canAdvance = viewModel.canAdvanceFrom(currentStep),
                errorMessage = uiState.errorMessage,
                onBack = { if (currentStep > 0) currentStep-- },
                onNext = {
                    if (currentStep < TOTAL_STEPS - 1) {
                        currentStep++
                    } else {
                        viewModel.register()
                    }
                },
                onBackToLogin = { onBackToLogin(null) }
            )
        }
    }
}

@Composable
private fun LinearStepper(
    currentStep: Int,
    steps: List<String>
) {
    // Linear stepper with a progress bar — every step label is visible from the start, and the bar fills as the user advances.
    Column(modifier = Modifier.fillMaxWidth()) {
        // Step labels stay aligned with the segments below so each label visually anchors to its bar slice.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= currentStep) AppPrimary else AppSecondaryText,
                    fontWeight = if (index == currentStep) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        // Linear progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        ) {
            steps.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            if (index <= currentStep) AppPrimary
                            else AppCardElevated
                        )
                )
            }
        }
    }
}

@Composable
private fun AccountStep(
    uiState: RegisterUiState,
    onUsernameChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onGenderChanged: (String) -> Unit
) {
    AuthCard {
        AuthTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = UserMessages.Register.USERNAME_LABEL,
            placeholder = UserMessages.Register.USERNAME_PLACEHOLDER,
            error = uiState.usernameError,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_username),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        AuthTextField(
            value = uiState.firstName,
            onValueChange = onFirstNameChanged,
            label = UserMessages.Register.FIRST_NAME_LABEL,
            placeholder = UserMessages.Register.FIRST_NAME_PLACEHOLDER,
            error = uiState.firstNameError,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_names),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        AuthTextField(
            value = uiState.lastName,
            onValueChange = onLastNameChanged,
            label = UserMessages.Register.LAST_NAME_LABEL,
            placeholder = UserMessages.Register.LAST_NAME_PLACEHOLDER,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_names),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        TaskDropdown(
            label = UserMessages.Register.GENDER_LABEL,
            value = genderOptions.find { it.second == uiState.gender }?.first
                ?: UserMessages.Register.GENDER_SELECT,
            options = genderOptions,
            onOptionSelected = onGenderChanged
        )
    }
}

@Composable
private fun CredentialsStep(
    uiState: RegisterUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    AuthCard {
        AuthTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = UserMessages.Login.EMAIL_LABEL,
            placeholder = UserMessages.Login.EMAIL_PLACEHOLDER,
            error = uiState.emailError,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        AuthPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = UserMessages.Login.PASSWORD_LABEL,
            placeholder = UserMessages.Login.PASSWORD_PLACEHOLDER,
            error = uiState.passwordError,
            isVisible = uiState.isPasswordVisible,
            onVisibilityToggle = onToggleVisibility,
            keyboardActions = KeyboardActions(
                onDone = { /* handled by next button */ }
            )
        )

        AuthPasswordField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = UserMessages.Register.CONFIRM_PASSWORD_LABEL,
            placeholder = UserMessages.Register.CONFIRM_PASSWORD_PLACEHOLDER,
            error = uiState.confirmPasswordError,
            isVisible = uiState.isPasswordVisible,
            onVisibilityToggle = onToggleVisibility,
            keyboardActions = KeyboardActions(
                onDone = { /* handled by next button */ }
            )
        )
    }
}

@Composable
private fun OptionalStep(
    uiState: RegisterUiState,
    onPhoneChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit
) {
    AuthCard {
        AuthTextField(
            value = uiState.phone,
            onValueChange = onPhoneChanged,
            label = UserMessages.Register.PHONE_LABEL,
            placeholder = UserMessages.Register.PHONE_PLACEHOLDER,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_phone),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        AuthTextField(
            value = uiState.bio,
            onValueChange = onBioChanged,
            label = UserMessages.Register.BIO_LABEL,
            placeholder = UserMessages.Register.BIO_PLACEHOLDER,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_about_you),
                    contentDescription = null,
                    tint = AppSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = false
        )
    }
}

@Composable
private fun BottomBar(
    currentStep: Int,
    isLastStep: Boolean,
    isLoading: Boolean,
    canSubmit: Boolean,
    canAdvance: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onBackToLogin: () -> Unit
) {
    // Fixed bottom bar with the back/next CTAs and the "I already have an account" link, so the primary actions never get cut off.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        AuthStatusMessage(message = errorMessage)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onBack,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppOnSurface
                    )
                ) {
                    Text(
                        text = "Atrás",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            AuthPrimaryButton(
                text = if (isLastStep) UserMessages.Register.SUBMIT else "Siguiente",
                onClick = onNext,
                modifier = Modifier.weight(if (currentStep > 0) 1f else 2f),
                loading = isLoading,
                enabled = if (isLastStep) canSubmit && !isLoading else canAdvance && !isLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBackToLogin,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = UserMessages.Register.BACK_TO_LOGIN,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class RegisterUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val phone: String = "",
    val bio: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val registrationCompleted: Boolean = false
) {
    val usernameError: String?
        get() = when {
            username.isBlank() -> null
            username.length < 3 -> UserMessages.Register.USERNAME_TOO_SHORT
            else -> null
        }

    val firstNameError: String?
        get() = when {
            firstName.isBlank() -> null
            else -> null
        }

    val emailError: String?
        get() = when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> UserMessages.Register.EMAIL_INVALID
            else -> null
        }

    val passwordError: String?
        get() = when {
            password.isBlank() -> null
            password.length < 8 -> UserMessages.Register.PASSWORD_MIN_LENGTH
            else -> null
        }

    val confirmPasswordError: String?
        get() = when {
            confirmPassword.isBlank() || password.isBlank() -> null
            confirmPassword != password -> UserMessages.Register.PASSWORDS_DONT_MATCH
            else -> null
        }

    val canSubmit: Boolean
        get() = username.isNotBlank() &&
            firstName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            !isLoading
}

class RegisterViewModel(
    private val authRepository: BackendAuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun canAdvanceFrom(step: Int): Boolean = when (step) {
        STEP_ACCOUNT -> uiState.username.isNotBlank() &&
            uiState.usernameError == null &&
            uiState.firstName.isNotBlank()
        STEP_CREDENTIALS -> uiState.email.isNotBlank() &&
            uiState.emailError == null &&
            uiState.password.isNotBlank() &&
            uiState.passwordError == null &&
            uiState.confirmPassword.isNotBlank() &&
            uiState.confirmPasswordError == null
        else -> true
    }

    fun onUsernameChanged(value: String) {
        uiState = uiState.copy(username = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onFirstNameChanged(value: String) {
        uiState = uiState.copy(firstName = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onLastNameChanged(value: String) {
        uiState = uiState.copy(lastName = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onGenderChanged(value: String) {
        uiState = uiState.copy(gender = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onPhoneChanged(value: String) {
        uiState = uiState.copy(phone = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onBioChanged(value: String) {
        uiState = uiState.copy(bio = value, errorMessage = null, infoMessage = null)
    }

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null, infoMessage = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        uiState = uiState.copy(confirmPassword = value, errorMessage = null, infoMessage = null)
    }

    fun togglePasswordVisibility() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun register() {
        val error = listOfNotNull(
            if (uiState.username.isBlank()) UserMessages.Register.USERNAME_REQUIRED else null,
            if (uiState.firstName.isBlank()) UserMessages.Register.FIRST_NAME_REQUIRED else null,
            uiState.emailError,
            uiState.passwordError,
            uiState.confirmPasswordError
        ).firstOrNull()

        if (error != null) {
            uiState = uiState.copy(errorMessage = error, infoMessage = null)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        viewModelScope.launch {
            when (val result = authRepository.register(
                RegisterRequest(
                    username = uiState.username.trim(),
                    firstName = uiState.firstName.trim(),
                    lastName = uiState.lastName.trim().ifBlank { null },
                    gender = uiState.gender.trim().ifBlank { null },
                    phone = uiState.phone.trim().ifBlank { null },
                    bio = uiState.bio.trim().ifBlank { null },
                    email = uiState.email.trim(),
                    password = uiState.password
                )
            )) {
                is ApiResult.Success -> {
                    val userId = result.data.user?.userId ?: 0
                    uiState = uiState.copy(
                        isLoading = false,
                        registrationCompleted = userId > 0,
                        infoMessage = if (result.data.emailVerificationSent) {
                            UserMessages.Register.REGISTRATION_PENDING
                        } else {
                            UserMessages.Register.REGISTRATION_FAILED_VERIFICATION
                        },
                        errorMessage = null
                    )
                }

                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        infoMessage = null
                    )
                }
            }
        }
    }
}

class RegisterViewModelFactory(
    private val authRepository: BackendAuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(authRepository) as T
    }
}

private val genderOptions = listOf(
    UserMessages.Register.GENDER_MALE to "male",
    UserMessages.Register.GENDER_FEMALE to "female",
    UserMessages.Register.GENDER_NON_BINARY to "non_binary",
    UserMessages.Register.GENDER_NOT_SAY to "prefer_not_to_say"
)
