package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceTelemetry
import com.example.model.FocusMode
import com.example.model.LockClockTint
import com.example.model.LockFontStyle
import com.example.model.LockNotification
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraTeal
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockScreenOverlay(
    visible: Boolean,
    timeText: String,
    dateText: String,
    telemetry: DeviceTelemetry,
    fontStyle: LockFontStyle,
    clockTint: LockClockTint,
    focusMode: FocusMode,
    notifications: List<LockNotification>,
    silencedCount: Int,
    showingSilenced: Boolean,
    expandedGroups: Set<String>,
    isCustomizerOpen: Boolean,
    showLockOnStart: Boolean,
    flashlightOn: Boolean,
    onUnlock: () -> Unit,
    onToggleCustomizer: (Boolean) -> Unit,
    onSelectFontStyle: (LockFontStyle) -> Unit,
    onSelectClockTint: (LockClockTint) -> Unit,
    onToggleShowOnStart: (Boolean) -> Unit,
    onToggleShowSilenced: () -> Unit,
    onToggleGroupExpanded: (String) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearGroup: (String) -> Unit,
    onAddSampleNotification: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onOpenCamera: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it / 6 },
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    ) {
        var dragOffsetY by remember { mutableFloatStateOf(0f) }
        val animatedDragY by animateFloatAsState(
            targetValue = dragOffsetY,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
            label = "lock_drag_y"
        )
        val unlockProgress = ((-animatedDragY) / 260f).coerceIn(0f, 1f)

        val groupedNotifications = remember(notifications) {
            notifications.groupBy { it.groupKey }.entries.toList()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, animatedDragY.roundToInt()) }
                .graphicsLayer { alpha = 1f - unlockProgress * 0.45f }
                .pointerInput(isCustomizerOpen) {
                    if (!isCustomizerOpen) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffsetY < -140f) {
                                    dragOffsetY = 0f
                                    onUnlock()
                                } else {
                                    dragOffsetY = 0f
                                }
                            },
                            onDragCancel = { dragOffsetY = 0f },
                            onVerticalDrag = { _, dragAmount ->
                                val next = dragOffsetY + dragAmount
                                dragOffsetY = next.coerceAtMost(0f)
                            }
                        )
                    }
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("lock_screen_overlay")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Top Lock + Customize pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(84.dp))
                    Icon(
                        imageVector = if (unlockProgress > 0.35f) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        contentDescription = "Lock status",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.20f))
                            .border(0.75.dp, Color.White.copy(alpha = 0.34f), RoundedCornerShape(18.dp))
                            .clickable { onToggleCustomizer(!isCustomizerOpen) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("lock_customize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Style,
                            contentDescription = "Customize Lock Screen",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCustomizerOpen) "Done" else "Style",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date + Stylized Clock
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { onToggleCustomizer(true) }
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dateText.ifBlank { "Wednesday, November 18" },
                        color = clockTint.color.copy(alpha = 0.92f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = timeText.ifBlank { "9:41" },
                        fontFamily = fontStyle.fontFamily,
                        color = clockTint.color,
                        fontSize = if (fontStyle == LockFontStyle.SCRIPT_SIGNATURE) 76.sp else 84.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = fontStyle.letterSpacingSp.sp,
                        lineHeight = 88.sp,
                        modifier = Modifier.testTag("lock_screen_clock")
                    )

                    // Inline Lock Screen Widgets Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WbSunny,
                                contentDescription = "Weather",
                                tint = clockTint.color,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${telemetry.weatherTempF}° ${telemetry.weatherCondition}",
                                color = clockTint.color.copy(alpha = 0.9f),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text("•", color = clockTint.color.copy(alpha = 0.5f), fontSize = 12.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (telemetry.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                                contentDescription = "Battery",
                                tint = if (telemetry.isCharging) AuraTeal else clockTint.color,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${telemetry.batteryLevel}%",
                                color = clockTint.color.copy(alpha = 0.9f),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customizer Drawer OR Notification Stacks
                if (isCustomizerOpen) {
                    LockScreenCustomizerPanel(
                        selectedFont = fontStyle,
                        selectedTint = clockTint,
                        showLockOnStart = showLockOnStart,
                        onSelectFont = onSelectFontStyle,
                        onSelectTint = onSelectClockTint,
                        onToggleShowOnStart = onToggleShowOnStart,
                        onAddSampleNotification = onAddSampleNotification,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("lock_notification_list")
                    ) {
                        if (focusMode != FocusMode.OFF && silencedCount > 0) {
                            item(key = "focus_silenced_summary") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(focusMode.tint.copy(alpha = 0.28f))
                                        .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                                        .clickable(onClick = onToggleShowSilenced)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .testTag("lock_focus_silenced_banner")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DoNotDisturbOn,
                                            contentDescription = focusMode.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "${focusMode.label} Active",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "$silencedCount notifications silenced",
                                                color = Color.White.copy(alpha = 0.78f),
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (showingSilenced) "Hide" else "Show",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        items(groupedNotifications, key = { it.key }) { (groupKey, groupItems) ->
                            val isExpanded = groupKey in expandedGroups || groupItems.size == 1
                            NotificationStackGroup(
                                groupKey = groupKey,
                                items = groupItems,
                                isExpanded = isExpanded,
                                onToggleExpand = { onToggleGroupExpanded(groupKey) },
                                onDismissItem = onDismissNotification,
                                onClearGroup = { onClearGroup(groupKey) }
                            )
                        }
                    }
                }
            }

            // Bottom Quick Actions + Swipe Up to Unlock Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (flashlightOn) Color.White else Color.Black.copy(alpha = 0.36f)
                            )
                            .border(0.8.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable(onClick = onToggleFlashlight)
                            .testTag("lock_flashlight_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (flashlightOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                            contentDescription = "Toggle Flashlight",
                            tint = if (flashlightOn) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    Text(
                        text = "Swipe up or tap to unlock",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onUnlock)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("lock_unlock_button")
                    )

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.36f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable(onClick = onOpenCamera)
                            .testTag("lock_camera_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = "Open Camera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .width(132.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable(onClick = onUnlock)
                )
            }
        }
    }
}

