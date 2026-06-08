package com.app.zonetask.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.TaskSectionCard
import com.app.zonetask.ui.components.ZoneTaskHeaderBar
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun ProfileScreen(
    onTabSelected: (NavDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = AuthSessionStore.currentUser
    val displayName = currentUser?.displayName.orEmpty().ifBlank { currentUser?.firstName.orEmpty() }
    val lastName = currentUser?.lastName.orEmpty().ifBlank { "No registrado" }

    ZoneTaskScaffold(
        title = "",
        showTopBar = false,
        currentDestination = NavDestination.PROFILE,
        onDestinationSelected = onTabSelected
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ZoneTaskHeaderBar(title = "Mi perfil")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaskSectionCard(
                    title = "Cuenta",
                    subtitle = "Datos guardados en tu sesión actual"
                ) {
                    ProfileDetailRow(label = "Usuario", value = currentUser?.username.orEmpty())
                    ProfileDetailRow(label = "Correo", value = currentUser?.email.orEmpty())
                    ProfileDetailRow(label = "Nombre", value = displayName)
                    ProfileDetailRow(label = "Apellido", value = lastName)
                }

                TaskSectionCard(title = "Sesión") {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppSecondaryText
        )
        Text(
            text = if (value.isBlank()) "No registrado" else value,
            style = MaterialTheme.typography.bodyLarge,
            color = AppOnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
