package com.app.zonetask.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.zonetask.R
import com.app.zonetask.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateScaffold(
    title: String,
    showBack: Boolean = true,
    onBackClick: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    topBarColor: Color = AppBackground,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Shared shell for task screens with drawer and top bar.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSidebar(
                onNavigate = { 
                    scope.launch { drawerState.close() }
                    onNavigate(it) 
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = AppOnSurface,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_caret_left),
                                    contentDescription = null,
                                    tint = AppOnSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_list),
                                    contentDescription = "Menu",
                                    tint = AppOnSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarColor,
                        titleContentColor = AppOnSurface,
                        navigationIconContentColor = AppOnSurface
                    )
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }
}

@Composable
fun TaskDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Read-only field with a custom dropdown menu.
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppOnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        anchorWidthPx = coordinates.size.width
                    },
                readOnly = true,
                enabled = false, 
                isError = error != null,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caret_down),
                        contentDescription = null,
                        tint = if (error != null) MaterialTheme.colorScheme.error else AppSecondaryText
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = if (error != null) MaterialTheme.colorScheme.error else AppBorder,
                    disabledLabelColor = if (error != null) MaterialTheme.colorScheme.error else AppSecondaryText,
                    disabledTextColor = AppOnSurface,
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = AppBorder,
                    focusedLabelColor = AppPrimary,
                    unfocusedLabelColor = AppSecondaryText,
                    focusedTextColor = AppOnSurface,
                    unfocusedTextColor = AppOnSurface,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(density) { anchorWidthPx.toDp() })
                    .background(AppSurface)
                    .border(1.dp, AppBorder, RoundedCornerShape(14.dp))
            ) {
                options.forEach { (labelStr, valStr) ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = labelStr, 
                                color = if (value == labelStr) AppPrimary else AppOnSurface,
                                fontWeight = if (value == labelStr) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            onOptionSelected(valStr)
                            expanded = false
                        },
                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                            textColor = AppOnSurface
                        )
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TaskSectionCard(
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Reusable card used to group form sections.
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!title.isNullOrBlank() || !subtitle.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AppOnSurface
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppSecondaryText
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun TaskTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    @DrawableRes leadingIcon: Int? = null,
    singleLine: Boolean = true,
    error: String? = null
) {
    // Standard text field with label and optional inline error.
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppOnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = singleLine,
            isError = error != null,
            placeholder = placeholder?.let { { Text(text = it) } },
            leadingIcon = leadingIcon?.let { { Icon(painter = painterResource(id = it), contentDescription = null) } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimary,
                unfocusedBorderColor = AppBorder,
                focusedTextColor = AppOnSurface,
                unfocusedTextColor = AppOnSurface,
                cursorColor = AppPrimary,
                focusedLeadingIconColor = AppPrimary,
                unfocusedLeadingIconColor = AppSecondaryText,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TaskDropdownField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppOnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_caret_down),
                    contentDescription = null,
                    tint = AppSecondaryText
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimary,
                unfocusedBorderColor = AppBorder,
                focusedTextColor = AppOnSurface,
                unfocusedTextColor = AppOnSurface
            )
        )
    }
}

@Composable
fun TaskRadioRow(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Compact radio row for yes/no style choices.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppPrimary,
                unselectedColor = AppPrimary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppOnSurface
        )
    }
}

@Composable
fun TaskCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Compact checkbox row for selectable object items.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = AppPrimary,
                uncheckedColor = AppPrimary,
                checkmarkColor = AppOnPrimary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppOnSurface
        )
    }
}

@Composable
fun TaskOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = AppOnSurface
        ),
        border = BorderStroke(1.dp, AppOnSurface)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TaskFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppPrimary,
            contentColor = Color.White
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TaskDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFD32F2F).copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.70f)
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TaskActionButtonsRow(
    cancelText: String,
    saveText: String,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // Bottom action bar shared by the form.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TaskOutlinedButton(
            text = cancelText,
            onClick = onCancelClick,
            modifier = Modifier.weight(1f)
        )
        TaskFilledButton(
            text = saveText,
            onClick = onSaveClick,
            modifier = Modifier.weight(1f)
        )
    }
}

data class TaskBottomNavItem(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val route: String,
    val selected: Boolean = false
)

@Composable
fun TaskBottomNavBar(
    selectedIndex: Int = 1,
    onNavigate: (String) -> Unit = {}
) {
    val items = listOf(
        TaskBottomNavItem(R.drawable.ic_house, "Inicio", "home", selectedIndex == 0),
        TaskBottomNavItem(R.drawable.ic_check_square, "Tareas", "task_create", selectedIndex == 1),
        TaskBottomNavItem(R.drawable.ic_chat_circle, "Chat", "chat", selectedIndex == 2),
        TaskBottomNavItem(R.drawable.ic_user, "Perfil", "profile", selectedIndex == 3),
        TaskBottomNavItem(R.drawable.ic_gear_six, "Ajustes", "settings", selectedIndex == 4)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground)
    ) {
        Divider(color = AppBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavCell(
                    item = item,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavCell(
    item: TaskBottomNavItem,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clickable { onNavigate(item.route) },
        contentAlignment = Alignment.Center
    ) {
        if (item.selected) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppPrimary,
                modifier = Modifier
                    .height(38.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.contentDescription,
                        tint = AppBackground
                    )
                }
            }
        } else {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.contentDescription,
                tint = AppOnSurface
            )
        }
    }
}
