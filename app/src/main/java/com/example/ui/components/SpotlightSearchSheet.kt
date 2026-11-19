package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSearchHit
import com.example.model.LauncherApp

@Composable
fun SpotlightSearchSheet(
    visible: Boolean,
    query: String,
    allApps: List<LauncherApp>,
    searchIndexedApps: (String) -> List<AppSearchHit>,
    drawableResolver: (String) -> Drawable?,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onToggleDnd: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenLockScreen: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onOpenWidgetPicker: () -> Unit,
    isDefaultHomeApp: Boolean = false,
    onOpenDefaultLauncherPrompt: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it / 4 },
        exit = fadeOut() + slideOutVertically { -it / 4 }
    ) {
        val matchingApps = remember(allApps, query) {
            searchIndexedApps(query).take(10).map { it.app }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xA6090D16))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .padding(top = 18.dp, start = 18.dp, end = 18.dp)
                .testTag("spotlight_overlay"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
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
                            .border(0.9.dp, Color.White.copy(alpha = 0.36f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search apps, widgets, or actions",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("spotlight_search_input")
                            )
                        }
                    }

                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("spotlight_cancel_button")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (query.isBlank()) "SUGGESTIONS" else "APPS",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    items(matchingApps, key = { it.id }) { app ->
                        AppIconTile(
                            app = app,
                            drawable = drawableResolver(app.id),
                            onClick = {
                                onDismiss()
                                onAppClick(app)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "QUICK ACTIONS",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val actions = listOf(
                        Triple(
                            if (isDefaultHomeApp) "Manage Default Home" else "Set as Default Launcher",
                            Icons.Rounded.Home,
                            onOpenDefaultLauncherPrompt
                        ),
                        Triple("Toggle Do Not Disturb", Icons.Rounded.DoNotDisturbOn, onToggleDnd),
                        Triple("Control Center", Icons.Rounded.Tune, onOpenControlCenter),
                        Triple("Lock Screen & Fonts", Icons.Rounded.Lock, onOpenLockScreen),
                        Triple("Dynamic Wallpapers", Icons.Rounded.Wallpaper, onOpenWallpaperStudio),
                        Triple("Add Home Widget", Icons.Rounded.DashboardCustomize, onOpenWidgetPicker),
                        Triple("Toggle Flashlight", Icons.Rounded.FlashlightOn, onToggleFlashlight)
                    ).filter {
                        query.isBlank() || it.first.contains(query.trim(), ignoreCase = true)
                    }

                    items(actions, key = { it.first }) { (title, icon, action) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(0.75.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                .clickable {
                                    onDismiss()
                                    action()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
