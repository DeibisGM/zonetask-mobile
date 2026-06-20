package com.app.zonetask.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.core.UserMessages
import com.app.zonetask.core.WorkspaceStore
import com.app.zonetask.navigation.plans.PlansDestinations
import com.app.zonetask.navigation.plans.PlansNavActions
import com.app.zonetask.navigation.plans.PlansNavKeys
import com.app.zonetask.navigation.plans.plansNavGraph
import com.app.zonetask.navigation.HomeNavKeys
import com.app.zonetask.navigation.spaces.SpacesDestinations
import com.app.zonetask.navigation.spaces.SpacesNavActions
import com.app.zonetask.navigation.spaces.SpacesNavKeys
import com.app.zonetask.navigation.spaces.spacesNavGraph
import com.app.zonetask.messaging.NotificationNavigationStore
import com.app.zonetask.ui.components.NavDestination
import com.app.zonetask.ui.components.ZoneTaskScaffold
import com.app.zonetask.ui.screens.home.HomeScreen
import com.app.zonetask.ui.screens.invitations.MyInvitationsScreen
import com.app.zonetask.ui.screens.login.LoginScreen
import com.app.zonetask.ui.screens.passwordreset.ForgotPasswordScreen
import com.app.zonetask.ui.screens.profile.ProfileEditScreen
import com.app.zonetask.ui.screens.profile.ProfileScreen
import com.app.zonetask.ui.screens.settings.SettingsScreen
import com.app.zonetask.ui.screens.register.RegisterScreen
import com.app.zonetask.ui.screens.chat.ChatEditScreen
import com.app.zonetask.ui.screens.chat.ChatScreen
import com.app.zonetask.ui.screens.chatlist.ChatListScreen
import com.app.zonetask.ui.screens.taskcreate.TaskCreateScreen
import com.app.zonetask.ui.screens.taskdetail.TaskDetailScreen
import com.app.zonetask.ui.screens.taskhistory.SpaceRotationHistoryScreen
import com.app.zonetask.ui.screens.tasks.TasksScreen
import kotlinx.coroutines.flow.collect

