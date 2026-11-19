package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HomeFolder
import com.example.model.LauncherApp
import com.example.ui.theme.AuraCoral

@Composable
fun FolderModalOverlay(
    folder: HomeFolder?,
    allAppsById: Map<String, LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onAppClick: (LauncherApp) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onRemoveAppFromFolder: (String, String) -> Unit
) {
    AnimatedVisibility(
        visible = folder != null,
        enter = fadeIn() + scaleIn(
            initialScale = 0.78f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.84f)
    ) {
        if (folder != null) {
            var editingTitle by remember(folder.id) { mutableStateOf(false) }
            var titleDraft by remember(folder.id, folder.title) { mutableStateOf(folder.title) }
            val folderApps = folder.appIds.mapNotNull { allAppsById[it] }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
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
                        .widthIn(max = 360.dp)
                        .fillMaxWidth(0.86f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (editingTitle) {
                            BasicTextField(
                                value = titleDraft,
                                onValueChange = {
                                    titleDraft = it
                                    onRenameFolder(folder.id, it)
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        } else {
                            Text(
                                text = folder.title,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { editingTitle = true }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = { editingTitle = !editingTitle }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Rename folder",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (isEditMode) {
                            IconButton(onClick = { onDeleteFolder(folder.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete folder",
                                    tint = AuraCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.24f))
                            .border(1.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(32.dp))
                            .padding(14.dp)
                            .testTag("folder_modal_card")
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.height(260.dp)
                        ) {
                            itemsIndexed(folderApps, key = { _, a -> a.id }) { idx, app ->
                                AppIconTile(
                                    app = app,
                                    drawable = drawableResolver(app.id),
                                    isEditMode = isEditMode,
                                    jigglePhaseOffset = idx.toFloat(),
                                    onClick = {
                                        onDismiss()
                                        onAppClick(app)
                                    },
                                    onRemoveClick = {
                                        onRemoveAppFromFolder(folder.id, app.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
