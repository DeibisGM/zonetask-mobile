package com.app.zonetask.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.app.zonetask.ui.theme.AppTopBar

@Composable
fun ZoneTaskHeaderBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTopBar)
            .padding(start = 20.dp, end = 4.dp, top = 18.dp, bottom = 18.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            actions()
        }
    }
}
