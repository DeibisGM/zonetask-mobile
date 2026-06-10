package com.app.zonetask.ui.screens.login

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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
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
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    authNotice: String? = null,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(AppContainer.authRepository)
    )
) {
    val uiState = viewModel.uiState

    // Once Firebase-backed login resolves a valid local user id, the screen
    // delegates navigation back to the app shell.
    LaunchedEffect(uiState.resolvedUserId) {
        uiState.resolvedUserId?.let(onLoginSuccess)
    }

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
                title = UserMessages.Login.TITLE,
                subtitle = UserMessages.Login.SUBTITLE
            )

            // The auth notice is injected only after a successful sign-up or password reset.
            if (!authNotice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AuthNote(text = authNotice)
            }

            Spacer(modifier = Modifier.height(24.dp))

            AuthCard(modifier = Modifier.fillMaxWidth()) {
                // Email validation runs live while the empty field remains visually neutral.
                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = UserMessages.Login.EMAIL_LABEL,
                    placeholder = UserMessages.Login.EMAIL_PLACEHOLDER,
                    error = uiState.emailError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { viewModel.requestFocusPassword() }
                    )
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Password visibility and submit handling are delegated to the shared auth components.
                    AuthPasswordField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = UserMessages.Login.PASSWORD_LABEL,
                        placeholder = UserMessages.Login.PASSWORD_PLACEHOLDER,
                        error = uiState.passwordError,
                        isVisible = uiState.isPasswordVisible,
                        onVisibilityToggle = viewModel::togglePasswordVisibility,
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.login() }
                        )
                    )

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(
                            onClick = onForgotPassword,
                            enabled = !uiState.isLoading,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(
                                text = UserMessages.Login.FORGOT_PASSWORD,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AuthStatusMessage(message = uiState.errorMessage)

                    AuthPrimaryButton(
                        text = UserMessages.Login.SUBMIT,
                        onClick = viewModel::login,
                        loading = uiState.isLoading,
                        enabled = uiState.canSubmit
                    )

                    TextButton(
                        onClick = onCreateAccount,
                        enabled = !uiState.isLoading
                    ) {
                        Text(text = UserMessages.Register.TITLE)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resolvedUserId: Int? = null
) {
    // Live validation keeps the UI responsive without flagging empty fields as errors.
    val emailError: String?
        get() = when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> UserMessages.Login.EMAIL_INVALID
            else -> null
        }

    val passwordError: String?
        get() = when {
            password.isBlank() -> null
            else -> null
        }

    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

class LoginViewModel(
    private val authRepository: BackendAuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
    private set

    // Keeps the form state in sync with typed input and clears stale server errors.
    fun onEmailChanged(value: String) {
        uiState = uiState.copy(
            email = value.trimStart(),
            errorMessage = null,
            resolvedUserId = null
        )
    }

    // Stores the password exactly as entered so the backend can forward it to Firebase Auth.
    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(
            password = value,
            errorMessage = null,
            resolvedUserId = null
        )
    }

    fun togglePasswordVisibility() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun requestFocusPassword() {

    }

    fun login() {
        // The submit action reuses the same validation rules before contacting the backend.
        val emailError = uiState.emailError
        val passwordError = uiState.passwordError
        if (emailError != null || passwordError != null) {
            uiState = uiState.copy(
                errorMessage = emailError ?: passwordError,
                isLoading = false
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, resolvedUserId = null)

        viewModelScope.launch {
            when (val result = authRepository.login(uiState.email.trim(), uiState.password)) {
                is ApiResult.Success -> {
                    val userId = result.data.user?.userId
                    if (userId != null && userId > 0) {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = null,
                            resolvedUserId = userId
                        )
                    } else {
                        authRepository.clearSession()
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = "El servidor no devolvió un usuario válido."
                        )
                    }
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
}

class LoginViewModelFactory(
    private val authRepository: BackendAuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository) as T
    }
}
