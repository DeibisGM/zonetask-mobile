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
import com.app.zonetask.data.auth.FirebaseAuthRepository
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.repository.UserRepository
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.AuthCard
import com.app.zonetask.ui.components.AuthHeader
import com.app.zonetask.ui.components.AuthNote
import com.app.zonetask.ui.components.AuthPasswordField
import com.app.zonetask.ui.components.AuthPrimaryButton
import com.app.zonetask.ui.components.AuthScreenShell
import com.app.zonetask.ui.components.AuthStatusMessage
import com.app.zonetask.ui.components.AuthTextField
import com.app.zonetask.ui.theme.AppSecondaryText
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            authRepository = AppContainer.authRepository,
            userRepository = AppContainer.userRepository
        )
    )
) {
    val uiState = viewModel.uiState

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
                title = "ZoneTask",
                subtitle = "Inicia sesión con tu correo y contraseña para continuar."
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthCard(modifier = Modifier.fillMaxWidth()) {
                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Correo",
                    placeholder = "tu-correo@ejemplo.com",
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

                AuthPasswordField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Contraseña",
                    placeholder = "Escribe tu contraseña",
                    error = uiState.passwordError,
                    isVisible = uiState.isPasswordVisible,
                    onVisibilityToggle = viewModel::togglePasswordVisibility,
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.login() }
                    )
                )

                AuthStatusMessage(message = uiState.errorMessage)

                AuthPrimaryButton(
                    text = "Ingresar",
                    onClick = viewModel::login,
                    loading = uiState.isLoading,
                    enabled = uiState.canSubmit
                )

                AuthNote(
                    text = "La validación se hace con Firebase Auth y luego se enlaza con tu usuario del backend."
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuthNote(
                text = "Si el inicio falla, revisa que `google-services.json` esté en `app/` y que Email/Password esté habilitado en Firebase Auth."
            )

            Spacer(modifier = Modifier.height(10.dp))

            androidx.compose.material3.Text(
                text = "Esta es la pantalla de entrada que verás al abrir la app.",
                color = AppSecondaryText.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )

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
    val emailError: String? = when {
        email.isBlank() -> "Ingresa tu correo."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Correo no válido."
        else -> null
    }

    val passwordError: String? = when {
        password.isBlank() -> "Ingresa tu contraseña."
        else -> null
    }

    val canSubmit: Boolean
        get() = emailError == null && passwordError == null && !isLoading
}

class LoginViewModel(
    private val authRepository: FirebaseAuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(
            email = value.trimStart(),
            errorMessage = null,
            resolvedUserId = null
        )
    }

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
        // El siguiente campo ya recibe foco por el teclado.
    }

    fun login() {
        val emailError = uiState.emailError
        val passwordError = uiState.passwordError
        if (emailError != null || passwordError != null) {
            uiState = uiState.copy(
                errorMessage = emailError ?: passwordError
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, resolvedUserId = null)

        viewModelScope.launch {
            authRepository.signIn(uiState.email.trim(), uiState.password)
                .onSuccess { sessionUser ->
                    when (val result = userRepository.resolveUserIdByEmail(sessionUser.email)) {
                        is ApiResult.Success -> {
                            uiState = uiState.copy(
                                isLoading = false,
                                errorMessage = null,
                                resolvedUserId = result.data
                            )
                        }

                        is ApiResult.Error -> {
                            authRepository.signOut()
                            uiState = uiState.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "No se pudo iniciar sesión."
                    )
                }
        }
    }
}

class LoginViewModelFactory(
    private val authRepository: FirebaseAuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository, userRepository) as T
    }
}
