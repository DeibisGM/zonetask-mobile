package com.app.zonetask.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.zonetask.R
import com.app.zonetask.ui.components.FloorPlanCanvas
import com.app.zonetask.ui.theme.AppBackground
import com.app.zonetask.ui.theme.AppBorder
import com.app.zonetask.ui.theme.AppCardElevated
import com.app.zonetask.ui.theme.AppPrimary
import com.app.zonetask.ui.theme.AppSecondaryText
import com.app.zonetask.ui.theme.AppSurface

private val HomeSheetTitleStyle = TextStyle(
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.SemiBold
)

private val HomeSheetSubtitleStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal
)

private val HomeEmptyTitleStyle = TextStyle(
    fontSize = 24.sp,
    lineHeight = 30.sp,
    fontWeight = FontWeight.SemiBold
)

private val HomeEmptyBodyStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal
)

private val HomeItemTitleStyle = TextStyle(
    fontSize = 16.sp,
    lineHeight = 22.sp,
    fontWeight = FontWeight.SemiBold
)

private val HomeItemMetaStyle = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Normal
)

private val HomeActionTextStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold
)

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
    onNavigateToMembers: (spaceId: Int) -> Unit = {},
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppCardElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    }
                    IconButton(onClick = {
                        val sid = uiState.currentSpaceId ?: spaceId
                        onNavigateToMembers(sid)
                    }) {
                        Icon(
                            imageVector        = Icons.Outlined.Group,
                            contentDescription = "Members",
                            tint               = MaterialTheme.colorScheme.onSurface,
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = {
                        val sid = uiState.currentSpaceId ?: spaceId
                        onNavigateToChat(sid)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Switch spaces",
                            tint = AppSecondaryText,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(22.dp)
                        )
                    }
                    if (uiState.currentSpaceId != null && uiState.currentSpaceId!! > 0) {
                        IconButton(onClick = onNavigateToManageSpaces) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Spaces",
                                modifier = Modifier.size(22.dp),
                                colorFilter = ColorFilter.tint(AppPrimary)
                            )
                        }
                        IconButton(onClick = {
                            val sid = uiState.currentSpaceId ?: spaceId
                            onNavigateToChat(sid)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Chat,
                                contentDescription = "Chat",
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
                        EmptySpacesState(
                            message = uiState.errorMessage!!,
                            onCreateSpace = onNavigateToCreateSpace
                        )
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
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = AppCardElevated,
                                    shape = RoundedCornerShape(28.dp),
                                    border = BorderStroke(1.dp, AppBorder),
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .background(
                                                    color = AppPrimary.copy(alpha = 0.10f),
                                                    shape = RoundedCornerShape(18.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(26.dp),
                                                colorFilter = ColorFilter.tint(AppPrimary)
                                            )
                                        }

                                        Text(
                                            text = "No floor plan yet",
                                            style = HomeEmptyTitleStyle.copy(fontSize = 22.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = "Create a floor from this space and start placing rooms.",
                                            style = HomeEmptyBodyStyle,
                                            color = AppSecondaryText,
                                            textAlign = TextAlign.Center
                                        )

                                        Button(
                                            onClick = {
                                                val sid = uiState.currentSpaceId ?: spaceId
                                                if (sid > 0) {
                                                    onNavigateToCreatePlan(sid)
                                                }
                                            },
                                            enabled = (uiState.currentSpaceId ?: spaceId) > 0,
                                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                        ) {
                                            Text(
                                                text = "Create floor",
                                                color = Color.Black,
                                                style = HomeActionTextStyle
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

    if (showSpacePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSpacePicker = false },
            containerColor = Color(0xFF121212),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Switch spaces",
                    style = HomeSheetTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
                )

                Text(
                    text = "Choose a space to open it quickly.",
                    style = HomeSheetSubtitleStyle,
                    color = AppSecondaryText,
                    modifier = Modifier.padding(bottom = 22.dp)
                )

                if (uiState.userSpaces.isEmpty()) {
                    Surface(
                        color = AppBackground,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, AppBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                colorFilter = ColorFilter.tint(AppPrimary)
                            )
                            Text(
                                text = "No spaces yet",
                                style = HomeSheetTitleStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create one to get started.",
                                style = HomeEmptyBodyStyle,
                                color = AppSecondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    uiState.userSpaces.forEach { space ->
                        val isActive = space.spaceId == uiState.currentSpaceId
                        Surface(
                            onClick = {
                                onSpaceChanged(space.spaceId)
                                showSpacePicker = false
                            },
                            color = if (isActive) AppPrimary.copy(alpha = 0.10f) else AppBackground,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isActive) AppPrimary.copy(alpha = 0.35f) else AppBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = space.name,
                                        style = HomeItemTitleStyle,
                                        color = if (isActive) AppPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = space.spaceType,
                                        style = HomeItemMetaStyle,
                                        color = AppSecondaryText
                                    )
                                }
                                if (isActive) {
                                    Surface(
                                        color = AppPrimary.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            color = AppPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            style = HomeItemMetaStyle,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showSpacePicker = false
                        onNavigateToCreateSpace()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                ) {
                    Text("New space", color = Color.Black, style = HomeActionTextStyle)
                }
            }
        }
    }
}

@Composable
private fun EmptySpacesState(
    message: String,
    onCreateSpace: () -> Unit
) {
    Surface(
        color = AppCardElevated,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier
            .padding(start = 20.dp, top = 12.dp, end = 20.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = AppPrimary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppPrimary)
                )
            }

            Text(
                text = "No spaces yet",
                style = HomeEmptyTitleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (message.contains("spaces yet", ignoreCase = true)) {
                    "Create one to get started."
                } else {
                    message
                },
                style = HomeEmptyBodyStyle,
                color = AppSecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCreateSpace,
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(
                    text = "Create your first space",
                    color = Color.Black,
                    style = HomeActionTextStyle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
