package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppIndexSnapshot
import com.example.data.AppSearchHit
import com.example.model.AppCategory
import com.example.model.LauncherApp
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraTeal

@Composable
fun AppLibraryPage(
    allApps: List<LauncherApp>,
    indexSnapshot: AppIndexSnapshot,
    searchIndexedApps: (String) -> List<AppSearchHit>,
    drawableResolver: (String) -> Drawable?,
    onAppClick: (LauncherApp) -> Unit,
    onAddToHome: (String) -> Unit,
    onOpenCategory: (AppCategory) -> Unit,
    onRefreshIndex: () -> Unit,
    onToggleDnd: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var listMode by remember { mutableStateOf(false) }

    val grouped = remember(allApps) {
        AppCategory.entries.mapNotNull { category ->
            val items = if (category == AppCategory.SUGGESTIONS) {
                allApps.take(6)
            } else {
                allApps.filter { it.category == category }
            }
            if (items.isEmpty()) null else category to items
        }
    }

    val indexedHits = remember(allApps, indexSnapshot.lastIndexedEpochMs, query) {
        searchIndexedApps(query)
    }
    val alphabeticalApps = remember(allApps) {
        allApps.sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("app_library_page")
    ) {
        // Search Bar + A-Z List Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.36f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search App Library",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "App Library",
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_library_search_input")
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear search",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.36f), RoundedCornerShape(16.dp))
                    .clickable { listMode = !listMode }
                    .testTag("app_library_view_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (listMode) Icons.Rounded.GridView else Icons.Rounded.FormatListBulleted,
                    contentDescription = if (listMode) "Show folders" else "Show alphabetical list",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Spotlight Index Status Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Spotlight Index",
                    tint = Color.White.copy(alpha = 0.78f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (indexSnapshot.isIndexing) "Indexing apps..."
                    else "Spotlight Index • ${indexSnapshot.indexedCount} apps ready",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("app_library_index_status")
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(onClick = onRefreshIndex)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("app_library_reindex_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Re-index installed apps",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Re-index",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (query.isNotBlank()) {
            val topHit = indexedHits.firstOrNull()
            val remainingHits = indexedHits.drop(1)
            val quickCommands = listOf(
                Triple("Toggle Do Not Disturb", Icons.Rounded.DoNotDisturbOn, onToggleDnd) to "dnd focus quiet silence mute",
                Triple("Open Control Center", Icons.Rounded.Tune, onOpenControlCenter) to "control wifi bluetooth volume brightness",
                Triple("Dynamic Wallpapers", Icons.Rounded.Wallpaper, onOpenWallpaperStudio) to "wallpaper dawn day sunset night theme"
            ).filter { (cmd, keywords) ->
                val q = query.trim().lowercase()
                cmd.first.contains(q, ignoreCase = true) || keywords.contains(q)
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("app_library_spotlight_results")
            ) {
                if (topHit != null) {
                    item(key = "top_hit_header") {
                        Text(
                            text = "TOP HIT",
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    item(key = "top_hit_${topHit.app.id}") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.24f))
                                .border(0.9.dp, Color.White.copy(alpha = 0.38f), RoundedCornerShape(22.dp))
                                .clickable { onAppClick(topHit.app) }
                                .padding(14.dp)
                                .testTag("spotlight_top_hit")
                        ) {
                            SquircleAppVisual(
                                app = topHit.app,
                                drawable = drawableResolver(topHit.app.id),
                                size = 54.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topHit.app.label,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${topHit.app.category.title} • ${topHit.matchReason}",
                                    color = AuraTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AuraBlue)
                                    .clickable { onAddToHome(topHit.app.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ Home",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (quickCommands.isNotEmpty()) {
                    item(key = "quick_cmd_header") {
                        Text(
                            text = "QUICK ACTIONS",
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    items(quickCommands, key = { it.first.first }) { (cmd, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(AuraPurple.copy(alpha = 0.24f))
                                .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                                .clickable { cmd.third() }
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Icon(
                                imageVector = cmd.second,
                                contentDescription = cmd.first,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cmd.first,
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (remainingHits.isNotEmpty()) {
                    item(key = "apps_header") {
                        Text(
                            text = "MATCHING APPS",
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    items(remainingHits, key = { it.app.id }) { hit ->
                        AppSearchResultRow(
                            app = hit.app,
                            subtitle = "${hit.app.category.title} • ${hit.matchReason}",
                            drawable = drawableResolver(hit.app.id),
                            onClick = { onAppClick(hit.app) },
                            onAddToHome = { onAddToHome(hit.app.id) }
                        )
                    }
                }

                if (topHit == null && quickCommands.isEmpty()) {
                    item(key = "no_hits") {
                        Text(
                            text = "No apps or actions matched \"$query\"",
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }
        } else if (listMode) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alphabeticalApps, key = { it.id }) { app ->
                    AppSearchResultRow(
                        app = app,
                        subtitle = app.category.title,
                        drawable = drawableResolver(app.id),
                        onClick = { onAppClick(app) },
                        onAddToHome = { onAddToHome(app.id) }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(grouped, key = { it.first.name }) { (category, apps) ->
                    AppLibraryCategoryCard(
                        category = category,
                        apps = apps,
                        drawableResolver = drawableResolver,
                        onAppClick = onAppClick,
                        onExpandCategory = { onOpenCategory(category) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppLibraryCategoryCard(
    category: AppCategory,
    apps: List<LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    onAppClick: (LauncherApp) -> Unit,
    onExpandCategory: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("library_folder_${category.name}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(0.9.dp, Color.White.copy(alpha = 0.36f), RoundedCornerShape(28.dp))
                .combinedClickable(
                    onClick = onExpandCategory,
                    onLongClick = onExpandCategory
                )
                .padding(14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val first = apps.getOrNull(0)
                    val second = apps.getOrNull(1)
                    if (first != null) {
                        Box(modifier = Modifier.clickable { onAppClick(first) }) {
                            SquircleAppVisual(
                                app = first,
                                drawable = drawableResolver(first.id),
                                size = 58.dp,
                                cornerRadius = 15.dp
                            )
                        }
                    }
                    if (second != null) {
                        Box(modifier = Modifier.clickable { onAppClick(second) }) {
                            SquircleAppVisual(
                                app = second,
                                drawable = drawableResolver(second.id),
                                size = 58.dp,
                                cornerRadius = 15.dp
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val third = apps.getOrNull(2)
                    if (third != null) {
                        Box(modifier = Modifier.clickable { onAppClick(third) }) {
                            SquircleAppVisual(
                                app = third,
                                drawable = drawableResolver(third.id),
                                size = 58.dp,
                                cornerRadius = 15.dp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(58.dp))
                    }

                    // 4th slot: mini 2x2 cluster showing additional apps
                    val remaining = apps.drop(3).take(4)
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onExpandCategory)
                            .padding(5.dp)
                    ) {
                        if (remaining.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    remaining.getOrNull(0)?.let {
                                        SquircleAppVisual(it, drawableResolver(it.id), 22.dp, 6.dp)
                                    }
                                    remaining.getOrNull(1)?.let {
                                        SquircleAppVisual(it, drawableResolver(it.id), 22.dp, 6.dp)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    remaining.getOrNull(2)?.let {
                                        SquircleAppVisual(it, drawableResolver(it.id), 22.dp, 6.dp)
                                    }
                                    remaining.getOrNull(3)?.let {
                                        SquircleAppVisual(it, drawableResolver(it.id), 22.dp, 6.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryFolderModal(
    category: AppCategory?,
    allApps: List<LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    onDismiss: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onAddToHome: (String) -> Unit
) {
    AnimatedVisibility(
        visible = category != null,
        enter = fadeIn() + scaleIn(
            initialScale = 0.82f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.86f)
    ) {
        if (category != null) {
            val apps = if (category == AppCategory.SUGGESTIONS) allApps.take(9)
            else allApps.filter { it.category == category }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.88f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Text(
                        text = category.title,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.24f))
                            .border(1.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(32.dp))
                            .padding(14.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.height(300.dp)
                        ) {
                            items(apps, key = { it.id }) { app ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AppIconTile(
                                        app = app,
                                        drawable = drawableResolver(app.id),
                                        onClick = {
                                            onDismiss()
                                            onAppClick(app)
                                        },
                                        onLongClick = { onAddToHome(app.id) }
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.14f))
                                            .clickable { onAddToHome(app.id) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add to Home",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Home",
                                            color = Color.White,
                                            fontSize = 10.sp
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

@Composable
private fun AppSearchResultRow(
    app: LauncherApp,
    subtitle: String,
    drawable: Drawable?,
    onClick: () -> Unit,
    onAddToHome: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        SquircleAppVisual(
            app = app,
            drawable = drawable,
            size = 44.dp,
            cornerRadius = 11.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AuraBlue.copy(alpha = 0.85f))
                .clickable(onClick = onAddToHome)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "+ Home",
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