private const val AUTH_NOTICE_KEY = "authNotice"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentUserId by rememberSaveable {
        mutableIntStateOf(AuthSessionStore.currentUser?.userId ?: 0)
    }
    var currentUserEmail by rememberSaveable {
        mutableStateOf(AuthSessionStore.currentUser?.email ?: "")
    }
    var currentSpaceId by rememberSaveable {
        mutableIntStateOf(WorkspaceStore.getLastSpaceId(currentUserId))
    }
    var pendingNotificationRoute by remember {
        mutableStateOf(NotificationNavigationStore.consumeLastRoute())
    }
    val startDestination = if (currentUserId > 0) {
        val lastSpaceId = WorkspaceStore.getLastSpaceId(currentUserId)
        AppDestinations.homeRoute(lastSpaceId)
    } else {
        AppDestinations.LOGIN
    }

    // Central logout handler that clears the cached session and returns to the login screen.
    val performLogout = {
        WorkspaceStore.clear(currentUserId)
        AuthSessionStore.clear()
        currentUserId = 0
        currentUserEmail = ""
        currentSpaceId = 0
        navController.navigate(AppDestinations.LOGIN) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val onTabSelected: (NavDestination) -> Unit = { destination ->
        navigateToTab(navController, destination, currentUserId, currentSpaceId)
    }

    LaunchedEffect(Unit) {
        NotificationNavigationStore.events.collect { route ->
            pendingNotificationRoute = route
        }
    }

    LaunchedEffect(currentUserId, pendingNotificationRoute) {
        val route = pendingNotificationRoute
        if (currentUserId > 0 && !route.isNullOrBlank()) {
            navController.navigate(route) {
                launchSingleTop = true
            }
            pendingNotificationRoute = null
        }
    }

    val spacesNavActions = rememberSpacesNavActions(navController, currentUserId)
    val plansNavActions  = rememberPlansNavActions(navController, currentUserId)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {

        composable(route = AppDestinations.LOGIN) { backStackEntry ->
            val registrationNotice by backStackEntry.savedStateHandle
                .getStateFlow<String?>(AUTH_NOTICE_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(registrationNotice) {
                if (!registrationNotice.isNullOrBlank()) {
                    backStackEntry.savedStateHandle[AUTH_NOTICE_KEY] = null
                }
            }

            LoginScreen(
                onLoginSuccess = { userId, email ->
                    currentUserId    = userId
                    currentUserEmail = email
                    val route = pendingNotificationRoute
                    if (!route.isNullOrBlank()) {
                        navController.navigate(route) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                        pendingNotificationRoute = null
                    } else {
                        val lastSpaceId = WorkspaceStore.getLastSpaceId(userId)
                        currentSpaceId = lastSpaceId
                        navController.navigate(AppDestinations.homeRoute(lastSpaceId)) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                    }
                },
                onCreateAccount = {
                    navController.navigate(AppDestinations.REGISTER)
                },
                onForgotPassword = {
                    navController.navigate(AppDestinations.FORGOT_PASSWORD)
                },
                authNotice = registrationNotice
            )
        }

        composable(route = AppDestinations.PROFILE) { backStackEntry ->
            val profileChanged by backStackEntry.savedStateHandle
                .getStateFlow("profileChanged", false)
                .collectAsStateWithLifecycle()

            // Reload the profile after edits so the screen always reflects the latest saved data.
            LaunchedEffect(profileChanged) {
                if (profileChanged) {
                    backStackEntry.savedStateHandle["profileChanged"] = false
                }
            }

            ProfileScreen(
                userId = currentUserId,
                onTabSelected = onTabSelected,
                onEditProfile = {
                    navController.navigate(AppDestinations.PROFILE_EDIT)
                },
                onLogout = performLogout,
                refreshTrigger = profileChanged
            )
        }

        composable(route = AppDestinations.PROFILE_EDIT) {
            ProfileEditScreen(
                userId = currentUserId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("profileChanged", true)
                }
            )
        }

        composable(route = AppDestinations.SETTINGS) {
            SettingsScreen(
                onTabSelected = onTabSelected,
                onOpenSpaces = {
                    navController.navigate(SpacesDestinations.list(currentUserId))
                },
                onOpenProfile = {
                    navController.navigate(AppDestinations.PROFILE)
                },
                onLogout = performLogout
            )
        }

        composable(route = AppDestinations.REGISTER) {
            RegisterScreen(
                onBackToLogin = { message ->
                    // The registration flow stores a one-time message for the login screen before returning.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            AUTH_NOTICE_KEY,
                            message ?: UserMessages.Login.REGISTRATION_NOTICE
                        )
                    navController.popBackStack(AppDestinations.LOGIN, inclusive = false)
                }
            )
        }

        composable(route = AppDestinations.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = {
                    navController.popBackStack(AppDestinations.LOGIN, inclusive = false)
                }
            )
        }

        composable(
            route = AppDestinations.MY_INVITATIONS,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            val invitationsSnackbarHostState = remember { SnackbarHostState() }
            ZoneTaskScaffold(
                title = UserMessages.Invitations.MY_TITLE,
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = invitationsSnackbarHostState
            ) { padding ->
                MyInvitationsScreen(
                    userId            = currentUserId,
                    email             = currentUserEmail,
                    snackbarHostState = invitationsSnackbarHostState,
                    modifier          = Modifier.padding(padding)
                )
            }
        }

        composable(
            route = AppDestinations.HOME,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 0
            val homeRefresh by backStackEntry.savedStateHandle
                .getStateFlow(HomeNavKeys.HOME_REFRESH, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(homeRefresh) {
                if (homeRefresh) {
                    backStackEntry.savedStateHandle[HomeNavKeys.HOME_REFRESH] = false
                }
            }

            ZoneTaskScaffold(
                title = "",
                showBack = false,
                onBackClick = {},
                showTopBar = false,
                currentDestination = NavDestination.HOME,
                onDestinationSelected = onTabSelected,
                snackbarHostState = snackbarHostState
            ) { padding ->
                HomeScreen(
                    spaceId = spaceId,
                    userId = currentUserId,
                    modifier = Modifier.padding(padding),
                    refreshTrigger = homeRefresh,
                    onRefreshHandled = {
                        backStackEntry.savedStateHandle[HomeNavKeys.HOME_REFRESH] = false
                    },
                    onNavigateToCreateSpace = {
                        navController.navigate(SpacesDestinations.CREATE)
                    },
                    onNavigateToCreatePlan = { sid ->
                        navController.navigate(PlansDestinations.list(sid))
                    },
                    onNavigateToCreateTask = {
                        val sid = if (currentSpaceId > 0) currentSpaceId else spaceId
                        if (sid > 0) {
                            navController.navigate(AppDestinations.taskCreateRoute(sid))
                        }
                    },
                    onNavigateToManageSpaces = {
                        navController.navigate(SpacesDestinations.list(currentUserId))
                    },
                    onNavigateToTaskDetail = { sid, taskId ->
                        navController.navigate(AppDestinations.taskDetailRoute(sid, taskId))
                    },
                    onNavigateToChat = { sid ->
                        navController.navigate(AppDestinations.chatRoute(sid))
                    },
                    onSpaceChanged = { newSpaceId ->
                        currentSpaceId = newSpaceId
                        navController.navigate(AppDestinations.homeRoute(newSpaceId)) {
                            popUpTo(AppDestinations.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = AppDestinations.TASK_DETAIL,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.IntType },
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
            val taskId  = backStackEntry.arguments?.getInt("taskId") ?: return@composable
            val taskChanged by backStackEntry.savedStateHandle
                .getStateFlow("taskChanged", false)
                .collectAsStateWithLifecycle()

            TaskDetailScreen(
                spaceId = spaceId,
                taskId = taskId,
                modifier = Modifier.fillMaxSize(),
                refreshTrigger = taskChanged,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle["taskChanged"] = false
                },
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(AppDestinations.taskEditRoute(spaceId, id))
                },
                onOpenRotationHistory = { id ->
                    navController.navigate(AppDestinations.taskRotationHistoryRoute(spaceId, id))
                },
                onDeleted = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("taskChanged", true)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppDestinations.TASK_ROTATION_HISTORY,
            arguments = listOf(
                navArgument("spaceId") { type = NavType.IntType },
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: return@composable
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable

            ZoneTaskScaffold(
                title = "Historial de rotación",
                showBack = true,
                onBackClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState
            ) { padding ->
                SpaceRotationHistoryScreen(
                    spaceId = spaceId,
                    requestingUserId = currentUserId,
                    initialTaskId = taskId,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            route = AppDestinations.CHAT,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId     = backStackEntry.arguments?.getInt("spaceId") ?: 0
            val chatChanged by backStackEntry.savedStateHandle
                .getStateFlow("chatChanged", false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(chatChanged) {
                if (chatChanged) {
                    backStackEntry.savedStateHandle["chatChanged"] = false
                }
            }

            ChatScreen(
                spaceId          = spaceId,
                userId           = currentUserId,
                reloadTrigger    = chatChanged,
                onBack           = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(AppDestinations.editChatRoute(spaceId)) }
            )
        }

        composable(
            route = AppDestinations.CHAT_EDIT,
            arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 0
            ChatEditScreen(
                spaceId = spaceId,
                userId  = currentUserId,
                onBack  = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("chatChanged", true)
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppDestinations.CHAT_LIST) {
            ChatListScreen(
                userId           = currentUserId,
                onNavigateToChat = { spaceId -> navController.navigate(AppDestinations.chatRoute(spaceId)) },
                onTabSelected    = onTabSelected
            )
        }

        spacesNavGraph(
            currentUserId = currentUserId,
            rootSnackbarHostState = snackbarHostState,
            actions = spacesNavActions,
            onTabSelected = onTabSelected
        )

        plansNavGraph(
            navController = navController,
            actions = plansNavActions,
            rootSnackbarHostState = snackbarHostState
        )

        tasksGraph(
            navController = navController,
            currentUserId = currentUserId,
            snackbarHostState = snackbarHostState,
            onTabSelected = onTabSelected,
            onLogout = performLogout
        )
    }
}

private fun navigateToTab(
    navController: NavHostController,
    destination: NavDestination,
    userId: Int,
    currentSpaceId: Int
) {
    if (userId <= 0) return
    val route = when (destination) {
        NavDestination.HOME -> {
            val sid = if (currentSpaceId > 0) currentSpaceId else 0
            AppDestinations.homeRoute(sid)
        }
        NavDestination.TASKS    -> AppDestinations.tasksRoute(userId)
        NavDestination.CHAT     -> AppDestinations.CHAT_LIST
        NavDestination.PROFILE  -> AppDestinations.PROFILE
        NavDestination.SETTINGS -> AppDestinations.SETTINGS
        else -> return
    }
    navController.navigate(route) { launchSingleTop = true }
}

@Composable
private fun rememberSpacesNavActions(
    navController: NavHostController,
    currentUserId: Int
): SpacesNavActions = remember(navController, currentUserId) {
    SpacesNavActions(
        onOpenDetail         = { id -> navController.navigate(SpacesDestinations.detail(id)) },
        onOpenCreate         = { navController.navigate(SpacesDestinations.CREATE) },
        onOpenEdit           = { id -> navController.navigate(SpacesDestinations.edit(id)) },
        onOpenPermissions    = { id -> navController.navigate(SpacesDestinations.permissions(id)) },
        onOpenInvite         = { id -> navController.navigate(SpacesDestinations.invite(id)) },
        onCreateTaskForSpace = { spaceId ->
            navController.navigate(AppDestinations.taskCreateRoute(spaceId))
        },
        onOpenPlans = { spaceId ->
            navController.navigate(PlansDestinations.list(spaceId))
        },
        onOpenCompletedTasks = { spaceId ->
            navController.navigate(SpacesDestinations.completedTasks(spaceId))
        },
        onOpenRotationHistory = { spaceId ->
            navController.navigate(SpacesDestinations.rotationHistory(spaceId))
        },
        onOpenStatisticsMenu = { spaceId, userId ->
            navController.navigate(SpacesDestinations.statisticsMenu(spaceId, userId))
        },
        onOpenStatistics = { spaceId, userId ->
            navController.navigate(SpacesDestinations.statistics(spaceId, userId))
        },
        onOpenSpaceStatistics = { spaceId ->
            navController.navigate(SpacesDestinations.spaceStatistics(spaceId))
        },
        onOpenUserReports = { spaceId ->
            navController.navigate(SpacesDestinations.userReports(spaceId))
        },
        onOpenSpaceReports = {
            navController.navigate(SpacesDestinations.spaceReports(currentUserId))
        },
        onOpenOverdueTrends = { spaceId ->
            navController.navigate(SpacesDestinations.overdueTrends(spaceId))
        },
        onBack = { navController.popBackStack() },
        onOpenInvitations = { navController.navigate(AppDestinations.myInvitationsRoute(currentUserId)) },
        onSpaceCreated = { message ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(SpacesNavKeys.SUCCESS_MESSAGE, message)
            navController.popBackStack()
        },
        onSpaceCreatedAndOpenPlans = { spaceId ->
            navController.navigate(PlansDestinations.list(spaceId)) {
                popUpTo(SpacesDestinations.CREATE) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        },
        onSpaceEdited = { message ->
            navController.getBackStackEntry(SpacesDestinations.LIST)
                .savedStateHandle[SpacesNavKeys.SUCCESS_MESSAGE] = message
            navController.getBackStackEntry(SpacesDestinations.DETAIL)
                .savedStateHandle[SpacesNavKeys.REFRESH_DETAIL] = true
            navController.popBackStack()
        },
        onSpaceDeleted = { message ->
            navController.getBackStackEntry(SpacesDestinations.LIST)
                .savedStateHandle[SpacesNavKeys.SUCCESS_MESSAGE] = message
            navController.popBackStack(SpacesDestinations.LIST, inclusive = false)
        }
    )
}

@Composable
private fun rememberPlansNavActions(
    navController: NavHostController,
    currentUserId: Int
): PlansNavActions = remember(navController, currentUserId) {
    PlansNavActions(
        onOpenList = { spaceId -> navController.navigate(PlansDestinations.list(spaceId)) },
        onCreatePlan = { spaceId ->
            WorkspaceStore.rememberSpace(currentUserId, spaceId)
            navController.navigate(PlansDestinations.templateSelect(spaceId))
        },
        onApplyTemplate = { spaceId, templateId ->
            WorkspaceStore.rememberSpace(currentUserId, spaceId)
            navController.navigate(PlansDestinations.newPlan(spaceId, templateId))
        },
        onOpenPlan = { spaceId, planId ->
            WorkspaceStore.rememberPlan(currentUserId, spaceId, planId)
            navController.navigate(PlansDestinations.editor(spaceId, planId))
        },
        onPlanSaved = { message ->
            // Return straight to the plan list, popping the template-selection screen too,
            // and signal the list to reload.
            runCatching {
                val listEntry = navController.getBackStackEntry(PlansDestinations.LIST)
                listEntry.savedStateHandle[PlansNavKeys.PLAN_SAVED_MESSAGE] = message
                listEntry.savedStateHandle[PlansNavKeys.RELOAD_PLANS] = true
            }
            navController.popBackStack(PlansDestinations.LIST, inclusive = false)
        },
        onBack = { navController.popBackStack() }
    )
}

private fun androidx.navigation.NavGraphBuilder.tasksGraph(
    navController: NavHostController,
    currentUserId: Int,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (NavDestination) -> Unit,
    onLogout: () -> Unit
) {
    fun markTaskRefresh(backStackEntry: androidx.navigation.NavBackStackEntry?) {
        backStackEntry?.savedStateHandle?.set("taskChanged", true)
        runCatching {
            navController.getBackStackEntry(AppDestinations.tasksRoute(currentUserId))
                .savedStateHandle["taskChanged"] = true
        }
    }

    composable(route = AppDestinations.TASK_CREATE) {
        TaskCreateScreen(
            initialSpaceId   = 1,
            initialCreatedBy = currentUserId,
            onNavigate       = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onLogout         = onLogout,
            onSaved          = { markTaskRefresh(navController.previousBackStackEntry) },
            onClose          = { navController.popBackStack() }
        )
    }

    composable(
        route = AppDestinations.TASK_CREATE_WITH_SPACE,
        arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
        TaskCreateScreen(
            initialSpaceId   = spaceId,
            initialCreatedBy = currentUserId,
            onNavigate       = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onLogout         = onLogout,
            onSaved          = { markTaskRefresh(navController.previousBackStackEntry) },
            onClose          = { navController.popBackStack() }
        )
    }

    composable(
        route = AppDestinations.TASK_EDIT_WITH_SPACE,
        arguments = listOf(
            navArgument("spaceId") { type = NavType.IntType },
            navArgument("taskId")  { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments?.getInt("spaceId") ?: 1
        val taskId  = backStackEntry.arguments?.getInt("taskId") ?: return@composable
        TaskCreateScreen(
            initialSpaceId   = spaceId,
            initialCreatedBy = currentUserId,
            taskId           = taskId,
            onNavigate       = { route -> navigateToSpacesFromTasks(navController, route, currentUserId) },
            onLogout         = onLogout,
            onSaved          = { markTaskRefresh(navController.previousBackStackEntry) },
            onClose          = { navController.popBackStack() }
        )
    }

    composable(
        route = AppDestinations.TASKS,
        arguments = listOf(navArgument("userId") { type = NavType.IntType })
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getInt("userId") ?: currentUserId
        val taskChanged by backStackEntry.savedStateHandle
            .getStateFlow("taskChanged", false)
            .collectAsStateWithLifecycle()

        ZoneTaskScaffold(
            title                 = UserMessages.Screens.TASKS_TITLE,
            showBack              = false,
            onBackClick           = {},
            showTopBar            = false,
            currentDestination    = NavDestination.TASKS,
            onDestinationSelected = onTabSelected,
            snackbarHostState     = snackbarHostState
        ) { padding ->
            TasksScreen(
                userId        = userId,
                modifier      = Modifier.padding(padding),
                reloadTrigger = taskChanged,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle["taskChanged"] = false
                },
                onCreateTask = { spaceId ->
                    navController.navigate(AppDestinations.taskCreateRoute(spaceId))
                },
                onEditTask = { spaceId, taskId ->
                    navController.navigate(AppDestinations.taskEditRoute(spaceId, taskId))
                },
                onTaskClick = { spaceId, taskId ->
                    navController.navigate(AppDestinations.taskDetailRoute(spaceId, taskId))
                }
            )
        }
    }
}

private fun navigateToSpacesFromTasks(
    navController: NavHostController,
    route: String,
    currentUserId: Int
) {
    // Routes emitted from task screens are normalized here so profile and spaces open consistently.
    if ((route == "spaces" || route.startsWith("spaces/")) && currentUserId > 0) {
        navController.navigate(SpacesDestinations.list(currentUserId)) {
            launchSingleTop = true
        }
        return
    }

    if (route == "profile" && currentUserId > 0) {
        navController.navigate(AppDestinations.PROFILE) {
            launchSingleTop = true
        }
        return
    }
}
