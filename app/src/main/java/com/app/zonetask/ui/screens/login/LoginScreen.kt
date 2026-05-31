package com.app.zonetask.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.UserResponse
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(AppContainer.userRepository)
    )
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ZoneTask",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage your space easily",
            style = MaterialTheme.typography.bodyLarge,
            color = AppSecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.userIdInput,
            onValueChange = viewModel::onUserIdInputChanged,
            label = { Text("User ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimary,
                unfocusedBorderColor = AppBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AppPrimary,
                focusedLabelColor = AppSecondaryText,
                unfocusedLabelColor = AppSecondaryText,
                focusedContainerColor = AppSurface,
                unfocusedContainerColor = AppSurface
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (uiState.users.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Available: ${uiState.users.joinToString { it.userId.toString() }}",
                style = MaterialTheme.typography.labelSmall,
                color = AppSecondaryText.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.login { onLoginSuccess(it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            enabled = uiState.canSubmit && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = true,
    val users: List<UserResponse> = emptyList(),
    val userIdInput: String = "",
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = userIdInput.toIntOrNull()?.let { it > 0 } == true
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
                            "No users found in database."
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

    fun login(onSuccess: (Int) -> Unit) {
        val userId = uiState.userIdInput.toIntOrNull()

        if (userId == null || userId <= 0) {
            uiState = uiState.copy(errorMessage = "Enter a valid user ID.")
            return
        }

        onSuccess(userId)
    }
}

class LoginViewModelFactory(
    private val userRepository: com.app.zonetask.data.remote.repository.UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(userRepository) as T
}
