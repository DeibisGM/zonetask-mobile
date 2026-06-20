package com.app.zonetask.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.R
import com.app.zonetask.ui.components.FloorPlanCanvas
import com.app.zonetask.ui.components.ScreenStateCard
import com.app.zonetask.ui.components.ScreenLoadingState
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    spaceId: Int,
    userId: Int,
    modifier: Modifier = Modifier,
    onNavigateToCreateSpace: () -> Unit = {},
    onNavigateToCreatePlan: (spaceId: Int) -> Unit = {},
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToManageSpaces: () -> Unit = {},
    onNavigateToTaskDetail: (spaceId: Int, taskId: Int) -> Unit = { _, _ -> },
    onNavigateToChat: (spaceId: Int) -> Unit = {},
    onSpaceChanged: (newSpaceId: Int) -> Unit = {},
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(spaceId = spaceId, userId = userId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSpacePicker by rememberSaveable { mutableStateOf(false) }
    var tasksExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.currentSpaceId) {
        val resolved = uiState.currentSpaceId
        if (spaceId == 0 && resolved != null && resolved > 0) {
            onSpaceChanged(resolved)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(
                title = uiState.spaceName.ifBlank { "ZoneTask" },
                onTitleClick = { showSpacePicker = true },
                onManageSpaces = { onNavigateToManageSpaces() },
                onOpenChat = {
                    val sid = uiState.currentSpaceId ?: spaceId
                    if (sid > 0) onNavigateToChat(sid)
                },
                onCreateTask = {
                    val sid = uiState.currentSpaceId ?: spaceId
                    if (sid > 0) onNavigateToCreateTask()
                }
            )

            when {
                uiState.isLoading && uiState.userSpaces.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        ScreenLoadingState(modifier = Modifier.fillMaxWidth(), lines = 3)
                    }
                }

                uiState.errorMessage != null && uiState.userSpaces.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ScreenStateCard(
                            title = "No spaces yet",
                            message = uiState.errorMessage ?: "Create your first space to start organizing floors and tasks.",
                            actionText = "Create space",
                            onAction = onNavigateToCreateSpace
                        )
                    }
                }

                else -> {
                    val currentPlanIndex = uiState.plans.indexOfFirst { it.planId == uiState.activePlan?.planId }
                    val previousPlan = if (currentPlanIndex > 0) uiState.plans[currentPlanIndex - 1] else null
                    val nextPlan = if (currentPlanIndex >= 0 && currentPlanIndex < uiState.plans.lastIndex) uiState.plans[currentPlanIndex + 1] else null

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HomeFloorSwitcher(
                            activePlan = uiState.activePlan?.name,
                            hasPlans = uiState.plans.isNotEmpty(),
                            canGoPrevious = previousPlan != null,
                            canGoNext = nextPlan != null,
                            onPreviousPlan = {
                                previousPlan?.let { viewModel.selectPlan(it.planId) }
                            },
                            onNextPlan = {
                                nextPlan?.let { viewModel.selectPlan(it.planId) }
                            }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier
                                .fillMaxSize()
                                .shadow(12.dp, RoundedCornerShape(28.dp)),
                            color = AppCardElevated,
                            shape = RoundedCornerShape(28.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when {
                                        uiState.activePlan != null -> {
                                            FloorPlanCanvas(
                                                worldWidth = uiState.activePlan!!.canvasWidth,
                                                worldHeight = uiState.activePlan!!.canvasHeight,
                                                bottomInset = if (tasksExpanded) 300.dp else 96.dp,
                                                zones = uiState.planZones,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            if (uiState.isZonesLoading || uiState.zonesErrorMessage != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(16.dp)
                                                ) {
                                                    when {
                                                        uiState.isZonesLoading -> ScreenStateCard(
                                                            title = "Loading floor",
                                                            message = "Refreshing zones and context."
                                                        )
                                                        uiState.zonesErrorMessage != null -> ScreenStateCard(
                                                            title = "Floor loading issue",
                                                            message = uiState.zonesErrorMessage!!,
                                                            actionText = "Retry",
                                                            onAction = viewModel::loadHomeData
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        uiState.plansErrorMessage != null -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ScreenStateCard(
                                                    title = "Could not load floors",
                                                    message = uiState.plansErrorMessage!!,
                                                    actionText = "Retry",
                                                    onAction = viewModel::loadHomeData
                                                )
                                            }
                                        }

                                        else -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ScreenStateCard(
                                                    title = "No floor yet",
                                                    message = "Create a floor in this space so zones and tasks have a place to live.",
                                                    actionText = "Open floors",
                                                    onAction = {
                                                        val sid = uiState.currentSpaceId ?: spaceId
                                                        if (sid > 0) onNavigateToCreatePlan(sid)
                                                    }
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
        }

        if (!uiState.isLoading && uiState.currentSpaceId != null) {
                        TasksPanel(
                            expanded = tasksExpanded,
                            pendingTasks = uiState.pendingTasks,
                            tasksErrorMessage = uiState.tasksErrorMessage,
                            onToggle = { tasksExpanded = !tasksExpanded },
                onOpenTask = { task -> onNavigateToTaskDetail(uiState.currentSpaceId!!, task.taskId) },
                onCreateTask = onNavigateToCreateTask,
                onRetry = viewModel::loadHomeData,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showSpacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSpacePicker = false },
            containerColor = AppBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            SpacePickerSheet(
                spaces = uiState.userSpaces,
                currentSpaceId = uiState.currentSpaceId,
                onManageSpaces = {
                    showSpacePicker = false
                    onNavigateToManageSpaces()
                },
                onSpaceSelected = { selectedSpaceId ->
                    onSpaceChanged(selectedSpaceId)
                    showSpacePicker = false
                },
                onCreateSpace = {
                    showSpacePicker = false
                    onNavigateToCreateSpace()
                }
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    title: String,
    onTitleClick: () -> Unit,
    onManageSpaces: () -> Unit,
    onOpenChat: () -> Unit,
    onCreateTask: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppCardElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTitleClick)
            ) {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onOpenChat,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chat_circle),
                    contentDescription = "Chat",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onManageSpaces,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Manage spaces",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onCreateTask,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "New task",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeFloorSwitcher(
    activePlan: String?,
    hasPlans: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousPlan: () -> Unit,
    onNextPlan: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousPlan,
            enabled = hasPlans && canGoPrevious
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "Previous floor",
                tint = if (hasPlans && canGoPrevious) AppSecondaryText else AppSecondaryText.copy(alpha = 0.3f)
            )
        }

        Text(
            text = activePlan ?: "No floor yet",
            modifier = Modifier.weight(1f),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IconButton(
            onClick = onNextPlan,
            enabled = hasPlans && canGoNext
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Next floor",
                tint = if (hasPlans && canGoNext) AppSecondaryText else AppSecondaryText.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun TasksPanel(
    expanded: Boolean,
    pendingTasks: List<HomeTaskItem>,
    tasksErrorMessage: String?,
    onToggle: () -> Unit,
    onOpenTask: (HomeTaskItem) -> Unit,
    onCreateTask: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
                .clickable(onClick = onToggle),
            color = AppCardElevated,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Today's tasks",
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${pendingTasks.size} pending",
                        color = AppSecondaryText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowUp,
                    contentDescription = null,
                    tint = AppSecondaryText
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppCardElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!tasksErrorMessage.isNullOrBlank()) {
                        ScreenStateCard(
                            title = "Tasks could not load",
                            message = tasksErrorMessage,
                            actionText = "Retry",
                            onAction = onRetry
                        )
                    } else if (pendingTasks.isEmpty()) {
                        ScreenStateCard(
                            title = "Nothing pending",
                            message = "There are no active tasks right now. Create one when you need it.",
                            actionText = "Create task",
                            onAction = onCreateTask
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pendingTasks, key = { it.taskId }) { task ->
                                HomeTaskRow(
                                    task = task,
                                    onClick = { onOpenTask(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTaskRow(
    task: HomeTaskItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = AppSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = when (task.dueStatusKey) {
                    "overdue" -> Color(0xFFE57373)
                    "upcoming" -> AppPrimary
                    else -> AppSecondaryText
                },
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = listOfNotNull(task.zoneName.takeIf { it.isNotBlank() }, task.assigneeName?.takeIf { it.isNotBlank() })
                        .joinToString(" · ")
                        .ifBlank { "No extra details" },
                    color = AppSecondaryText,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (task.scheduledTime != null) {
                Text(
                    text = task.scheduledTime.take(5),
                    color = AppSecondaryText,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SpacePickerSheet(
    spaces: List<com.app.zonetask.domain.model.Space>,
    currentSpaceId: Int?,
    onManageSpaces: () -> Unit,
    onSpaceSelected: (Int) -> Unit,
    onCreateSpace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Switch spaces",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Open a different group without losing your floor context.",
            color = AppSecondaryText,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = onManageSpaces) {
            Text(
                text = "Manage spaces",
                color = AppPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (spaces.isEmpty()) {
            ScreenStateCard(
                title = "No spaces yet",
                message = "Create a space to start organizing tasks.",
                actionText = "Create space",
                onAction = onCreateSpace
            )
        } else {
            spaces.forEach { space ->
                val isActive = space.spaceId == currentSpaceId
                Surface(
                    onClick = { onSpaceSelected(space.spaceId) },
                    color = if (isActive) AppPrimary.copy(alpha = 0.10f) else AppSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isActive) AppPrimary.copy(alpha = 0.35f) else AppBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isActive) AppPrimary.copy(alpha = 0.18f) else AppBackground,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = space.name.take(1).uppercase(),
                                color = if (isActive) AppPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = space.name,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = space.spaceType,
                                color = AppSecondaryText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }

                        if (isActive) {
                            Text(
                                text = "Active",
                                color = AppPrimary,
                                fontWeight = FontWeight.SemiBold,
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onCreateSpace,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
        ) {
            Text("New space", color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}
