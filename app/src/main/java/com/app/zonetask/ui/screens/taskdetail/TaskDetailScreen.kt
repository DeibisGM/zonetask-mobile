package com.app.zonetask.ui.screens.taskdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.TaskResponse
import com.app.zonetask.di.AppContainer
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppError
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import com.app.zonetask.ui.theme.AppTopBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val task: TaskResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class TaskDetailViewModel(
    private val spaceId: Int,
    private val taskId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState(isLoading = true)
            when (val result = AppContainer.taskRepository.getTaskById(taskId)) {
                is ApiResult.Success -> {
                    _uiState.value = TaskDetailUiState(
                        task = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = TaskDetailUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun deleteTask(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val result = AppContainer.taskRepository.deleteTask(taskId)) {
                is ApiResult.Success -> onResult(true, "Task deleted")
                is ApiResult.Error -> onResult(false, result.message)
            }
        }
    }
}

class TaskDetailViewModelFactory(
    private val spaceId: Int,
    private val taskId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TaskDetailViewModel(spaceId, taskId) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    spaceId: Int,
    taskId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEdit: (taskId: Int) -> Unit = {},
    onDeleted: () -> Unit = {},
    viewModel: TaskDetailViewModel = viewModel(
        factory = TaskDetailViewModelFactory(spaceId, taskId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {

        // === Top Bar ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTopBar)
                .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Task Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = AppSecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.loadTask() }) {
                            Text("Retry", color = AppPrimary)
                        }
                    }
                }
            }

            uiState.task != null -> {
                val task = uiState.task!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AppBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            task.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppSecondaryText
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AppBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DetailRow(
                                icon = Icons.Outlined.SpaceDashboard,
                                label = "Assigned zone",
                                value = task.zoneId?.let { "Zone $it" } ?: "None"
                            )
                            HorizontalDivider(
                                color = AppBorder,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )

                            DetailRow(
                                icon = Icons.Outlined.Repeat,
                                label = "Frequency",
                                value = task.frequency.replaceFirstChar { it.uppercase() }
                            )
                            HorizontalDivider(
                                color = AppBorder,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )

                            task.scheduledTime?.takeIf { it.isNotBlank() }?.let { time ->
                                DetailRow(
                                    icon = Icons.Outlined.AccessTime,
                                    label = "Time",
                                    value = time.take(5)
                                )
                                HorizontalDivider(
                                    color = AppBorder,
                                    modifier = Modifier.padding(vertical = 14.dp)
                                )
                            }

                            task.startDate?.takeIf { it.isNotBlank() }?.let { date ->
                                DetailRow(
                                    icon = Icons.Outlined.CalendarMonth,
                                    label = "Due date",
                                    value = date.take(10)
                                )
                                HorizontalDivider(
                                    color = AppBorder,
                                    modifier = Modifier.padding(vertical = 14.dp)
                                )
                            }

                            DetailRow(
                                icon = Icons.Outlined.CheckCircle,
                                label = "Reminder",
                                value = if (task.reminderEnabled) "On" else "Off"
                            )
                        }
                    }

                    deleteError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEdit(task.taskId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                        ) {
                            Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EDIT", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppError)
                        ) {
                            Icon(Icons.Outlined.Delete, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("DELETE", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete task") },
            text = { Text("Are you sure you want to delete this task? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask { success, message ->
                            if (success) {
                                showDeleteDialog = false
                                onDeleted()
                            } else {
                                deleteError = message
                                showDeleteDialog = false
                            }
                        }
                    }
                ) {
                    Text("DELETE", color = AppError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppSecondaryText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = AppSecondaryText
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
