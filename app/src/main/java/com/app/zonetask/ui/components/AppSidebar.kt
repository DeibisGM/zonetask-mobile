package com.app.zonetask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Regular

import com.adamglin.phosphoricons.bold.Leaf
import com.adamglin.phosphoricons.regular.Buildings
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.CheckSquare
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.SignOut
import com.adamglin.phosphoricons.regular.User
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

@Composable
fun AppSidebar(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = AppBackground,
        drawerContentColor = AppOnSurface,
        drawerShape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AppPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PhosphorIcons.Bold.Leaf,
                            contentDescription = null,
                            tint = AppBackground
                        )
                    }
                }
                Column {
                    Text(
                        text = "ZoneTask",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppOnSurface
                    )
                    Text(
                        text = "Gestión de Espacios",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppSecondaryText
                    )
                }
            }

            Divider(color = AppSecondaryText.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 24.dp))

            // Navigation Items
            SidebarItem(
                icon = PhosphorIcons.Regular.House,
                label = "Inicio",
                onClick = { onNavigate("home") }
            )
            SidebarItem(
                icon = PhosphorIcons.Regular.CheckSquare,
                label = "Tareas",
                onClick = { onNavigate("task_create") }
            )
            SidebarItem(
                icon = PhosphorIcons.Regular.Buildings,
                label = "Espacios",
                onClick = { onNavigate("spaces") }
            )
            SidebarItem(
                icon = PhosphorIcons.Regular.ChatCircle,
                label = "Chat",
                onClick = { onNavigate("chat") }
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Divider(color = AppSecondaryText.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))

            SidebarItem(
                icon = PhosphorIcons.Regular.User,
                label = "Mi Perfil",
                onClick = { onNavigate("profile") }
            )
            SidebarItem(
                icon = PhosphorIcons.Regular.SignOut,
                label = "Cerrar Sesión",
                onClick = onLogout,
                color = Color(0xFFE57373)
            )
        }
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = AppOnSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontSize = 16.sp
        )
    }
}
