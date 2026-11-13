package app.playcore.ui.library

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.playcore.core.designsystem.Chamfer
import app.playcore.core.designsystem.Console
import app.playcore.core.designsystem.PanelShape
import app.playcore.core.designsystem.Space
import app.playcore.core.designsystem.TileShape
import app.playcore.core.model.Game
import app.playcore.core.model.InstalledApp
import app.playcore.core.model.LaunchResult
import app.playcore.core.model.LibrarySort
import app.playcore.ui.components.AngularButton
import app.playcore.ui.components.AngularTabs
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.ChamferPanel
import app.playcore.ui.components.ConsoleBackground
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.ErrorState
import app.playcore.ui.components.LoadingState
import app.playcore.ui.components.ReferenceHeader
import app.playcore.ui.system.SystemViewModel

@Composable
fun LibraryScreen(
    onOpenGame: (String) -> Unit,
    modeBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: LibraryViewModel = viewModel(),
    systemViewModel: SystemViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val system by systemViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showBrowser by remember { mutableStateOf(false) }

    ConsoleBackground(system.settings.backdrop, system.settings.vignette) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReferenceHeader(
                title = "Library",
                subtitle = "PLAYCORE / ${state.totalInLibrary} ON THE CONSOLE",
                trailing = {
                    IconButton(onClick = {
                        viewModel.loadInstalledApps()
                        showBrowser = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add apps", tint = Console.Text)
                    }
                },
            )
            modeBar()

            Column(modifier = Modifier.padding(horizontal = Space.l)) {
                OutlinedTextField(
                    value = state.query.search,
                    onValueChange = viewModel::setSearch,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Console.TextDim) },
                    trailingIcon = {
                        if (state.query.search.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = Console.TextDim)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Console.Violet,
                        unfocusedBorderColor = Console.VioletDeep,
                        focusedTextColor = Console.Text,
                        unfocusedTextColor = Console.Text,
                        cursorColor = Console.Violet,
                    ),
                )
                Spacer(Modifier.height(Space.s))
                val sorts = LibrarySort.entries
                AngularTabs(
                    options = listOf("Recent", "Name", "Favourites", if (state.query.favoritesOnly) "Only fav ✓" else "Only fav"),
                    selectedIndex = sorts.indexOf(state.query.sort).coerceAtLeast(0),
                    onSelect = { index ->
                        if (index < sorts.size) viewModel.setSort(sorts[index]) else viewModel.toggleFavoritesOnly()
                    },
                )
                Spacer(Modifier.height(Space.m))
            }

            when {
                state.isLoading && state.games.isEmpty() -> LoadingState("Reading installed apps")
                state.error != null -> ErrorState(
                    title = "Discovery failed",
                    message = state.error ?: "Installed apps could not be read.",
                    onRetry = viewModel::refreshInstalled,
                )
                state.games.isEmpty() -> EmptyState(
                    title = if (state.totalInLibrary == 0) "Your lobby is empty" else "No matches",
                    message = if (state.totalInLibrary == 0) {
                        "PLAYCORE never guesses which apps are games. Open the browser and add the ones you play."
                    } else {
                        "No library entry matches this search or filter."
                    },
                    actionLabel = if (state.totalInLibrary == 0) "Browse installed apps" else "Clear filters",
                    onAction = {
                        if (state.totalInLibrary == 0) {
                            viewModel.loadInstalledApps()
                            showBrowser = true
                        } else {
                            viewModel.setSearch("")
                            viewModel.setCategoryFilter(null)
                        }
                    },
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = Space.l, end = Space.l, bottom = Space.l),
                    horizontalArrangement = Arrangement.spacedBy(Space.m),
                    verticalArrangement = Arrangement.spacedBy(Space.m),
                ) {
                    gridItems(state.games, key = { it.packageName }) { game ->
                        GameTile(
                            game = game,
                            onOpen = { onOpenGame(game.packageName) },
                            onLaunch = {
                                viewModel.launch(game.packageName) { result ->
                                    if (result != LaunchResult.Launched) {
                                        Toast.makeText(context, "That app could not be launched.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(game.packageName) },
                        )
                    }
                }
            }
            bottomBar()
        }
    }

    if (showBrowser) {
        InstalledAppsDialog(
            apps = installedApps,
            isScanning = isScanning,
            libraryPackages = state.games.map { it.packageName }.toSet(),
            onDismiss = { showBrowser = false },
            onAdd = { app ->
                viewModel.addGame(app)
                Toast.makeText(context, "${app.label} added to the console", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun GameTile(
    game: Game,
    onOpen: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    ChamferPanel(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(packageName = game.packageName, size = 38.dp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle favourite",
                    tint = if (game.isFavorite) Console.VioletLight else Console.TextDim,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(Space.s))
        Text(
            text = game.label,
            style = MaterialTheme.typography.titleMedium,
            color = Console.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (game.isInstalled) game.packageName else "Not installed",
            style = MaterialTheme.typography.labelSmall,
            color = Console.TextDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Space.s))
        AngularButton(
            label = "Launch",
            filled = true,
            enabled = game.isInstalled,
            onClick = onLaunch,
        )
    }
}

@Composable
private fun InstalledAppsDialog(
    apps: List<InstalledApp>,
    isScanning: Boolean,
    libraryPackages: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (InstalledApp) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxSize(0.84f)
                .clip(PanelShape)
                .background(Console.Panel)
                .padding(Space.l),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Add apps", style = MaterialTheme.typography.headlineSmall, color = Console.Text)
                        Text(
                            text = "Every app with a launcher icon. PLAYCORE does not decide what a game is.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Console.TextDim,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Console.TextDim)
                    }
                }
                Spacer(Modifier.height(Space.m))
                when {
                    isScanning -> LoadingState("Scanning installed apps")
                    apps.isEmpty() -> EmptyState(
                        title = "No launchable apps found",
                        message = "This device reported no apps with a launcher activity.",
                    )
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        items(apps, key = { it.packageName }) { app ->
                            val added = libraryPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(TileShape)
                                    .clickable(enabled = !added) { onAdd(app) }
                                    .padding(vertical = Space.s),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIcon(packageName = app.packageName, size = 34.dp)
                                Spacer(Modifier.width(Space.m))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Console.Text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Console.TextDim,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = if (added) "ADDED" else "ADD",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (added) Console.TextFaint else Console.VioletLight,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
