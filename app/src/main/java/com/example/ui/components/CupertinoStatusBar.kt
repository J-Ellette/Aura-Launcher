package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlCenterState
import com.example.model.DeviceTelemetry
import com.example.model.FocusMode
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraTeal

@Composable
fun CupertinoStatusBar(
    timeText: String,
    controlCenter: ControlCenterState,
    telemetry: DeviceTelemetry,
    isDynamicIslandExpanded: Boolean,
    onToggleDynamicIsland: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenLockScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topInset.coerceAtLeast(8.dp), start = 18.dp, end = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Live Clock
            Text(
                text = timeText.ifBlank { "9:41" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenLockScreen)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("status_time_button")
            )

            // Center: Dynamic Pill (Live Island)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xE60B0E14))
                    .border(0.75.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                    .clickable(onClick = onToggleDynamicIsland)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .padding(
                        horizontal = if (isDynamicIslandExpanded) 14.dp else 12.dp,
                        vertical = if (isDynamicIslandExpanded) 10.dp else 6.dp
                    )
                    .testTag("dynamic_island_pill")
            ) {
                if (!isDynamicIslandExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (controlCenter.isPlayingMusic) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = "Now playing",
                                tint = AuraTeal,
                                modifier = Modifier.size(13.dp)
                            )
                        } else if (controlCenter.focusMode != FocusMode.OFF) {
                            Icon(
                                imageVector = Icons.Rounded.NightsStay,
                                contentDescription = controlCenter.focusMode.label,
                                tint = controlCenter.focusMode.tint,
                                modifier = Modifier.size(13.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (telemetry.isCharging) AuraTeal else AuraBlue)
                            )
                        }
                        Text(
                            text = when {
                                controlCenter.isPlayingMusic -> controlCenter.currentTrackTitle
                                controlCenter.focusMode != FocusMode.OFF -> controlCenter.focusMode.label
                                else -> "Aura"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(AuraBlue.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = "Track art",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.width(118.dp)) {
                                Text(
                                    text = controlCenter.currentTrackTitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = controlCenter.currentTrackArtist,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .clickable(onClick = onTogglePlayPause),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (controlCenter.isPlayingMusic) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (controlCenter.isPlayingMusic) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniIslandAction(
                                icon = Icons.Rounded.Lock,
                                label = "Lock",
                                onClick = onOpenLockScreen
                            )
                            MiniIslandAction(
                                icon = Icons.Rounded.Tune,
                                label = "Controls",
                                onClick = onOpenControlCenter
                            )
                        }
                    }
                }
            }

            // Right: Signal + Wi-Fi + Battery (tap opens Control Center)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenControlCenter)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("status_control_trigger")
            ) {
                if (controlCenter.airplaneMode) {
                    Icon(
                        imageVector = Icons.Rounded.AirplanemodeActive,
                        contentDescription = "Airplane Mode",
                        tint = AuraAmber,
                        modifier = Modifier.size(15.dp)
                    )
                } else {
                    if (controlCenter.cellularConnected) {
                        Icon(
                            imageVector = Icons.Rounded.SignalCellularAlt,
                            contentDescription = "Cellular signal",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (controlCenter.wifiConnected) {
                        Icon(
                            imageVector = Icons.Rounded.Wifi,
                            contentDescription = "Wi-Fi connected",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Battery pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (telemetry.isCharging) AuraTeal.copy(alpha = 0.9f)
                            else Color.White.copy(alpha = 0.24f)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    if (telemetry.isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = "Charging",
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = "${telemetry.batteryLevel}%",
                        color = if (telemetry.isCharging) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniIslandAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
