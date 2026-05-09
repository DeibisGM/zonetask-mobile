package com.app.zonetask.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.zonetask.R
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

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
                    BoxLogo()
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

            SidebarItem(
                iconRes = R.drawable.ic_house,
                label = "Inicio",
                onClick = { onNavigate("home") }
            )
            SidebarItem(
                iconRes = R.drawable.ic_check_square,
                label = "Tareas",
                onClick = { onNavigate("task_create") }
            )
            SidebarItem(
                iconRes = R.drawable.ic_buildings,
                label = "Espacios",
                onClick = { onNavigate("spaces") }
            )
            SidebarItem(
                iconRes = R.drawable.ic_chat_circle,
                label = "Chat",
                onClick = { onNavigate("chat") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Divider(color = AppSecondaryText.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))

            SidebarItem(
                iconRes = R.drawable.ic_user,
                label = "Mi Perfil",
                onClick = { onNavigate("profile") }
            )
            SidebarItem(
                iconRes = R.drawable.ic_sign_out,
                label = "Cerrar Sesión",
                onClick = onLogout,
                color = Color(0xFFE57373)
            )
        }
    }
}

@Composable
private fun BoxLogo() {
    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
        Text(
            text = "ZT",
            color = AppBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun SidebarItem(
    @DrawableRes iconRes: Int,
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
            painter = painterResource(id = iconRes),
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
