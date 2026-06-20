package com.app.zonetask.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.TaskDangerButton
import com.app.zonetask.ui.components.TaskFilledButton
import com.app.zonetask.ui.components.TaskSectionCard
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SettingsScreen(
    onTabSelected: (NavDestination) -> Unit,
    onOpenSpaces: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = AuthSessionStore.currentUser
    val displayName = user?.displayName.orEmpty().ifBlank {
        listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").trim()
    }

    ZoneTaskScaffold(
        title = "Ajustes",
        showBack = false,
        currentDestination = NavDestination.SETTINGS,
        onDestinationSelected = onTabSelected
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TaskSectionCard(
                title = displayName.ifBlank { "Mi cuenta" },
                subtitle = user?.email?.takeIf { it.isNotBlank() } ?: "Cuenta activa"
            ) {
                Text(
                    text = "Gestiona espacios, revisa tu perfil y cierra sesión desde un solo lugar.",
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            TaskSectionCard(title = "Accesos rápidos") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TaskFilledButton(
                        text = "Manage spaces",
                        onClick = onOpenSpaces,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TaskFilledButton(
                        text = "Editar perfil",
                        onClick = onOpenProfile,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            TaskSectionCard(title = "Sesión") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Si necesitas salir de la app, usa el cierre de sesión aquí.",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TaskDangerButton(
                        text = "Cerrar sesión",
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
