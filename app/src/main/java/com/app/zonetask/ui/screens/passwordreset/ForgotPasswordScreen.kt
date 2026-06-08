package com.app.zonetask.ui.screens.passwordreset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.components.AuthCard
import com.app.zonetask.ui.components.AuthHeader
import com.app.zonetask.ui.components.AuthNote
import com.app.zonetask.ui.components.AuthPrimaryButton
import com.app.zonetask.ui.components.AuthScreenShell
import com.app.zonetask.ui.components.AuthStatusMessage
import com.app.zonetask.ui.components.AuthTextField
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModelFactory(AppContainer.authRepository)
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
                title = UserMessages.PasswordReset.REQUEST_TITLE,
                subtitle = UserMessages.PasswordReset.REQUEST_SUBTITLE
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthCard(modifier = Modifier.fillMaxWidth()) {
                // This screen only requests the email so Firebase can send the recovery link.
                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = UserMessages.Login.EMAIL_LABEL,
                    placeholder = UserMessages.Login.EMAIL_PLACEHOLDER,
                    error = uiState.emailError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { viewModel.requestReset() }
                    )
                )

                AuthStatusMessage(message = uiState.errorMessage)

                if (uiState.infoMessage != null) {
                    AuthNote(text = uiState.infoMessage)
                }

                // The backend only needs the email to trigger Firebase's recovery message.
                AuthPrimaryButton(
                    text = UserMessages.PasswordReset.REQUEST_BUTTON,
                    onClick = viewModel::requestReset,
                    loading = uiState.isLoading,
                    enabled = uiState.canSubmit
                )

                TextButton(
                    onClick = onBackToLogin,
                    enabled = !uiState.isLoading
                ) {
                    androidx.compose.material3.Text(text = UserMessages.PasswordReset.REQUEST_BACK_TO_LOGIN)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    val emailError: String?
        get() = when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> UserMessages.Login.EMAIL_INVALID
            else -> null
        }

    val canSubmit: Boolean
        get() = email.isNotBlank() && !isLoading
}

class ForgotPasswordViewModel(
    private val authRepository: BackendAuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value.trimStart(), errorMessage = null, infoMessage = null)
    }

    fun requestReset() {
        if (uiState.email.isBlank()) {
            uiState = uiState.copy(errorMessage = UserMessages.Login.EMAIL_REQUIRED, infoMessage = null)
            return
        }

        if (uiState.emailError != null) {
            uiState = uiState.copy(errorMessage = uiState.emailError, infoMessage = null)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        viewModelScope.launch {
            when (val result = authRepository.requestPasswordReset(uiState.email.trim())) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        infoMessage = UserMessages.PasswordReset.REQUEST_SUCCESS,
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

class ForgotPasswordViewModelFactory(
    private val authRepository: BackendAuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ForgotPasswordViewModel(authRepository) as T
    }
}
