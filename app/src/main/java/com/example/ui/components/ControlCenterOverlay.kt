package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenLockRotation
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlCenterState
import com.example.model.FocusMode
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraCoral
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraTeal

@Composable
fun ControlCenterOverlay(
    visible: Boolean,
    state: ControlCenterState,
    silencedNotificationCount: Int,
    onDismiss: () -> Unit,
    onToggleWifi: () -> Unit,
    onOpenWifiPanel: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onToggleAirplane: () -> Unit,
    onToggleCellular: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSliderDetent: () -> Unit = {},
    onToggleOrientation: () -> Unit,
    onCycleFocus: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onOpenLockScreen: () -> Unit,
    isDefaultHomeApp: Boolean = false,
    onOpenDefaultHomePrompt: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { -it / 5 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
        ),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 5 })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x990A0F1A))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -28f) onDismiss()
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("control_center_overlay"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Control Center",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(
                                    if (isDefaultHomeApp) AuraTeal.copy(alpha = 0.24f)
                                    else AuraBlue.copy(alpha = 0.85f)
                                )
                                .border(
                                    0.75.dp,
                                    Color.White.copy(alpha = 0.30f),
                                    RoundedCornerShape(19.dp)
                                )
                                .clickable(onClick = onOpenDefaultHomePrompt)
                                .padding(horizontal = 12.dp)
                                .testTag("cc_set_default_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = "Default Home Launcher",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isDefaultHomeApp) "Default Home" else "Set Default",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .clickable(onClick = onOpenLockScreen)
                                .testTag("cc_lock_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Lock Screen",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .clickable(onClick = onDismiss)
                                .testTag("cc_close_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Control Center",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Top Row: Connectivity 2x2 + Media Player 2x2
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ConnectivityBentoTile(
                        state = state,
                        onToggleAirplane = onToggleAirplane,
                        onToggleCellular = onToggleCellular,
                        onToggleWifi = onToggleWifi,
                        onLongPressWifi = onOpenWifiPanel,
                        onToggleBluetooth = onToggleBluetooth,
                        onLongPressBluetooth = onOpenBluetoothSettings,
                        modifier = Modifier.weight(1f)
                    )

                    MediaPlayerBentoTile(
                        state = state,
                        onTogglePlayPause = onTogglePlayPause,
                        onSkipPrev = onSkipPrev,
                        onSkipNext = onSkipNext,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Middle Row: Quick Toggles (left) + Brightness & Volume Sliders (right)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ControlTileButton(
                                icon = Icons.Rounded.ScreenLockRotation,
                                label = "Rotation",
                                isActive = state.orientationLocked,
                                activeColor = AuraCoral,
                                onClick = onToggleOrientation,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cc_rotation_tile")
                            )
                            ControlTileButton(
                                icon = if (state.flashlightOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                                label = "Torch",
                                isActive = state.flashlightOn,
                                activeColor = AuraBlue,
                                onClick = onToggleFlashlight,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cc_flashlight_tile")
                            )
                        }

                        // Focus Mode wide pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    if (state.focusMode != FocusMode.OFF) Color.White.copy(alpha = 0.92f)
                                    else Color.White.copy(alpha = 0.16f)
                                )
                                .border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                                .clickable(onClick = onCycleFocus)
                                .padding(horizontal = 14.dp)
                                .testTag("cc_focus_tile")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (state.focusMode != FocusMode.OFF) state.focusMode.tint
                                        else Color.White.copy(alpha = 0.18f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NightsStay,
                                    contentDescription = "Focus Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = state.focusMode.label,
                                    color = if (state.focusMode != FocusMode.OFF) Color(0xFF0F172A) else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = when {
                                        state.focusMode == FocusMode.OFF -> "Tap to cycle"
                                        silencedNotificationCount > 0 -> "$silencedNotificationCount alerts filtered"
                                        else -> "Active"
                                    },
                                    color = if (state.focusMode != FocusMode.OFF) Color(0xFF475569) else Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Brightness + Volume Vertical Sliders
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(148.dp)
                    ) {
                        VerticalPillSlider(
                            value = state.brightness,
                            onValueChange = onBrightnessChange,
                            onStepDetent = onSliderDetent,
                            icon = Icons.Rounded.BrightnessHigh,
                            contentDescription = "Brightness slider",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_brightness_slider")
                        )
                        VerticalPillSlider(
                            value = state.mediaVolume,
                            onValueChange = onVolumeChange,
                            onStepDetent = onSliderDetent,
                            icon = Icons.Rounded.VolumeUp,
                            contentDescription = "Media volume slider",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cc_volume_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Utility Row: Timer, Calculator, Camera, Wallpapers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ControlTileButton(
                        icon = Icons.Rounded.Timer,
                        label = "Timer",
                        isActive = false,
                        activeColor = AuraAmber,
                        onClick = onOpenTimer,
                        modifier = Modifier.weight(1f)
                    )
                    ControlTileButton(
                        icon = Icons.Rounded.Calculate,
                        label = "Calc",
                        isActive = false,
                        activeColor = AuraAmber,
                        onClick = onOpenCalculator,
                        modifier = Modifier.weight(1f)
                    )
                    ControlTileButton(
                        icon = Icons.Rounded.CameraAlt,
                        label = "Camera",
                        isActive = false,
                        activeColor = AuraBlue,
                        onClick = onOpenCamera,
                        modifier = Modifier.weight(1f)
                    )
                    ControlTileButton(
                        icon = Icons.Rounded.Wallpaper,
                        label = "Backdrop",
                        isActive = false,
                        activeColor = AuraIndigo,
                        onClick = onOpenWallpaperStudio,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cc_wallpaper_tile")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Swipe-up drag pill hint
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.48f))
                        .clickable(onClick = onDismiss)
                )
            }
        }
    }
}

