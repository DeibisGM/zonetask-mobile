package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.components.SpaceCard
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

import com.app.zonetask.ui.components.ZoneTaskScaffold

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

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(message = successMessage)
            onSuccessMessageShown()
        }
    }

    ZoneTaskScaffold(
        title = UserMessages.Screens.SPACES_TITLE,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate
    ) { padding ->
        val errorBanner = uiState.errorBanner

        Box(modifier = modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier        = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = UserMessages.Spaces.LOADING,
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                errorBanner != null -> {
                    Box(
                        modifier        = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { viewModel.fetchSpaces() }) {
                            Text(
                                text  = errorBanner,
                                color = AppPrimary            // teal accent on retry tap
                            )
                        }
                    }
                }

                uiState.spaces.isEmpty() -> {
                    Box(
                        modifier        = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = UserMessages.Spaces.EMPTY,
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 12.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.spaces) { space ->
                            SpaceCard(
                                space   = space,
                                onClick = { onSpaceClick(space) }
                            )
                        }
                    }
                }
            }
        }
    }
}