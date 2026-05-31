package com.app.zonetask.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.ui.components.FloorPlanCanvas
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface
import com.app.zonetask.ui.theme.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    spaceId: Int,
    userId: Int,
    modifier: Modifier = Modifier,
    onNavigateToCreateSpace: () -> Unit = {},
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToManageSpaces: () -> Unit = {},
    onNavigateToTaskDetail: (spaceId: Int, taskId: Int) -> Unit = { _, _ -> },
    onSpaceChanged: (newSpaceId: Int) -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(spaceId = spaceId, userId = userId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSpacePicker by remember { mutableStateOf(false) }
    var tasksExpanded by remember { mutableStateOf(true) }

    // Notify parent when first space is resolved from spaceId=0
    LaunchedEffect(uiState.currentSpaceId) {
        val resolved = uiState.currentSpaceId
        if (spaceId == 0 && resolved != null && resolved > 0) {
            onSpaceChanged(resolved)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTopBar)
                    .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSpacePicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.spaceName.ifBlank { "ZoneTask" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Switch space",
                        tint = AppSecondaryText,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(22.dp)
                    )
                }

                if (uiState.currentSpaceId != null && uiState.currentSpaceId!! > 0) {
                    IconButton(onClick = onNavigateToManageSpaces) {
                        Icon(
                            painter = painterResource(id = com.app.zonetask.R.drawable.ic_buildings),
                            contentDescription = "Spaces",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToCreateTask) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add task",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Plan area — fills available space, plan centers in visible area
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }

                uiState.errorMessage != null && uiState.userSpaces.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = AppSecondaryText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToCreateSpace,
                                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                            ) {
                                Text("Create your first space")
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(AppSurface)
                    ) {
                        if (uiState.activePlan != null) {
                            FloorPlanCanvas(
                                worldWidth = uiState.activePlan!!.canvasWidth,
                                worldHeight = uiState.activePlan!!.canvasHeight,
                                bottomInset = if (tasksExpanded) 280.dp else 64.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No floor plan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppSecondaryText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create one from your spaces",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppSecondaryText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // === Tasks card (fixed bottom, collapsible) ===
        if (!uiState.isLoading && uiState.currentSpaceId != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                // Collapsed bar (always visible when not loading)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clickable { tasksExpanded = !tasksExpanded },
                    color = AppCardElevated,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Today's tasks",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = AppPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${uiState.pendingTasks.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (tasksExpanded)
                                Icons.Outlined.KeyboardArrowDown
                            else Icons.Outlined.KeyboardArrowUp,
                            contentDescription = if (tasksExpanded) "Collapse" else "Expand",
                            tint = AppSecondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Expanded task list
                AnimatedVisibility(
                    visible = tasksExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppCardElevated
                    ) {
                        if (uiState.pendingTasks.isEmpty()) {
                            Text(
                                text = "No pending tasks for today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppSecondaryText,
                                modifier = Modifier.padding(
                                    start = 20.dp, end = 20.dp, bottom = 20.dp
                                )
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 210.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(uiState.pendingTasks) { task ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val sid = uiState.currentSpaceId ?: spaceId
                                                onNavigateToTaskDetail(sid, task.taskId)
                                            },
                                        color = Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                vertical = 10.dp,
                                                horizontal = 20.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = when (task.dueStatusKey) {
                                                    "overdue" -> Color(0xFFE57373)
                                                    "upcoming" -> AppPrimary
                                                    else -> AppSecondaryText.copy(alpha = 0.5f)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (task.assigneeName != null) {
                                                    Text(
                                                        text = task.assigneeName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = AppSecondaryText
                                                    )
                                                }
                                            }
                                            if (task.scheduledTime != null) {
                                                Text(
                                                    text = task.scheduledTime.take(5),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AppSecondaryText
                                                )
                                            }
                                        }
                                    }
                                    if (task != uiState.pendingTasks.last()) {
                                        HorizontalDivider(
                                            color = AppBorder,
                                            modifier = Modifier.padding(horizontal = 20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // === Space Picker Bottom Sheet ===
    if (showSpacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSpacePicker = false },
            containerColor = AppTopBar,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Your spaces",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppSecondaryText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                uiState.userSpaces.forEach { space ->
                    val isActive = space.spaceId == uiState.currentSpaceId
                    Surface(
                        onClick = {
                            onSpaceChanged(space.spaceId)
                            showSpacePicker = false
                        },
                        color = if (isActive) AppPrimary.copy(alpha = 0.10f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = space.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isActive) AppPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isActive) FontWeight.Medium
                                    else FontWeight.Normal
                                )
                                Text(
                                    text = space.spaceType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppSecondaryText
                                )
                            }
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = AppPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showSpacePicker = false
                            onNavigateToCreateSpace()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("New space")
                    }
                    TextButton(
                        onClick = {
                            showSpacePicker = false
                            onNavigateToManageSpaces()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Manage spaces")
                    }
                }
            }
        }
    }
}
