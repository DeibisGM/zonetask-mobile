package com.app.zonetask.ui.screens.taskcreate

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.components.*
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppSecondaryText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {},
    viewModel: TaskCreateViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val formOptions = viewModel.formOptionsUiState
    val context = LocalContext.current
    val activity = context as? Activity

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Helper to parse date string back to millis for constraints
    fun parseDate(dateStr: String?): Long? {
        return try { dateStr?.let { dateFormatter.parse(it)?.time } } catch (e: Exception) { null }
    }

    // Date Pickers State
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Constraints: Start Date cannot be after End Date
    val startDatePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val endMillis = parseDate(uiState.endDate)
                return endMillis == null || utcTimeMillis <= endMillis
            }
        }
    )

    // Constraints: End Date cannot be before Start Date
    val endDatePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val startMillis = parseDate(uiState.startDate)
                return startMillis == null || utcTimeMillis >= startMillis
            }
        }
    )

    // Time Picker State
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val dateString = dateFormatter.format(Date(millis))
                        viewModel.updateState { copy(startDate = dateString) }
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val dateString = dateFormatter.format(Date(millis))
                        viewModel.updateState { copy(endDate = dateString) }
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = endDatePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val time = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    viewModel.updateState { copy(scheduledTime = time) }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("CANCELAR") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    TaskCreateScaffold(
        title = UserMessages.Screens.CREATE_TASK_TITLE,
        showBack = true,
        onBackClick = { onNavigate("spaces") },
        onNavigate = onNavigate,
        bottomBar = {
            Column(modifier = Modifier.background(AppBackground)) {
                TaskActionButtonsRow(
                    cancelText = "CANCELAR",
                    saveText = UserMessages.TaskCreate.SAVE_BUTTON,
                    onCancelClick = { viewModel.updateState { TaskCreateUiState() } },
                    onSaveClick = {
                        if (viewModel.validate()) {
                            // TODO: Implement save logic
                        }
                    }
                )
            }
        }
    ) { padding ->
        TaskCreateContent(
            uiState = uiState,
            formOptions = formOptions,
            modifier = modifier.padding(padding),
            onShowStartDate = { showStartDatePicker = true },
            onShowEndDate = { showEndDatePicker = true },
            onShowTime = { showTimePicker = true },
            onUpdate = { update -> viewModel.updateState(update) }
        )
    }
}

