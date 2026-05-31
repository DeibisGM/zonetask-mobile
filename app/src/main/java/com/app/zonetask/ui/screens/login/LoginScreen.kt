package com.app.zonetask.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.UserResponse
import com.app.zonetask.di.AppContainer
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(AppContainer.userRepository)
    )
) {
    val uiState = viewModel.uiState
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Elegí un usuario existente para ver sus espacios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = uiState.userIdInput,
                    onValueChange = viewModel::onUserIdInputChanged,
                    label = { Text("ID de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.emailInput,
                    onValueChange = viewModel::onEmailInputChanged,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (uiState.users.isNotEmpty()) {
                        Text(
                            text = "Usuarios disponibles: ${uiState.users.joinToString { it.userId.toString() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.login { userId, email ->
                            onLoginSuccess(userId, email)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canSubmit && !uiState.isLoading
                ) {
                    Text("Entrar")
                }

                TextButton(
                    onClick = viewModel::useFirstAvailableUser,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.users.isNotEmpty() && !uiState.isLoading
                ) {
                    Text("Usar primer usuario disponible")
                }
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = true,
    val users: List<UserResponse> = emptyList(),
    val userIdInput: String = "",
    val emailInput: String = "",
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = userIdInput.toIntOrNull()?.let { it > 0 } == true &&
                emailInput.isNotBlank()
}

class LoginViewModel(
    private val userRepository: com.app.zonetask.data.remote.repository.UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun loadUsers() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = userRepository.getUsers()) {
                is ApiResult.Success -> {
                    val users = result.data
                    uiState = uiState.copy(
                        isLoading = false,
                        users = users,
                        userIdInput = uiState.userIdInput.ifBlank {
                            users.firstOrNull()?.userId?.toString().orEmpty()
                        },
                        errorMessage = if (users.isEmpty()) {
                            "No se encontraron usuarios en la base de datos."
                        } else {
                            null
                        }
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

    fun onUserIdInputChanged(value: String) {
        uiState = uiState.copy(
            userIdInput = value.filter { it.isDigit() },
            errorMessage = null
        )
    }

    fun onEmailInputChanged(value: String) {
        uiState = uiState.copy(
            emailInput = value,
            errorMessage = null
        )
    }

    fun useFirstAvailableUser() {
        val firstUserId = uiState.users.firstOrNull()?.userId ?: return
        uiState = uiState.copy(userIdInput = firstUserId.toString(), errorMessage = null)
    }

    fun login(onSuccess: (Int, String) -> Unit) {
        val userId = uiState.userIdInput.toIntOrNull()

        if (userId == null || userId <= 0) {
            uiState = uiState.copy(errorMessage = "Ingresá un ID de usuario válido.")
            return
        }

        if (uiState.emailInput.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresá un correo electrónico.")
            return
        }

        onSuccess(userId, uiState.emailInput.trim())
    }
}

class LoginViewModelFactory(
    private val userRepository: com.app.zonetask.data.remote.repository.UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(userRepository) as T
}