package com.app.zonetask.ui.screens.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.domain.model.FloorPlanTemplate
import com.app.zonetask.domain.model.TemplateZone
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

@Composable
fun TemplateSelectScreen(
    modifier: Modifier = Modifier,
    onSelectTemplate: (templateId: Int) -> Unit,
    onSelectBlank: () -> Unit,
    viewModel: TemplateSelectViewModel = viewModel(
        factory = TemplateSelectViewModelFactory(AppContainer.floorPlanTemplateRepository)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var previewTemplate by remember { mutableStateOf<FloorPlanTemplate?>(null) }

    Box(modifier.fillMaxSize().background(Color(0xFF090B0C))) {
        when {
            state.isLoading -> CircularProgressIndicator(
                color = AppPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
            state.error != null -> Text(
                text = state.error ?: "",
                color = Color(0xFFFFC2CB),
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
            else -> TemplateGrid(
                templates = state.templates,
                // Tapping a template opens an expanded preview; applying only happens on confirm.
                onPreviewTemplate = { previewTemplate = it },
                onSelectBlank = onSelectBlank
            )
        }
    }

    previewTemplate?.let { template ->
        TemplatePreviewDialog(
            template = template,
            onConfirm = {
                previewTemplate = null
                onSelectTemplate(template.templateId)
            },
            onDismiss = { previewTemplate = null }
        )
    }
}

@Composable
private fun TemplateGrid(
    templates: List<FloorPlanTemplate>,
    onPreviewTemplate: (FloorPlanTemplate) -> Unit,
    onSelectBlank: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BlankTemplateCard(onClick = onSelectBlank)
        }
        items(templates) { template ->
            TemplateCard(template = template, onClick = { onPreviewTemplate(template) })
        }
    }
}

@Composable
private fun BlankTemplateCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF111418),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2A3436), RoundedCornerShape(14.dp))
    ) {
        Column(Modifier.padding(bottom = 14.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFF1A1A2A)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = AppSecondaryText, style = MaterialTheme.typography.displaySmall)
            }
            Column(Modifier.padding(horizontal = 12.dp).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Blank plan", color = AppOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text("Start from scratch", color = AppSecondaryText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TemplateCard(template: FloorPlanTemplate, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF111418),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2A3436), RoundedCornerShape(14.dp))
    ) {
        Column(Modifier.padding(bottom = 14.dp)) {
            TemplateMiniPreview(
                zones = template.zones,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            )
            Column(Modifier.padding(horizontal = 12.dp).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(template.name, color = AppOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(template.description, color = AppSecondaryText, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

@Composable
private fun TemplatePreviewDialog(
    template: FloorPlanTemplate,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF111418),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A3436)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(template.name, color = AppOnSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                // Larger preview that keeps the template's real proportions (cols × rows).
                TemplateMiniPreview(
                    zones = template.zones,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .aspectRatio(
                            (template.defaultColumns.toFloat() / template.defaultRows.coerceAtLeast(1))
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF2A3436), RoundedCornerShape(12.dp))
                )

                Text(template.description, color = AppSecondaryText, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${template.zones.size} zones · ${template.defaultColumns}×${template.defaultRows} cells",
                    color = AppPrimary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    "You can move, resize, and edit all zones after applying the template.",
                    color = AppSecondaryText,
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = AppSecondaryText)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                    ) {
                        Text("Use template", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateMiniPreview(zones: List<TemplateZone>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color(0xFF1A1A2A))
        zones.forEach { zone ->
            val fillColor = runCatching {
                Color(android.graphics.Color.parseColor(zone.fillColor))
            }.getOrDefault(Color.Gray)

            drawRect(
                color = fillColor.copy(alpha = 0.85f),
                topLeft = Offset(zone.relativeX * size.width, zone.relativeY * size.height),
                size = Size(zone.relativeWidth * size.width, zone.relativeHeight * size.height)
            )
            drawRect(
                color = Color(0x66111111),
                topLeft = Offset(zone.relativeX * size.width, zone.relativeY * size.height),
                size = Size(zone.relativeWidth * size.width, zone.relativeHeight * size.height),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
