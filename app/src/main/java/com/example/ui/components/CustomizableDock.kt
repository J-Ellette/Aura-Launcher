package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DockStyle
import com.example.model.LauncherApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomizableDock(
    dockApps: List<LauncherApp>,
    dockStyle: DockStyle,
    isEditMode: Boolean,
    drawableResolver: (String) -> Drawable?,
    onAppClick: (LauncherApp) -> Unit,
    onAppLongClick: () -> Unit,
    onRemoveFromDock: (String) -> Unit,
    onOpenDockCustomizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor) = when (dockStyle) {
        DockStyle.FROSTED_GLASS -> Color.White.copy(alpha = 0.26f) to Color.White.copy(alpha = 0.44f)
        DockStyle.CLEAR_TINT -> Color.White.copy(alpha = 0.14f) to Color.White.copy(alpha = 0.30f)
        DockStyle.DARK_OBSIDIAN -> Color(0xB30F172A) to Color.White.copy(alpha = 0.20f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(30.dp))
                .combinedClickable(
                    onClick = {
                        if (isEditMode) onOpenDockCustomizer()
                    },
                    onLongClick = onOpenDockCustomizer
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("customizable_dock"),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.forEachIndexed { index, app ->
                AppIconTile(
                    app = app,
                    drawable = drawableResolver(app.id),
                    showLabel = false,
                    iconSize = 54.dp,
                    isEditMode = isEditMode,
                    jigglePhaseOffset = index.toFloat(),
                    onClick = { onAppClick(app) },
                    onLongClick = onAppLongClick,
                    onRemoveClick = { onRemoveFromDock(app.id) }
                )
            }
        }
    }
}
