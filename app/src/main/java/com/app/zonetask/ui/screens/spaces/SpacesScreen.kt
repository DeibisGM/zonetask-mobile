package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.components.SpaceCard
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SpacesScreen(
    snackbarHostState: SnackbarHostState,
    userId: Int,
    modifier: Modifier = Modifier,
    successMessage: String? = null,
    onSuccessMessageShown: () -> Unit = {},
    onSpaceClick: (Space) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: SpacesViewModel = viewModel(
        factory = SpacesViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            userId = userId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SpacesEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(message = successMessage)
            onSuccessMessageShown()
        }
    }

    // Shows loaded errors as snackbars without replacing the whole list.
    LaunchedEffect(uiState.errorBanner) {
        val error = uiState.errorBanner
        if (error != null && uiState.spaces.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearErrorBanner()
        }
    }

    when {
        uiState.isLoading && uiState.spaces.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = UserMessages.Spaces.LOADING,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        uiState.errorBanner != null && uiState.spaces.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = uiState.errorBanner!!,
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { viewModel.fetchSpaces() }) {
                        Text(
                            text = UserMessages.TAP_TO_RETRY_SUFFIX.trim(),
                            color = AppPrimary
                        )
                    }
                }
            }
        }

        uiState.spaces.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = UserMessages.Spaces.EMPTY,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.spaces,
                    key = { it.spaceId }
                ) { space ->
                    val isOwner = space.ownerId == viewModel.userId

                    SpaceCard(
                        space = space,
                        isOwner = isOwner,
                        onClick = { onSpaceClick(space) },
                        isDeleting = uiState.deletingSpaceId == space.spaceId,
                        onDelete = if (isOwner) {
                            { viewModel.deleteSpace(space.spaceId) }
                        } else null,
                        onDeleteNotAllowed = { viewModel.notifyDeleteNotAllowed() }
                    )
                }
            }
        }
    }
}
