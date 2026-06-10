package com.app.zonetask.ui.screens.invitations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppOnPrimary
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun InviteMemberScreen(
    spaceId: Int,
    invitedBy: Int,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: InviteMemberViewModel = viewModel(
        factory = InviteMemberViewModelFactory(
            invitationRepository = AppContainer.invitationRepository,
            spaceId              = spaceId,
            invitedBy            = invitedBy
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InviteMemberEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text  = UserMessages.Invitations.INTRO,
            color = AppSecondaryText,
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value          = uiState.email,
            onValueChange  = viewModel::onEmailChange,
            label          = { Text(UserMessages.Invitations.EMAIL_LABEL) },
            singleLine     = true,
            isError        = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Next
            ),
            enabled  = !uiState.isSending,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = uiState.message,
            onValueChange = viewModel::onMessageChange,
            label         = { Text(UserMessages.Invitations.MESSAGE_LABEL) },
            minLines      = 3,
            enabled       = !uiState.isSending,
            modifier      = Modifier.fillMaxWidth()
        )

        Button(
            onClick  = viewModel::submit,
            enabled  = uiState.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSending) {
                CircularProgressIndicator(
                    color       = AppOnPrimary,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text  = UserMessages.Invitations.SEND_BUTTON,
                    color = AppOnPrimary
                )
            }
        }
    }
}