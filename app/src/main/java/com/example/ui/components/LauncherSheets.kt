package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveSheet
import com.example.model.DockStyle
import com.example.model.LauncherApp
import com.example.model.TimeOfDayPhase
import com.example.model.WallpaperMode
import com.example.model.WidgetType
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherBottomSheets(
    activeSheet: ActiveSheet,
    wallpaperMode: WallpaperMode,
    activePhase: TimeOfDayPhase,
    dockStyle: DockStyle,
    dockAppIds: List<String>,
    allApps: List<LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    onDismiss: () -> Unit,
    onSelectWallpaperMode: (WallpaperMode) -> Unit,
    onAddWidget: (WidgetType) -> Unit,
    onSelectDockStyle: (DockStyle) -> Unit,
    onToggleDockApp: (String) -> Unit,
    onCreateFolder: (String, List<String>) -> Unit,
    onOpenCreateFolderSheet: () -> Unit
) {
    if (activeSheet == ActiveSheet.NONE) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF2121826),
        contentColor = Color.White
    ) {
        when (activeSheet) {
            ActiveSheet.WALLPAPER_STUDIO -> WallpaperStudioSheetContent(
                selectedMode = wallpaperMode,
                activePhase = activePhase,
                onSelectMode = onSelectWallpaperMode
            )

            ActiveSheet.WIDGET_PICKER -> WidgetPickerSheetContent(
                onAddWidget = onAddWidget
            )

            ActiveSheet.DOCK_CUSTOMIZER -> DockCustomizerSheetContent(
                dockStyle = dockStyle,
                dockAppIds = dockAppIds,
                allApps = allApps,
                drawableResolver = drawableResolver,
                onSelectDockStyle = onSelectDockStyle,
                onToggleDockApp = onToggleDockApp,
                onOpenCreateFolderSheet = onOpenCreateFolderSheet
            )

            ActiveSheet.CREATE_FOLDER -> CreateFolderSheetContent(
                allApps = allApps,
                drawableResolver = drawableResolver,
                onCreateFolder = onCreateFolder
            )

            ActiveSheet.LOCK_CUSTOMIZER, ActiveSheet.NONE -> Unit
        }
    }
}

@Composable
private fun WallpaperStudioSheetContent(
    selectedMode: WallpaperMode,
    activePhase: TimeOfDayPhase,
    onSelectMode: (WallpaperMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp)
            .testTag("wallpaper_studio_sheet")
    ) {
        Text(
            text = "Dynamic Wallpapers",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Currently active: ${activePhase.label} (${activePhase.timeWindow})",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.72f)
        )

        // Auto Time-of-Day toggle card
        val isAuto = selectedMode == WallpaperMode.AUTO_DYNAMIC
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isAuto) AuraBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                .border(
                    width = if (isAuto) 1.5.dp else 0.8.dp,
                    color = if (isAuto) AuraBlue else Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onSelectMode(WallpaperMode.AUTO_DYNAMIC) }
                .padding(14.dp)
                .testTag("wallpaper_auto_option")
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto (Time of Day)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Automatically transitions across Dawn, Day, Sunset, and Midnight.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            if (isAuto) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = AuraBlue
                )
            }
        }

        Text(
            text = "Preview or Pin Time Phase",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.85f)
        )

        val phaseModes = listOf(
            WallpaperMode.MANUAL_DAWN to TimeOfDayPhase.DAWN,
            WallpaperMode.MANUAL_DAY to TimeOfDayPhase.DAY,
            WallpaperMode.MANUAL_SUNSET to TimeOfDayPhase.SUNSET,
            WallpaperMode.MANUAL_NIGHT to TimeOfDayPhase.NIGHT
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(phaseModes, key = { it.first.name }) { (mode, phase) ->
                val selected = selectedMode == mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(106.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(
                            width = if (selected) 2.dp else 0.8.dp,
                            color = if (selected) AuraBlue else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(6.dp)
                        .testTag("wallpaper_mode_${mode.name}")
                ) {
                    Image(
                        painter = painterResource(id = phase.wallpaperRes),
                        contentDescription = phase.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = phase.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetPickerSheetContent(
    onAddWidget: (WidgetType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp)
            .testTag("widget_picker_sheet")
    ) {
        Text(
            text = "Widget Gallery",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Tap any widget to place it on your current Home Screen page.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.height(340.dp)
        ) {
            items(WidgetType.entries, key = { it.name }) { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                        .clickable { onAddWidget(type) }
                        .padding(14.dp)
                        .testTag("pick_widget_${type.name}")
                ) {
                    Column {
                        Text(
                            text = type.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${type.subtitle} • ${if (type.defaultSize.name.startsWith("SMALL")) "2x2" else "4x2"}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AuraBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add ${type.title}",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockCustomizerSheetContent(
    dockStyle: DockStyle,
    dockAppIds: List<String>,
    allApps: List<LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    onSelectDockStyle: (DockStyle) -> Unit,
    onToggleDockApp: (String) -> Unit,
    onOpenCreateFolderSheet: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp)
            .testTag("dock_customizer_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dock & Layout",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(AuraBlue.copy(alpha = 0.25f))
                    .clickable(onClick = onOpenCreateFolderSheet)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CreateNewFolder,
                    contentDescription = "New Folder",
                    tint = AuraBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "New Folder",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = "Dock Glass Style",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.85f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DockStyle.entries.forEach { style ->
                val selected = style == dockStyle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) AuraBlue.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                        .border(
                            width = if (selected) 1.5.dp else 0.75.dp,
                            color = if (selected) AuraBlue else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectDockApp(style, onSelectDockStyle) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = style.label,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = "Pinned Dock Apps (${dockAppIds.size}/5)",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.85f)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(260.dp)
        ) {
            items(allApps, key = { it.id }) { app ->
                val pinned = app.id in dockAppIds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (pinned) AuraBlue.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f))
                        .clickable { onToggleDockApp(app.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    SquircleAppVisual(
                        app = app,
                        drawable = drawableResolver(app.id),
                        size = 38.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (pinned) AuraTeal else Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pinned) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Pinned",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private inline fun onSelectDockApp(style: DockStyle, cb: (DockStyle) -> Unit) = cb(style)

@Composable
private fun CreateFolderSheetContent(
    allApps: List<LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    onCreateFolder: (String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "New Home Folder",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Folder Name") },
            placeholder = { Text("e.g. Productivity") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Select Apps (${selectedIds.size} selected)",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.78f)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(allApps, key = { it.id }) { app ->
                val isSelected = app.id in selectedIds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) AuraBlue.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
                        .clickable {
                            if (isSelected) selectedIds.remove(app.id) else selectedIds.add(app.id)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    SquircleAppVisual(
                        app = app,
                        drawable = drawableResolver(app.id),
                        size = 34.dp,
                        cornerRadius = 9.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = AuraTeal
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onCreateFolder(title, selectedIds.toList()) },
            enabled = selectedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Folder")
        }
    }
}
