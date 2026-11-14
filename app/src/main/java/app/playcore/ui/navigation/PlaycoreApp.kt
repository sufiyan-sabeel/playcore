package app.playcore.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.Space
import app.playcore.ui.components.AngularTabs
import app.playcore.ui.components.IconTile
import app.playcore.ui.dashboard.DashboardScreen
import app.playcore.ui.details.GameDetailScreen
import app.playcore.ui.explore.ExploreHomeScreen
import app.playcore.ui.gamepad.GamepadScreen
import app.playcore.ui.handheld.HandheldScreen
import app.playcore.ui.help.HelpScreen
import app.playcore.ui.library.LibraryScreen
import app.playcore.ui.licenses.LicensesScreen
import app.playcore.ui.overlay.EnergyCubeScreen
import app.playcore.ui.settings.SettingsScreen
import app.playcore.ui.superbase.SuperBaseScreen
import app.playcore.ui.superbase.TileScreen

object Routes {
    const val EXPLORE_HOME = "explore/home"
    const val EXPLORE_LIBRARY = "explore/library"
    const val EXPLORE_MONITOR = "explore/monitor"
    const val SUPER_BASE = "superbase"
    const val HANDHELD = "handheld"
    const val GAMEPAD = "gamepad"
    const val ENERGY_CUBE = "overlay"
    const val HELP = "help"
    const val SETTINGS = "settings"
    const val LICENSES = "licenses"
    const val GAME_DETAIL = "game/{packageName}"
    const val TILE = "superbase/{tile}"

    fun gameDetail(packageName: String) = "game/$packageName"
    fun tile(id: String) = "superbase/$id"
}

private val modeRoutes = listOf(Routes.EXPLORE_HOME, Routes.HANDHELD, Routes.GAMEPAD)
private val modeLabels = listOf("Explore", "Handheld", "Gamepad")

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val exploreBottom = listOf(
    BottomDestination(Routes.EXPLORE_HOME, "Home", Icons.Filled.Home),
    BottomDestination(Routes.EXPLORE_LIBRARY, "Library", Icons.AutoMirrored.Filled.List),
    BottomDestination(Routes.EXPLORE_MONITOR, "Monitor", Icons.Filled.Info),
    BottomDestination(Routes.SUPER_BASE, "Super Base", Icons.Filled.Star),
)

@Composable
fun PlaycoreApp() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    NavHost(navController = nav, startDestination = Routes.EXPLORE_HOME) {
        composable(Routes.EXPLORE_HOME) {
            ExploreHomeScreen(
                onOpenLibrary = { nav.navigateTop(Routes.EXPLORE_LIBRARY) },
                onOpenMonitor = { nav.navigateTop(Routes.EXPLORE_MONITOR) },
                onOpenSuperBase = { nav.navigateTop(Routes.SUPER_BASE) },
                onOpenGame = { nav.navigate(Routes.gameDetail(it)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenHelp = { nav.navigate(Routes.HELP) },
                modeBar = { ModeTabs(route, nav) },
                bottomBar = { ExploreBottomBar(route, nav) },
            )
        }
        composable(Routes.EXPLORE_LIBRARY) {
            LibraryScreen(
                onOpenGame = { nav.navigate(Routes.gameDetail(it)) },
                modeBar = { ModeTabs(route, nav) },
                bottomBar = { ExploreBottomBar(route, nav) },
            )
        }
        composable(Routes.EXPLORE_MONITOR) {
            DashboardScreen(
                modeBar = { ModeTabs(route, nav) },
                bottomBar = { ExploreBottomBar(route, nav) },
            )
        }
        composable(Routes.SUPER_BASE) {
            SuperBaseScreen(
                onOpenTile = { nav.navigate(Routes.tile(it)) },
                onOpenOverlay = { nav.navigate(Routes.ENERGY_CUBE) },
                onOpenHelp = { nav.navigate(Routes.HELP) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                modeBar = { ModeTabs(route, nav) },
                bottomBar = { ExploreBottomBar(route, nav) },
            )
        }
        composable(Routes.HANDHELD) {
            HandheldScreen(
                onOpenGame = { nav.navigate(Routes.gameDetail(it)) },
                onOpenSuperBase = { nav.navigateTop(Routes.SUPER_BASE) },
                onOpenLibrary = { nav.navigateTop(Routes.EXPLORE_LIBRARY) },
                modeBar = { ModeTabs(route, nav) },
            )
        }
        composable(Routes.GAMEPAD) {
            GamepadScreen(
                onOpenGame = { nav.navigate(Routes.gameDetail(it)) },
                modeBar = { ModeTabs(route, nav) },
            )
        }
        composable(Routes.ENERGY_CUBE) {
            EnergyCubeScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.HELP) {
            HelpScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() }, onOpenLicenses = { nav.navigate(Routes.LICENSES) })
        }
        composable(Routes.LICENSES) {
            LicensesScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.GAME_DETAIL,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
        ) {
            GameDetailScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.TILE,
            arguments = listOf(navArgument("tile") { type = NavType.StringType }),
        ) { backStackEntry ->
            TileScreen(
                tileId = backStackEntry.arguments?.getString("tile").orEmpty(),
                onBack = { nav.popBackStack() },
                onOpenGame = { nav.navigate(Routes.gameDetail(it)) },
            )
        }
    }
}

@Composable
private fun ModeTabs(currentRoute: String?, nav: NavHostController) {
    val index = modeRoutes.indexOf(currentRoute).coerceAtLeast(0)
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = Space.l, vertical = Space.s)) {
        AngularTabs(
            options = modeLabels,
            selectedIndex = index,
            onSelect = { selected -> nav.navigateTop(modeRoutes[selected]) },
        )
    }
}

@Composable
private fun ExploreBottomBar(currentRoute: String?, nav: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Console.Background)
            .padding(horizontal = Space.l, vertical = Space.m),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        exploreBottom.forEach { destination ->
            IconTile(
                icon = destination.icon,
                caption = destination.label,
                selected = currentRoute == destination.route,
                modifier = Modifier.testTag("nav_${destination.route}"),
                onClick = { if (currentRoute != destination.route) nav.navigateTop(destination.route) },
            )
        }
    }
}

/** Settings lives in the header gear, as in the reference chrome. */
@Composable
fun HeaderGear(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Console.TextDim)
    }
}

@Composable
fun HeaderBuild(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Build, contentDescription = "Tools", tint = Console.TextDim)
    }
}

private fun NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(Routes.EXPLORE_HOME) { saveState = false }
        launchSingleTop = true
    }
}

@Composable
fun ModeLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = Console.TextFaint)
}
