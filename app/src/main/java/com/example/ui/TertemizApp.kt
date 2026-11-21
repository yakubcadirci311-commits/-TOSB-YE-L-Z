package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.BadgesProfileScreen
import com.example.ui.screens.GuideTeamScreen
import com.example.ui.screens.HandwashEcoScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportScreen
import kotlinx.coroutines.flow.collectLatest

object TertemizRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_REPORT = "report"
    const val ROUTE_TIMER = "timer"
    const val ROUTE_BADGES = "badges"
    const val ROUTE_GUIDE = "guide"
}

enum class MainNavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        route = TertemizRoutes.ROUTE_HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    REPORT(
        route = TertemizRoutes.ROUTE_REPORT,
        labelRes = R.string.nav_reports,
        selectedIcon = Icons.Rounded.NotificationsActive,
        unselectedIcon = Icons.Outlined.NotificationsActive,
        testTag = "nav_tab_report"
    ),
    TIMER(
        route = TertemizRoutes.ROUTE_TIMER,
        labelRes = R.string.nav_timer,
        selectedIcon = Icons.Rounded.Timer,
        unselectedIcon = Icons.Outlined.Timer,
        testTag = "nav_tab_timer"
    ),
    BADGES(
        route = TertemizRoutes.ROUTE_BADGES,
        labelRes = R.string.nav_badges,
        selectedIcon = Icons.Rounded.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        testTag = "nav_tab_badges"
    ),
    GUIDE(
        route = TertemizRoutes.ROUTE_GUIDE,
        labelRes = R.string.nav_guide,
        selectedIcon = Icons.Rounded.Info,
        unselectedIcon = Icons.Outlined.Info,
        testTag = "nav_tab_guide"
    )
}

@Composable
fun TertemizApp(
    viewModel: TertemizViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TertemizRoutes.ROUTE_HOME

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessages.collectLatest { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Geniş ekranlarda alt gezinme çubuğu yerine yan NavigationRail kullanılır
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        MainNavTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        MainNavTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = TertemizRoutes.ROUTE_HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(TertemizRoutes.ROUTE_HOME) {
                        HomeScreen(
                            uiState = uiState,
                            onQuickReportClick = { navigateToTab(TertemizRoutes.ROUTE_REPORT) },
                            onStartHandwashClick = {
                                navigateToTab(TertemizRoutes.ROUTE_TIMER)
                                viewModel.startHandwashTimer(restartIfCompleted = true)
                            },
                            onNextTipClick = viewModel::nextDailyTip,
                            onAreaChipClick = { navigateToTab(TertemizRoutes.ROUTE_REPORT) },
                            onOpenBadgesClick = { navigateToTab(TertemizRoutes.ROUTE_BADGES) }
                        )
                    }
                    composable(TertemizRoutes.ROUTE_REPORT) {
                        ReportScreen(
                            uiState = uiState,
                            onSelectArea = viewModel::selectArea,
                            onSelectCondition = viewModel::selectCondition,
                            onNoteChange = viewModel::updateNoteInput,
                            onSubmitReport = viewModel::submitReport,
                            onFilterChange = viewModel::setReportFilter,
                            onToggleResolved = viewModel::toggleReportResolved,
                            onDeleteReport = viewModel::deleteReport
                        )
                    }
                    composable(TertemizRoutes.ROUTE_TIMER) {
                        HandwashEcoScreen(
                            uiState = uiState,
                            onStartTimer = { viewModel.startHandwashTimer() },
                            onPauseTimer = viewModel::pauseHandwashTimer,
                            onResetTimer = viewModel::resetHandwashTimer,
                            onLogWaterSaving = viewModel::logWaterSaving,
                            onLogZeroWaste = viewModel::logZeroWaste,
                            onToggleChecklistItem = viewModel::toggleChecklistItem
                        )
                    }
                    composable(TertemizRoutes.ROUTE_BADGES) {
                        BadgesProfileScreen(
                            uiState = uiState,
                            onBadgeFilterChange = viewModel::setBadgeFilter
                        )
                    }
                    composable(TertemizRoutes.ROUTE_GUIDE) {
                        GuideTeamScreen(
                            onResetAllData = viewModel::resetAllData,
                            onRestoreSampleData = viewModel::restoreSampleData
                        )
                    }
                }
            }
        }
    }
}
