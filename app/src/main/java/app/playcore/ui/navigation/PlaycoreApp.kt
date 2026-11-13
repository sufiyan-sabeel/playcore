package app.playcore.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.playcore.ui.dashboard.DashboardScreen
import app.playcore.ui.details.GameDetailScreen
import app.playcore.ui.home.HomeScreen
import app.playcore.ui.library.LibraryScreen
import app.playcore.ui.licenses.LicensesScreen
import app.playcore.ui.settings.SettingsScreen
import app.playcore.ui.tools.ToolsScreen

object PlaycoreRoutes {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val DASHBOARD = "dashboard"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"
    const val LICENSES = "licenses"
    const val GAME_DETAIL = "game/{packageName}"

    fun gameDetail(packageName: String): String = "game/$packageName"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(PlaycoreRoutes.HOME, "Home", Icons.Filled.Home),
    TopLevelDestination(PlaycoreRoutes.LIBRARY, "Library", Icons.Filled.List),
    TopLevelDestination(PlaycoreRoutes.DASHBOARD, "Dashboard", Icons.Filled.Info),
    TopLevelDestination(PlaycoreRoutes.TOOLS, "Tools", Icons.Filled.Build),
    TopLevelDestination(PlaycoreRoutes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun PlaycoreApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PlaycoreRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(PlaycoreRoutes.HOME) {
                HomeScreen(
                    onOpenLibrary = { navController.navigateToTopLevel(PlaycoreRoutes.LIBRARY) },
                    onOpenTools = { navController.navigateToTopLevel(PlaycoreRoutes.TOOLS) },
                    onOpenDashboard = { navController.navigateToTopLevel(PlaycoreRoutes.DASHBOARD) },
                    onOpenGame = { packageName ->
                        navController.navigate(PlaycoreRoutes.gameDetail(packageName))
                    },
                )
            }
            composable(PlaycoreRoutes.LIBRARY) {
                LibraryScreen(
                    onOpenGame = { packageName ->
                        navController.navigate(PlaycoreRoutes.gameDetail(packageName))
                    },
                )
            }
            composable(PlaycoreRoutes.DASHBOARD) {
                DashboardScreen()
            }
            composable(PlaycoreRoutes.TOOLS) {
                ToolsScreen(
                    onOpenLicenses = { navController.navigate(PlaycoreRoutes.LICENSES) },
                )
            }
            composable(PlaycoreRoutes.SETTINGS) {
                SettingsScreen(
                    onOpenLicenses = { navController.navigate(PlaycoreRoutes.LICENSES) },
                )
            }
            composable(
                route = PlaycoreRoutes.GAME_DETAIL,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
            ) {
                GameDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(PlaycoreRoutes.LICENSES) {
                LicensesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
