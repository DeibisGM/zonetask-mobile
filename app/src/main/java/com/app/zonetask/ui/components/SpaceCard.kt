package com.app.zonetask.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.zonetask.core.UserMessages
import com.app.zonetask.domain.model.Space
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppIconTint
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun SpaceCard(
    space: Space,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        border    = BorderStroke(1.dp, AppBorder),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = space.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            space.description?.let { desc ->
                Text(
                    text  = desc,
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SpaceInfoRow(
                icon = {
                    Icon(
                        imageVector        = Icons.Outlined.Category,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = AppIconTint
                    )
                },
                text = UserMessages.Spaces.TYPE_PREFIX + space.spaceType
            )
        }
    }
}

@Composable
private fun SpaceInfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text  = text,
            color = AppSecondaryText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}