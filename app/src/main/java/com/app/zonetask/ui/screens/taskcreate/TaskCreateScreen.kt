package com.app.zonetask.ui.screens.taskcreate

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.core.UserMessages
import com.app.zonetask.ui.components.*
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppOnSurface
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppTopBar
import com.app.zonetask.ui.theme.AppSecondaryText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateScreen(
    initialSpaceId: Int = 1,
    initialCreatedBy: Int = 1,
    taskId: Int? = null,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {},
    onClose: () -> Unit = {},
    viewModel: TaskCreateViewModel = viewModel(
        factory = TaskCreateViewModelFactory(
            initialSpaceId = initialSpaceId,
            initialCreatedBy = initialCreatedBy,
            taskId = taskId
        )
    )
) {
    val uiState = viewModel.uiState
    val formOptions = viewModel.formOptionsUiState
    val context = LocalContext.current
    val activity = context as? Activity

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Convert the saved date string into millis for picker limits.
    fun parseDate(dateStr: String?): Long? {
        return try { dateStr?.let { dateFormatter.parse(it)?.time } } catch (e: Exception) { null }
    }

    // Picker visibility flags.
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var saveErrorMessage by remember { mutableStateOf<String?>(null) }

    // Keep the start date on or before the selected end date.
    val startDatePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val endMillis = parseDate(uiState.endDate)
                return endMillis == null || utcTimeMillis <= endMillis
            }
        }
    )

    // Keep the end date on or after the selected start date.
    val endDatePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val startMillis = parseDate(uiState.startDate)
                return startMillis == null || utcTimeMillis >= startMillis
            }
        }
    )

    // Default time picker state.
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
                TextButton(onClick = { showTimePicker = false }) {                 Text("CANCEL") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (saveErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { saveErrorMessage = null },
            confirmButton = {
                TextButton(onClick = { saveErrorMessage = null }) {
                    Text("OK")
                }
            },
            title = { Text("Save error") },
            text = {
                Text(
                    text = saveErrorMessage.orEmpty(),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }

    val isWaitingForInitialData =
        formOptions.isLoading ||
            (viewModel.isEditMode && uiState.objectSelectionEnabled && formOptions.objectsLoading && uiState.selectedObjectIds.isNotEmpty())

    LaunchedEffect(uiState.zoneId, uiState.objectSelectionEnabled) {
        if (uiState.objectSelectionEnabled && uiState.zoneId != null) {
            viewModel.loadZoneObjects(uiState.zoneId)
        }
    }

    TaskCreateScaffold(
        title = if (viewModel.isEditMode) "Editar tarea" else UserMessages.Screens.CREATE_TASK_TITLE,
        showBack = true,
        onBackClick = onClose,
        onNavigate = onNavigate,
        topBarColor = AppTopBar,
        bottomBar = {
            Column(modifier = Modifier.background(AppBackground)) {
                if (!isWaitingForInitialData) {
                    TaskActionButtonsRow(
                        cancelText = "Cancel",
                        saveText = "Save",
                        onCancelClick = {
                            if (viewModel.isEditMode) {
                                onClose()
                            } else {
                                viewModel.updateState { TaskCreateUiState() }
                            }
                        },
                        onSaveClick = {
                            // Only clear the form after a successful save.
                            if (viewModel.validate()) {
                                viewModel.saveTask { success, message ->
                                    if (success) {
                                        saveErrorMessage = null
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (viewModel.isEditMode) {
                                            onClose()
                                        } else {
                                            viewModel.resetForm()
                                        }
                                    } else {
                                        saveErrorMessage = message
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        if (isWaitingForInitialData) {
            Box(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading form...",
                        color = AppSecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
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
        "Once" to "once",
        "Daily" to "daily",
        "Every 2 days" to "every_2_days",
        "Every 3 days" to "every_3_days",
        "Weekly" to "weekly",
        "Biweekly" to "biweekly",
        "Monthly" to "monthly",
        "Custom" to "custom"
    )

    val targetLevelOptions = listOf(
        "Space" to "space",
        "Zone" to "zone",
        "Object" to "object"
    )

    val zoneOptions = formOptions.zones.ifEmpty {
        listOf("Loading zones..." to "")
    }

    val estimatedTimeOptions = listOf(
        "5 min" to "5",
        "15 min" to "15",
        "30 min" to "30",
        "45 min" to "45",
        "1 hour" to "60",
        "1.5 hours" to "90",
        "2 hours" to "120",
        "3 hours" to "180",
        "5 hours" to "300"
    )

    val reminderOptions = listOf(
        "At time" to "0",
        "5 min before" to "5",
        "15 min before" to "15",
        "30 min before" to "30",
        "1 hour before" to "60",
        "2 hours before" to "120",
        "1 day before" to "1440"
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
                               placeholder = "E.g. Clean the kitchen",
                error = if (uiState.showErrors && !uiState.isTitleValid) "Campo obligatorio" else null
            )

            TaskTextField(
                label = UserMessages.TaskCreate.DESCRIPTION_LABEL,
                value = uiState.description,
                onValueChange = { value -> 
                    if (value.length <= 500) onUpdate { copy(description = value) }
                },
                               placeholder = "Describe what needs to be done...",
                singleLine = false
            )
            
            TaskDropdown(
                label = UserMessages.TaskCreate.TARGET_LEVEL_LABEL + " *",
                    value = targetLevelOptions.find { it.second == uiState.targetLevel }?.first ?: "Space",
                options = targetLevelOptions,
                onOptionSelected = { selectedValue -> 
                    onUpdate { copy(targetLevel = selectedValue) }
                }
            )

            TaskDropdown(
                label = UserMessages.TaskCreate.ZONE_LABEL + " *",
                    value = zoneOptions.find { it.second == uiState.zoneId.toString() }?.first ?: "General Zone",
                options = zoneOptions,
                onOptionSelected = { selectedValue ->
                    onUpdate { copy(zoneId = selectedValue.toIntOrNull() ?: 1, selectedObjectIds = emptyList()) }
                }
            )

            TaskDropdown(
                label = UserMessages.TaskCreate.CATEGORY_LABEL,
                    value = formOptions.categories.find { it.second == uiState.categoryId.toString() }?.first
                        ?: "Select a category",
                options = formOptions.categories.ifEmpty { listOf("Loading categories..." to "") },
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
                value = frequencyOptions.find { it.second == uiState.frequency }?.first ?: "Once",
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
                        placeholder = "Optional",
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
                    value = estimatedTimeOptions.find { it.second == uiState.estimatedMinutes?.toString() }?.first ?: "Not set",
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
                        label = "Yes",
                        selected = uiState.reminderEnabled,
                        onSelected = { onUpdate { copy(reminderEnabled = true) } },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (uiState.reminderEnabled) {
                    TaskDropdown(
                        label = UserMessages.TaskCreate.REMINDER_MINUTES_LABEL,
                        value = reminderOptions.find { it.second == uiState.reminderMinutes.toString() }?.first ?: "15 min before",
                        options = reminderOptions,
                        onOptionSelected = { selectedValue ->
                            onUpdate { copy(reminderMinutes = selectedValue.toIntOrNull() ?: 30) }
                        }
                    )
                }

                Divider(color = AppBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Objects in zone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppOnSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    TaskRadioRow(
                        label = "No",
                        selected = !uiState.objectSelectionEnabled,
                        onSelected = {
                            onUpdate { copy(objectSelectionEnabled = false, selectedObjectIds = emptyList()) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TaskRadioRow(
                        label = "Yes",
                        selected = uiState.objectSelectionEnabled,
                        onSelected = {
                            onUpdate { copy(objectSelectionEnabled = true) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (uiState.objectSelectionEnabled) {
                    if (uiState.zoneId == null) {
                        Text(
                            text = "Select a zone first",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppSecondaryText
                        )
                    } else if (formOptions.objectsLoading) {
                        Text(
                            text = "Loading objects...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppSecondaryText
                        )
                    } else if (formOptions.objects.isEmpty()) {
                        Text(
                            text = "No objects in this zone",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppSecondaryText
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            formOptions.objects.forEach { (objectName, objectValue) ->
                                val objectId = objectValue.toIntOrNull()
                                if (objectId != null) {
                                    TaskCheckboxRow(
                                        label = objectName,
                                        checked = uiState.selectedObjectIds.contains(objectId),
                                        onCheckedChange = { checked ->
                                            onUpdate {
                                                val updated = if (checked) {
                                                    selectedObjectIds + objectId
                                                } else {
                                                    selectedObjectIds - objectId
                                                }
                                                copy(selectedObjectIds = updated.distinct())
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
