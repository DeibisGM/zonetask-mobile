package com.app.zonetask.ui.screens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.data.auth.BackendAuthRepository
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.RegisterRequest
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.AuthHeader
import com.app.zonetask.ui.components.AuthNote
import com.app.zonetask.ui.components.AuthPasswordField
import com.app.zonetask.ui.components.AuthPrimaryButton
import com.app.zonetask.ui.components.AuthScreenShell
import com.app.zonetask.ui.components.AuthStatusMessage
import com.app.zonetask.ui.components.AuthTextField
import com.app.zonetask.ui.components.TaskSectionCard
import com.app.zonetask.ui.components.TaskDropdown
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onBackToLogin: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(AppContainer.authRepository)
    )
) {
    val uiState = viewModel.uiState

    // When sign-up succeeds, the screen hands control back to the login flow
    // with the verification notice that should be shown exactly once.
    LaunchedEffect(uiState.registrationCompleted) {
        if (uiState.registrationCompleted) {
            onBackToLogin(uiState.infoMessage ?: UserMessages.Login.REGISTRATION_NOTICE)
        }
    }

    AuthScreenShell(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            // The header remains fixed so the form can scroll independently in sections.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader(
                    title = UserMessages.Register.TITLE,
                    subtitle = UserMessages.Register.SUBTITLE
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TaskSectionCard(
                        title = UserMessages.Register.ACCOUNT_SECTION,
                        subtitle = UserMessages.Register.ACCOUNT_SUBTITLE
                    ) {
                        AuthTextField(
                            value = uiState.username,
                            onValueChange = viewModel::onUsernameChanged,
                            label = UserMessages.Register.USERNAME_LABEL,
                            placeholder = UserMessages.Register.USERNAME_PLACEHOLDER,
                            error = uiState.usernameError,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Badge, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        AuthTextField(
                            value = uiState.firstName,
                            onValueChange = viewModel::onFirstNameChanged,
                            label = UserMessages.Register.FIRST_NAME_LABEL,
                            placeholder = UserMessages.Register.FIRST_NAME_PLACEHOLDER,
                            error = uiState.firstNameError,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        AuthTextField(
                            value = uiState.lastName,
                            onValueChange = viewModel::onLastNameChanged,
                            label = UserMessages.Register.LAST_NAME_LABEL,
                            placeholder = UserMessages.Register.LAST_NAME_PLACEHOLDER,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
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
                            onOptionSelected = viewModel::onGenderChanged
                        )
                    }
                }

                item {
                    TaskSectionCard(
                        title = UserMessages.Register.CREDENTIALS_SECTION,
                        subtitle = UserMessages.Register.CREDENTIALS_SUBTITLE
                    ) {
                        AuthTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChanged,
                            label = UserMessages.Login.EMAIL_LABEL,
                            placeholder = UserMessages.Login.EMAIL_PLACEHOLDER,
                            error = uiState.emailError,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.AlternateEmail, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        AuthPasswordField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChanged,
                            label = UserMessages.Login.PASSWORD_LABEL,
                            placeholder = UserMessages.Login.PASSWORD_PLACEHOLDER,
                            error = uiState.passwordError,
                            isVisible = uiState.isPasswordVisible,
                            onVisibilityToggle = viewModel::togglePasswordVisibility,
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.register() }
                            )
                        )

                        AuthPasswordField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            label = UserMessages.Register.CONFIRM_PASSWORD_LABEL,
                            placeholder = UserMessages.Register.CONFIRM_PASSWORD_PLACEHOLDER,
                            error = uiState.confirmPasswordError,
                            isVisible = uiState.isPasswordVisible,
                            onVisibilityToggle = viewModel::togglePasswordVisibility,
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.register() }
                            )
                        )
                    }
                }

                item {
                    TaskSectionCard(
                        title = UserMessages.Register.OPTIONAL_SECTION,
                        subtitle = UserMessages.Register.OPTIONAL_SUBTITLE
                    ) {
                        AuthTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChanged,
                            label = UserMessages.Register.PHONE_LABEL,
                            placeholder = UserMessages.Register.PHONE_PLACEHOLDER,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Call, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        )

                        AuthTextField(
                            value = uiState.bio,
                            onValueChange = viewModel::onBioChanged,
                            label = UserMessages.Register.BIO_LABEL,
                            placeholder = UserMessages.Register.BIO_PLACEHOLDER,
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
                            },
                            singleLine = false
                        )
                    }
                }

                item {
                    TaskSectionCard(
                        title = UserMessages.Register.FINISH_SECTION,
                        subtitle = UserMessages.Register.FINISH_SUBTITLE
                    ) {
                        AuthStatusMessage(message = uiState.errorMessage)

                        if (uiState.infoMessage != null) {
                            AuthNote(text = uiState.infoMessage)
                        }

                        AuthPrimaryButton(
                            text = UserMessages.Register.SUBMIT,
                            onClick = viewModel::register,
                            loading = uiState.isLoading,
                            enabled = uiState.canSubmit
                        )

                        TextButton(onClick = { onBackToLogin(null) }, enabled = !uiState.isLoading) {
                            Text(text = UserMessages.Register.BACK_TO_LOGIN)
                        }
                    }
                }
            }
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
    // Username validation is kept live so the form can show the error without waiting for submit.
    val usernameError: String?
        get() = when {
            username.isBlank() -> null
            username.length < 3 -> UserMessages.Register.USERNAME_TOO_SHORT
            else -> null
        }

    // Optional fields stay neutral and do not block the flow.
    val firstNameError: String?
        get() = when {
            firstName.isBlank() -> null
            else -> null
        }

    // Email validation mirrors the login screen so both flows behave consistently.
    val emailError: String?
        get() = when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> UserMessages.Register.EMAIL_INVALID
            else -> null
        }

    // Password rules are checked before calling the backend so Firebase receives only valid input.
    val passwordError: String?
        get() = when {
            password.isBlank() -> null
            password.length < 8 -> UserMessages.Register.PASSWORD_MIN_LENGTH
            else -> null
        }

    // Both password fields must match before the account creation request is sent.
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

    fun onUsernameChanged(value: String) {
        // Keeps the local form state fresh while clearing any stale backend notice.
        uiState = uiState.copy(username = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onFirstNameChanged(value: String) {
        uiState = uiState.copy(firstName = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onLastNameChanged(value: String) {
        uiState = uiState.copy(lastName = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun onGenderChanged(value: String) {
        // The selected value is stored as the backend-friendly gender code.
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
        // The submit action uses the same validation rules before calling the backend.
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
            // The backend creates the Firebase account, persists the local user, and returns verification status.
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
                    // Successful sign-up keeps the success note available for the login screen.
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
                    // Server-side errors stay on the registration screen so the user can fix the form.
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
