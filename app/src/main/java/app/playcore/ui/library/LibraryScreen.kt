package app.playcore.ui.library

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import app.playcore.core.designsystem.PlaycoreSpacing
import app.playcore.core.model.Game
import app.playcore.core.model.InstalledApp
import app.playcore.core.model.LayoutMode
import app.playcore.core.model.LibrarySort
import app.playcore.ui.components.AppIcon
import app.playcore.ui.components.EmptyState
import app.playcore.ui.components.ErrorState
import app.playcore.ui.components.LoadingState
import app.playcore.ui.components.PlaycoreCard
import app.playcore.ui.components.SectionHeader
import android.widget.Toast

@Composable
fun LibraryScreen(
    onOpenGame: (String) -> Unit,
    viewModel: LibraryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showBrowser by remember { mutableStateOf(false) }
    var layout by remember { mutableStateOf(LayoutMode.GRID) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = PlaycoreSpacing.l, vertical = PlaycoreSpacing.l)) {
            SectionHeader(
                title = "Library",
                subtitle = "${state.totalInLibrary} apps you chose to keep",
                trailing = {
                    Row {
                        IconButton(onClick = {
                            viewModel.loadInstalledApps()
                            showBrowser = true
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add apps")
                        }
                        IconButton(onClick = {
                            layout = if (layout == LayoutMode.GRID) LayoutMode.LIST else LayoutMode.GRID
                        }) {
                            Text(
                                text = if (layout == LayoutMode.GRID) "List" else "Grid",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )

            Spacer(Modifier.height(PlaycoreSpacing.m))

            OutlinedTextField(
                value = state.query.search,
                onValueChange = viewModel::setSearch,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search name or package") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.search.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearch("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                keyboardActions = KeyboardActions.Default,
            )

            Spacer(Modifier.height(PlaycoreSpacing.s))

            Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s)) {
                FilterChip(
                    selected = state.query.sort == LibrarySort.RECENT,
                    onClick = { viewModel.setSort(LibrarySort.RECENT) },
                    label = { Text("Recent") },
                )
                FilterChip(
                    selected = state.query.sort == LibrarySort.NAME,
                    onClick = { viewModel.setSort(LibrarySort.NAME) },
                    label = { Text("Name") },
                )
                FilterChip(
                    selected = state.query.sort == LibrarySort.FAVORITES_FIRST,
                    onClick = { viewModel.setSort(LibrarySort.FAVORITES_FIRST) },
                    label = { Text("Favourites") },
                )
                FilterChip(
                    selected = state.query.favoritesOnly,
                    onClick = viewModel::toggleFavoritesOnly,
                    label = { Text("Only favourites") },
                )
            }

            if (state.categories.isNotEmpty()) {
                Spacer(Modifier.height(PlaycoreSpacing.s))
                Row(horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s)) {
                    FilterChip(
                        selected = state.query.categoryId == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All categories") },
                    )
                    state.categories.take(3).forEach { category ->
                        FilterChip(
                            selected = state.query.categoryId == category.id,
                            onClick = { viewModel.setCategoryFilter(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }
        }

        when {
            state.isLoading && state.games.isEmpty() -> LoadingState(message = "Reading installed apps")

            state.error != null -> ErrorState(
                title = "Discovery failed",
                message = state.error ?: "Installed apps could not be read.",
                onRetry = viewModel::refreshInstalled,
            )

            state.games.isEmpty() -> EmptyState(
                title = if (state.totalInLibrary == 0) "Your library is empty" else "No matches",
                message = if (state.totalInLibrary == 0) {
                    "PLAYCORE does not guess which apps are games. Open the browser and add the ones you play."
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

            layout == LayoutMode.GRID -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = PlaycoreSpacing.l,
                    end = PlaycoreSpacing.l,
                    bottom = PlaycoreSpacing.xxl,
                ),
                horizontalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m),
                verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.m),
            ) {
                gridItems(state.games, key = { it.packageName }) { game ->
                    GameGridCard(
                        game = game,
                        onOpen = { onOpenGame(game.packageName) },
                        onLaunch = {
                            viewModel.launch(game.packageName) { result ->
                                if (result != app.playcore.core.model.LaunchResult.Launched) {
                                    Toast.makeText(
                                        context,
                                        "That app could not be launched.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(game.packageName) },
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = PlaycoreSpacing.l,
                    end = PlaycoreSpacing.l,
                    bottom = PlaycoreSpacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.s),
            ) {
                items(state.games, key = { it.packageName }) { game ->
                    GameListRow(
                        game = game,
                        onOpen = { onOpenGame(game.packageName) },
                        onLaunch = {
                            viewModel.launch(game.packageName) { result ->
                                if (result != app.playcore.core.model.LaunchResult.Launched) {
                                    Toast.makeText(
                                        context,
                                        "That app could not be launched.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(game.packageName) },
                    )
                }
            }
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
                Toast.makeText(context, "${app.label} added to library", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun GameGridCard(
    game: Game,
    onOpen: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    PlaycoreCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(packageName = game.packageName, size = 40.dp)
            Spacer(Modifier.width(PlaycoreSpacing.s))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle favourite",
                    tint = if (game.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(PlaycoreSpacing.s))
        Text(
            text = game.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (game.isInstalled) game.packageName else "Not installed",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(PlaycoreSpacing.s))
        Button(onClick = onLaunch, enabled = game.isInstalled, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(PlaycoreSpacing.xs))
            Text("Launch")
        }
    }
}

@Composable
private fun GameListRow(
    game: Game,
    onOpen: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    PlaycoreCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(packageName = game.packageName, size = 40.dp)
            Spacer(Modifier.width(PlaycoreSpacing.m))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = game.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle favourite",
                    tint = if (game.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onLaunch, enabled = game.isInstalled) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Launch")
            }
        }
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxSize(0.82f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(modifier = Modifier.padding(PlaycoreSpacing.l)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add apps",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Every app with a launcher icon. PLAYCORE does not decide what a game is.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                Spacer(Modifier.height(PlaycoreSpacing.m))
                when {
                    isScanning -> LoadingState(message = "Scanning installed apps")
                    apps.isEmpty() -> EmptyState(
                        title = "No launchable apps found",
                        message = "This device reported no apps with a launcher activity.",
                    )
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(PlaycoreSpacing.xs)) {
                        items(apps, key = { it.packageName }) { app ->
                            val alreadyAdded = libraryPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !alreadyAdded) { onAdd(app) }
                                    .padding(vertical = PlaycoreSpacing.s),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIcon(packageName = app.packageName, size = 36.dp)
                                Spacer(Modifier.width(PlaycoreSpacing.m))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            if (alreadyAdded) {
                                                MaterialTheme.colorScheme.surfaceContainerHigh
                                            } else {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                            }
                                        )
                                        .padding(horizontal = PlaycoreSpacing.m, vertical = PlaycoreSpacing.xs),
                                ) {
                                    Text(
                                        text = if (alreadyAdded) "Added" else "Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (alreadyAdded) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
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