@Composable
private fun NotificationStackGroup(
    groupKey: String,
    items: List<LockNotification>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDismissItem: (String) -> Unit,
    onClearGroup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            .testTag("notification_stack_$groupKey")
    ) {
        if (!isExpanded && items.size > 1) {
            val top = items.first()
            // Realistic stacked layers underneath top notification
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
            ) {
                if (items.size >= 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 18.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(0.6.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(top = 9.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .border(0.6.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(22.dp))
                )
                NotificationCardContent(
                    notification = top,
                    extraCount = items.size - 1
                )
            }
        } else {
            if (items.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = items.first().appName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .clickable(onClick = onToggleExpand)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExpandLess,
                                contentDescription = "Collapse stack",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Stack", color = Color.White, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .clickable(onClick = onClearGroup)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Clear", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    DismissibleNotificationCard(
                        notification = item,
                        onDismiss = { onDismissItem(item.id) },
                        onClick = if (items.size > 1) onToggleExpand else ({})
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleNotificationCard(
    notification: LockNotification,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss notification",
                    tint = Color.White
                )
            }
        }
    ) {
        Box(modifier = Modifier.clickable(onClick = onClick)) {
            NotificationCardContent(notification = notification, extraCount = 0)
        }
    }
}

@Composable
private fun NotificationCardContent(
    notification: LockNotification,
    extraCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x99111827))
            .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(notification.accentColor.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForKey(notification.iconKey),
                contentDescription = notification.appName,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = notification.timeLabel,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = notification.body,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 12.5.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (extraCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+$extraCount more ${notification.appName} notifications • Tap to expand",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LockScreenCustomizerPanel(
    selectedFont: LockFontStyle,
    selectedTint: LockClockTint,
    showLockOnStart: Boolean,
    onSelectFont: (LockFontStyle) -> Unit,
    onSelectTint: (LockClockTint) -> Unit,
    onToggleShowOnStart: (Boolean) -> Unit,
    onAddSampleNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 86.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xCC0F172A))
            .border(0.9.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
            .padding(16.dp)
            .testTag("lock_customizer_panel")
    ) {
        Text(
            text = "Clock Font Style",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(LockFontStyle.entries, key = { it.name }) { style ->
                val active = style == selectedFont
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (active) AuraBlue.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f))
                        .border(
                            width = if (active) 1.5.dp else 0.75.dp,
                            color = if (active) AuraBlue else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectFont(style) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("font_option_${style.name}")
                ) {
                    Text(
                        text = "9:41",
                        fontFamily = style.fontFamily,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = style.displayName,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Text(
            text = "Clock Tint",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LockClockTint.entries.forEach { tint ->
                val active = tint == selectedTint
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint.color)
                        .border(
                            width = if (active) 2.5.dp else 1.dp,
                            color = if (active) AuraBlue else Color.White.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                        .clickable { onSelectTint(tint) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Show Lock Screen on Launch",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Open directly to Lock Screen when starting",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = showLockOnStart,
                onCheckedChange = onToggleShowOnStart
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AuraBlue)
                .clickable(onClick = onAddSampleNotification)
                .testTag("add_sample_notification_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.AddAlert,
                contentDescription = "Add alert",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Send Notification to Stack",
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
