package com.app.zonetask.ui.screens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.app.zonetask.ui.components.AuthCard
import com.app.zonetask.ui.components.AuthHeader
import com.app.zonetask.ui.components.AuthNote
import com.app.zonetask.ui.components.AuthPasswordField
import com.app.zonetask.ui.components.AuthPrimaryButton
import com.app.zonetask.ui.components.AuthScreenShell
import com.app.zonetask.ui.components.AuthStatusMessage
import com.app.zonetask.ui.components.AuthTextField
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(AppContainer.authRepository)
    )
) {
    val uiState = viewModel.uiState

    AuthScreenShell(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AuthHeader(
                title = UserMessages.Register.TITLE,
                subtitle = UserMessages.Register.SUBTITLE
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthCard(modifier = Modifier.fillMaxWidth()) {
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

                AuthTextField(
                    value = uiState.gender,
                    onValueChange = viewModel::onGenderChanged,
                    label = UserMessages.Register.GENDER_LABEL,
                    placeholder = UserMessages.Register.GENDER_PLACEHOLDER,
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = UserMessages.Register.EMAIL_LABEL,
                    placeholder = UserMessages.Register.EMAIL_PLACEHOLDER,
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
                    label = UserMessages.Register.PASSWORD_LABEL,
                    placeholder = UserMessages.Register.PASSWORD_PLACEHOLDER,
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

                AuthStatusMessage(message = uiState.errorMessage)

                if (uiState.infoMessage != null) {
                    AuthNote(text = uiState.infoMessage)
                }

                if (uiState.registrationCompleted) {
                    AuthPrimaryButton(
                        text = UserMessages.Register.BACK_TO_LOGIN,
                        onClick = onBackToLogin,
                        loading = false,
                        enabled = true
                    )
                } else {
                    AuthPrimaryButton(
                        text = UserMessages.Register.SUBMIT,
                        onClick = viewModel::register,
                        loading = uiState.isLoading,
                        enabled = uiState.canSubmit
                    )
                    TextButton(
                        onClick = onBackToLogin,
                        enabled = !uiState.isLoading
                    ) {
                        androidx.compose.material3.Text(text = UserMessages.Register.BACK_TO_LOGIN)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class RegisterUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
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
        get() = if (username.isBlank()) null else null

    val firstNameError: String?
        get() = if (firstName.isBlank()) null else null

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

    fun onUsernameChanged(value: String) {
        uiState = uiState.copy(
            username = value.trimStart(),
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onFirstNameChanged(value: String) {
        uiState = uiState.copy(
            firstName = value.trimStart(),
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onLastNameChanged(value: String) {
        uiState = uiState.copy(
            lastName = value.trimStart(),
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onGenderChanged(value: String) {
        uiState = uiState.copy(
            gender = value.trimStart(),
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(
            email = value.trimStart(),
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(
            password = value,
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onConfirmPasswordChanged(value: String) {
        uiState = uiState.copy(
            confirmPassword = value,
            errorMessage = null,
            infoMessage = null
        )
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
                            "Account created, but the verification email could not be sent yet."
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
