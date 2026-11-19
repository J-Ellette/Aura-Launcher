package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.model.HomeFolder
import com.example.model.LauncherApp
import com.example.ui.theme.AuraCoral

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconTile(
    app: LauncherApp,
    drawable: Drawable?,
    showLabel: Boolean = true,
    iconSize: Dp = 58.dp,
    isEditMode: Boolean = false,
    jigglePhaseOffset: Float = 0f,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onRemoveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_press_scale"
    )

    val jiggleRotation = if (isEditMode) {
        val transition = rememberInfiniteTransition(label = "jiggle")
        val angle by transition.animateFloat(
            initialValue = -2.2f,
            targetValue = 2.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "jiggle_angle"
        )
        if ((jigglePhaseOffset.toInt() % 2) == 0) angle else -angle
    } else {
        0f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                rotationZ = jiggleRotation
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("app_icon_${app.id}")
    ) {
        Box(modifier = Modifier.size(iconSize + 8.dp), contentAlignment = Alignment.Center) {
            SquircleAppVisual(
                app = app,
                drawable = drawable,
                size = iconSize
            )

            // Notification badge
            if (app.badgeCount > 0 && !isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-1).dp, y = 1.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AuraCoral)
                        .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app.badgeCount > 9) "9+" else app.badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Edit mode remove badge
            if (isEditMode && onRemoveClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 0.dp, y = 0.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xE6E2E8F0))
                        .clickable(onClick = onRemoveClick)
                        .testTag("remove_app_${app.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Remove ${app.label}",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun SquircleAppVisual(
    app: LauncherApp,
    drawable: Drawable?,
    size: Dp,
    cornerRadius: Dp = (size.value * 0.25f).dp
) {
    val bitmap = remember(app.id, drawable) {
        drawable?.let {
            runCatching { it.toBitmap(width = 160, height = 160).asImageBitmap() }.getOrNull()
        }
    }

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 6.dp, shape = shape, clip = false)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(app.accentColor, app.secondaryColor)
                )
            )
            .border(0.75.dp, Color.White.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Subtle top specular highlight for iOS icon sheen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.24f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.14f)
                            )
                        )
                    )
            )
            Icon(
                imageVector = iconForKey(app.iconKey),
                contentDescription = app.label,
                tint = Color.White,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderIconTile(
    folder: HomeFolder,
    apps: List<LauncherApp>,
    drawableResolver: (String) -> Drawable?,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "folder_press_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("home_folder_${folder.id}")
    ) {
        Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.24f))
                    .border(0.9.dp, Color.White.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val preview = apps.take(9)
                    for (row in 0 until 3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            for (col in 0 until 3) {
                                val item = preview.getOrNull(row * 3 + col)
                                if (item != null) {
                                    SquircleAppVisual(
                                        app = item,
                                        drawable = drawableResolver(item.id),
                                        size = 13.dp,
                                        cornerRadius = 4.dp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xE6E2E8F0))
                        .clickable(onClick = onRemoveClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Delete ${folder.title}",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = folder.title,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun iconForKey(key: String): ImageVector = when (key) {
    "phone" -> Icons.Rounded.Call
    "messages" -> Icons.Rounded.ChatBubble
    "safari" -> Icons.Rounded.Explore
    "camera" -> Icons.Rounded.CameraAlt
    "calendar" -> Icons.Rounded.CalendarMonth
    "calculator" -> Icons.Rounded.Calculate
    "settings" -> Icons.Rounded.Settings
    "wallpaper" -> Icons.Rounded.Wallpaper
    "lock_style" -> Icons.Rounded.Style
    "dock" -> Icons.Rounded.Dock
    "widgets" -> Icons.Rounded.DashboardCustomize
    "controls" -> Icons.Rounded.Tune
    "lock" -> Icons.Rounded.Lock
    "photos" -> Icons.Rounded.PhotoLibrary
    "music" -> Icons.Rounded.MusicNote
    "mail" -> Icons.Rounded.Mail
    "maps" -> Icons.Rounded.Map
    "clock" -> Icons.Rounded.Schedule
    "weather" -> Icons.Rounded.AutoAwesome
    "home" -> Icons.Rounded.Home
    else -> Icons.Rounded.Apps
}