@Composable
private fun TaskCreateContent(
    uiState: TaskCreateUiState,
    formOptions: TaskFormOptionsUiState,
    modifier: Modifier = Modifier,
    onShowStartDate: () -> Unit,
    onShowEndDate: () -> Unit,
    onShowTime: () -> Unit,
    onUpdate: (TaskCreateUiState.() -> TaskCreateUiState) -> Unit
) {
    val frequencyOptions = listOf(
        "Una vez" to "once",
        "Diaria" to "daily",
        "Cada 2 días" to "every_2_days",
        "Cada 3 días" to "every_3_days",
        "Semanal" to "weekly",
        "Quincenal" to "biweekly",
        "Mensual" to "monthly",
        "Personalizado" to "custom"
    )

    val targetLevelOptions = listOf(
        "Espacio" to "space",
        "Zona" to "zone",
        "Objeto" to "object"
    )

    val zoneOptions = formOptions.zones.ifEmpty {
        listOf("Cargando zonas..." to "")
    }

    val estimatedTimeOptions = listOf(
        "5 minutos" to "5",
        "15 minutos" to "15",
        "30 minutos" to "30",
        "45 minutos" to "45",
        "1 hora" to "60",
        "1.5 horas" to "90",
        "2 horas" to "120",
        "3 horas" to "180",
        "5 horas" to "300"
    )

    val reminderOptions = listOf(
        "Al momento" to "0",
        "5 minutos antes" to "5",
        "15 minutos antes" to "15",
        "30 minutos antes" to "30",
        "1 hora antes" to "60",
        "2 horas antes" to "120",
        "1 día antes" to "1440"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General Info Section
        TaskSectionCard(
            title = UserMessages.TaskCreate.GENERAL_SECTION,
            subtitle = UserMessages.TaskCreate.INTRO
        ) {
            TaskTextField(
                label = UserMessages.TaskCreate.TITLE_LABEL + " *",
                value = uiState.title,
                onValueChange = { value -> 
                    if (value.length <= 150) onUpdate { copy(title = value) }
                },
                placeholder = "Ej: Limpiar la sala",
                error = if (uiState.showErrors && !uiState.isTitleValid) "Campo obligatorio" else null
            )

            TaskTextField(
                label = UserMessages.TaskCreate.DESCRIPTION_LABEL,
                value = uiState.description,
                onValueChange = { value -> 
                    if (value.length <= 500) onUpdate { copy(description = value) }
                },
                placeholder = "Detalla lo que se debe hacer...",
                singleLine = false
            )
            
            TaskDropdown(
                label = UserMessages.TaskCreate.TARGET_LEVEL_LABEL + " *",
                value = targetLevelOptions.find { it.second == uiState.targetLevel }?.first ?: "Espacio",
                options = targetLevelOptions,
                onOptionSelected = { selectedValue -> 
                    onUpdate { copy(targetLevel = selectedValue) }
                }
            )

            TaskDropdown(
                label = UserMessages.TaskCreate.ZONE_LABEL + " *",
                value = zoneOptions.find { it.second == uiState.zoneId.toString() }?.first ?: "Zona General",
                options = zoneOptions,
                onOptionSelected = { selectedValue ->
                    onUpdate { copy(zoneId = selectedValue.toIntOrNull() ?: 1) }
                }
            )

            TaskDropdown(
                label = UserMessages.TaskCreate.CATEGORY_LABEL,
                value = formOptions.categories.find { it.second == uiState.categoryId.toString() }?.first
                    ?: "Selecciona una categoria",
                options = formOptions.categories.ifEmpty { listOf("Cargando categorias..." to "") },
                onOptionSelected = { selectedValue ->
                    onUpdate { copy(categoryId = selectedValue.toIntOrNull() ?: 1) }
                }
            )
        }

        // Schedule Section
        TaskSectionCard(
            title = UserMessages.TaskCreate.SCHEDULE_SECTION
        ) {
            TaskDropdown(
                label = UserMessages.TaskCreate.FREQUENCY_LABEL + " *",
                value = frequencyOptions.find { it.second == uiState.frequency }?.first ?: "Una vez",
                options = frequencyOptions,
                onOptionSelected = { selectedValue ->
                    onUpdate { copy(frequency = selectedValue) }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f).clickable { onShowStartDate() }) {
                    TaskTextField(
                        label = UserMessages.TaskCreate.START_DATE_LABEL + " *",
                        value = uiState.startDate,
                        onValueChange = {},
                        placeholder = "YYYY-MM-DD",
                        modifier = Modifier.fillMaxWidth(),
                        error = if (uiState.showErrors && uiState.startDate.isBlank()) "Obligatorio" else null
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { onShowStartDate() })
                }

                Box(modifier = Modifier.weight(1f).clickable { onShowEndDate() }) {
                    TaskTextField(
                        label = UserMessages.TaskCreate.END_DATE_LABEL,
                        value = uiState.endDate ?: "",
                        onValueChange = {},
                        placeholder = "Opcional",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { onShowEndDate() })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f).clickable { onShowTime() }) {
                    TaskTextField(
                        label = UserMessages.TaskCreate.TIME_LABEL + " *",
                        value = uiState.scheduledTime,
                        onValueChange = {},
                        placeholder = "HH:MM",
                        modifier = Modifier.fillMaxWidth(),
                        error = if (uiState.showErrors && uiState.scheduledTime.isBlank()) "Obligatorio" else null
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { onShowTime() })
                }
                
                TaskDropdown(
                    label = UserMessages.TaskCreate.ESTIMATED_MINUTES_LABEL + " *",
                    value = estimatedTimeOptions.find { it.second == uiState.estimatedMinutes?.toString() }?.first ?: "No definido",
                    options = estimatedTimeOptions,
                    onOptionSelected = { selectedValue ->
                        onUpdate { copy(estimatedMinutes = selectedValue.toIntOrNull()) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Configuration Section
        TaskSectionCard(
            title = UserMessages.TaskCreate.RULES_SECTION
        ) {
            TaskCheckboxRow(
                label = UserMessages.TaskCreate.ROTATING_LABEL,
                checked = uiState.rotating,
                onCheckedChange = { onUpdate { copy(rotating = it) } }
            )
            
            TaskCheckboxRow(
                label = UserMessages.TaskCreate.REQUIRE_PROOF_LABEL,
                checked = uiState.requiresProof,
                onCheckedChange = { onUpdate { copy(requiresProof = it) } }
            )
            
            TaskCheckboxRow(
                label = UserMessages.TaskCreate.REQUIRE_DESCRIPTION_LABEL,
                checked = uiState.requiresDescription,
                onCheckedChange = { onUpdate { copy(requiresDescription = it) } }
            )

            Divider(color = AppBorder, modifier = Modifier.padding(vertical = 4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = UserMessages.TaskCreate.REMINDER_LABEL,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppOnSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    TaskRadioRow(
                        label = "No",
                        selected = !uiState.reminderEnabled,
                        onSelected = { onUpdate { copy(reminderEnabled = false) } },
                        modifier = Modifier.weight(1f)
                    )
                    TaskRadioRow(
                        label = "Si",
                        selected = uiState.reminderEnabled,
                        onSelected = { onUpdate { copy(reminderEnabled = true) } },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (uiState.reminderEnabled) {
                    TaskDropdown(
                        label = UserMessages.TaskCreate.REMINDER_MINUTES_LABEL,
                        value = reminderOptions.find { it.second == uiState.reminderMinutes.toString() }?.first ?: "15 minutos antes",
                        options = reminderOptions,
                        onOptionSelected = { selectedValue ->
                            onUpdate { copy(reminderMinutes = selectedValue.toIntOrNull() ?: 30) }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