@Composable
private fun ConnectivityBentoTile(
    state: ControlCenterState,
    onToggleAirplane: () -> Unit,
    onToggleCellular: () -> Unit,
    onToggleWifi: () -> Unit,
    onLongPressWifi: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onLongPressBluetooth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(152.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .border(0.8.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(26.dp))
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircleToggle(
                icon = Icons.Rounded.AirplanemodeActive,
                label = "Airplane Mode",
                isActive = state.airplaneMode,
                activeColor = AuraAmber,
                onClick = onToggleAirplane
            )
            CircleToggle(
                icon = Icons.Rounded.CellTower,
                label = "Cellular Data",
                isActive = state.cellularConnected && !state.airplaneMode,
                activeColor = AuraTeal,
                onClick = onToggleCellular
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircleToggle(
                icon = Icons.Rounded.Wifi,
                label = "Wi-Fi",
                isActive = state.wifiConnected && !state.airplaneMode,
                activeColor = AuraBlue,
                onClick = onToggleWifi,
                onLongClick = onLongPressWifi
            )
            CircleToggle(
                icon = Icons.Rounded.Bluetooth,
                label = "Bluetooth",
                isActive = state.bluetoothEnabled && !state.airplaneMode,
                activeColor = AuraBlue,
                onClick = onToggleBluetooth,
                onLongClick = onLongPressBluetooth
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CircleToggle(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val bg by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.18f),
        label = "circle_toggle_bg"
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MediaPlayerBentoTile(
    state: ControlCenterState,
    onTogglePlayPause: () -> Unit,
    onSkipPrev: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .height(152.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x4D38BDF8), Color(0x4D1E293B))
                )
            )
            .border(0.8.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(26.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AuraBlue.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.currentTrackTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.currentTrackArtist,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSkipPrev)
                    .testTag("cc_media_prev"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(onClick = onTogglePlayPause)
                    .testTag("cc_media_play_pause"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isPlayingMusic) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlayingMusic) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSkipNext)
                    .testTag("cc_media_next"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ControlTileButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isActive) Color.White.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.16f),
        label = "tile_bg"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isActive) Color(0xFF0F172A) else Color.White.copy(alpha = 0.85f),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun VerticalPillSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onStepDetent: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var heightPx by remember { mutableFloatStateOf(1f) }
    var lastDetentBucket by remember { mutableFloatStateOf((value * 10f).toInt().toFloat()) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .border(0.8.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(24.dp))
            .onSizeChanged { heightPx = it.height.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val fraction = 1f - (change.position.y / heightPx).coerceIn(0f, 1f)
                    val bucket = (fraction * 10f).toInt().toFloat()
                    if (bucket != lastDetentBucket) {
                        lastDetentBucket = bucket
                        onStepDetent()
                    }
                    onValueChange(fraction)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(value.coerceIn(0.05f, 1f))
                .background(Color.White.copy(alpha = 0.90f))
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (value > 0.25f) Color(0xFF334155) else Color.White,
            modifier = Modifier
                .padding(bottom = 14.dp)
                .size(24.dp)
        )
    }
}
