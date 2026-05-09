package com.app.zonetask.ui.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText

private val SPACE_TYPE_OPTIONS    = listOf("Casa de habitacion", "Apartamento", "Oficina", "Local comercial")
private val ROTATION_TYPE_OPTIONS = listOf("Manual", "Automático", "Semanal")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSpaceScreen(
    spaceId : Int,
    modifier: Modifier = Modifier,
    onSaved : (successMessage: String) -> Unit = {},
    viewModel: EditSpaceViewModel = viewModel(
        factory = EditSpaceViewModelFactory(
            spaceRepository = AppContainer.spaceRepository,
            spaceId         = spaceId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSaved("Espacio actualizado correctamente")
    }

    if (uiState.isLoadingData) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppPrimary)
        }
        return
    }

    var spaceTypeExpanded    by remember { mutableStateOf(false) }
    var rotationTypeExpanded by remember { mutableStateOf(false) }
    var rotationType         by remember { mutableStateOf(ROTATION_TYPE_OPTIONS.first()) }
    var requireProof         by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ── Avatar ──────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.AddAPhoto,
                contentDescription = "Foto de perfil",
                tint               = AppSecondaryText,
                modifier           = Modifier.size(32.dp)
            )
        }
        Text(text = "Foto de perfil", style = MaterialTheme.typography.bodyMedium, color = AppSecondaryText)

        if (uiState.errorBanner != null) {
            Text(text = uiState.errorBanner!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        FormField(label = "Nombre del espacio") {
            OutlinedTextField(
                value         = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = editTextFieldColors()
            )
        }

        FormField(label = "Descripción") {
            OutlinedTextField(
                value         = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier      = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines      = 4,
                shape         = RoundedCornerShape(12.dp),
                colors        = editTextFieldColors(),
                placeholder   = { Text(text = "Opcional", color = AppSecondaryText) }
            )
        }

        FormField(label = "Tipo de espacio") {
            ExposedDropdownMenuBox(
                expanded         = spaceTypeExpanded,
                onExpandedChange = { spaceTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value         = uiState.spaceType,
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon  = { Icon(Icons.Default.KeyboardArrowDown, null, tint = AppSecondaryText) },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = editTextFieldColors()
                )
                ExposedDropdownMenu(expanded = spaceTypeExpanded, onDismissRequest = { spaceTypeExpanded = false }) {
                    SPACE_TYPE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option) },
                            onClick = { viewModel.onSpaceTypeChange(option); spaceTypeExpanded = false }
                        )
                    }
                }
            }
        }

        FormField(label = "Tipo de Rotación") {
            ExposedDropdownMenuBox(
                expanded         = rotationTypeExpanded,
                onExpandedChange = { rotationTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value         = rotationType,
                    onValueChange = {},
                    readOnly      = true,
                    modifier      = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon  = { Icon(Icons.Default.KeyboardArrowDown, null, tint = AppSecondaryText) },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = editTextFieldColors()
                )
                ExposedDropdownMenu(expanded = rotationTypeExpanded, onDismissRequest = { rotationTypeExpanded = false }) {
                    ROTATION_TYPE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option) },
                            onClick = { rotationType = option; rotationTypeExpanded = false }
                        )
                    }
                }
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text     = "¿Se requiere prueba para completar tareas?",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked         = requireProof,
                onCheckedChange = { requireProof = it },
                colors          = CheckboxDefaults.colors(checkedColor = AppPrimary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick  = { },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp),
            border   = androidx.compose.foundation.BorderStroke(1.dp, AppPrimary)
        ) {
            Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "CREAR PLANO", color = AppPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick  = viewModel::updateSpace,
            enabled  = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AppPrimary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Editar", color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        content()
    }
}

@Composable
private fun editTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AppPrimary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor     = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor   = MaterialTheme.colorScheme.onBackground,
    cursorColor          = AppPrimary
)