package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ControlCenterState
import com.example.model.DeviceTelemetry
import com.example.model.FocusMode
import com.example.model.HomeWidget
import com.example.model.NotificationFilterGroup
import com.example.model.QuickNoteItem
import com.example.model.TimeOfDayPhase
import com.example.model.WidgetType
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraCoral
import com.example.ui.theme.AuraGold
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraTeal
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeWidgetCard(
    widget: HomeWidget,
    phase: TimeOfDayPhase,
    telemetry: DeviceTelemetry,
    quickNotes: List<QuickNoteItem>,
    photoWidgetUri: String?,
    currentHour: Int,
    currentDateText: String,
    controlCenter: ControlCenterState,
    silencedNotificationCount: Int,
    isEditMode: Boolean,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
    onToggleNote: (String) -> Unit,
    onAddNoteRequested: () -> Unit,
    onPickPhotoRequested: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onToggleDnd: () -> Unit,
    onCycleFocusMode: () -> Unit,
    onToggleFocusGroup: (String) -> Unit,
    onOpenDndPolicySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)

    Box(modifier = modifier.testTag("widget_${widget.type.name}")) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
                .clip(shape)
                .combinedClickable(
                    onClick = {
                        if (!isEditMode) {
                            when (widget.type) {
                                WidgetType.PHOTO_FRAME -> onPickPhotoRequested()
                                WidgetType.WEATHER_SUN -> onOpenWallpaperStudio()
                                WidgetType.FOCUS_MODE -> onToggleDnd()
                                else -> Unit
                            }
                        }
                    },
                    onLongClick = onLongClick
                )
        ) {
            when (widget.type) {
                WidgetType.WEATHER_SUN -> WeatherSunWidget(phase, telemetry, currentHour)
                WidgetType.CLOCK_WORLD -> ClockAnalogWidget(currentHour)
                WidgetType.BATTERY_RING -> BatteryRingWidget(telemetry)
                WidgetType.CALENDAR_TODAY -> CalendarTodayWidget(currentDateText)
                WidgetType.PHOTO_FRAME -> PhotoMemoryWidget(phase, photoWidgetUri)
                WidgetType.QUICK_NOTES -> QuickNotesWidget(
                    notes = quickNotes,
                    onToggleNote = onToggleNote,
                    onAddNoteRequested = onAddNoteRequested
                )
                WidgetType.FOCUS_MODE -> FocusModeWidget(
                    controlCenter = controlCenter,
                    silencedCount = silencedNotificationCount,
                    onToggleDnd = onToggleDnd,
                    onCycleFocusMode = onCycleFocusMode,
                    onToggleFocusGroup = onToggleFocusGroup,
                    onOpenDndPolicySettings = onOpenDndPolicySettings
                )
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xF0E2E8F0))
                    .clickable(onClick = onRemove)
                    .testTag("remove_widget_${widget.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Remove ${widget.type.title} widget",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun WeatherSunWidget(
    phase: TimeOfDayPhase,
    telemetry: DeviceTelemetry,
    currentHour: Int
) {
    val (bgTop, bgBottom, phaseIcon) = when (phase) {
        TimeOfDayPhase.DAWN -> Triple(Color(0xCC6D597A), Color(0xCCEAAC8B), Icons.Rounded.WbTwilight)
        TimeOfDayPhase.DAY -> Triple(Color(0xCC1E6EC8), Color(0xCC58B4F2), Icons.Rounded.WbSunny)
        TimeOfDayPhase.SUNSET -> Triple(Color(0xCC8A3B6A), Color(0xCCE67E53), Icons.Rounded.WbTwilight)
        TimeOfDayPhase.NIGHT -> Triple(Color(0xCC111936), Color(0xCC213A5C), Icons.Rounded.NightsStay)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "San Francisco",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${telemetry.weatherTempF}°",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 36.sp
                    )
                }
                Icon(
                    imageVector = phaseIcon,
                    contentDescription = telemetry.weatherCondition,
                    tint = AuraGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Sun arc progress across the day
            val progress = ((currentHour - 6).coerceIn(0, 14)) / 14f
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    quadraticTo(
                        size.width / 2f,
                        -size.height * 0.35f,
                        size.width,
                        size.height * 0.8f
                    )
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.3f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                val sunX = size.width * progress
                val t = progress
                val sunY = (1 - t) * (1 - t) * (size.height * 0.8f) +
                    2 * (1 - t) * t * (-size.height * 0.35f) +
                    t * t * (size.height * 0.8f)
                drawCircle(
                    color = AuraGold,
                    radius = 7f,
                    center = Offset(sunX, sunY)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = telemetry.weatherCondition,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "H:${telemetry.weatherHighF}° L:${telemetry.weatherLowF}°",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ClockAnalogWidget(currentHour: Int) {
    val cal = remember(currentHour) { Calendar.getInstance() }
    val minute = cal.get(Calendar.MINUTE)
    val hour12 = cal.get(Calendar.HOUR)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9101522))
            .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(116.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            drawCircle(
                color = Color(0xFF1E2638),
                radius = radius
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = radius,
                style = Stroke(width = 2.dp.toPx())
            )

            // Hour tick marks
            for (i in 0 until 12) {
                val angleRad = Math.toRadians((i * 30 - 90).toDouble())
                val start = Offset(
                    center.x + (radius * 0.80f * cos(angleRad)).toFloat(),
                    center.y + (radius * 0.80f * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    center.x + (radius * 0.92f * cos(angleRad)).toFloat(),
                    center.y + (radius * 0.92f * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (i % 3 == 0) Color.White else Color.White.copy(alpha = 0.45f),
                    start = start,
                    end = end,
                    strokeWidth = if (i % 3 == 0) 3f else 1.8f,
                    cap = StrokeCap.Round
                )
            }

            // Hour hand
            val hourAngle = Math.toRadians(((hour12 + minute / 60f) * 30f - 90f).toDouble())
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    center.x + (radius * 0.50f * cos(hourAngle)).toFloat(),
                    center.y + (radius * 0.50f * sin(hourAngle)).toFloat()
                ),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )

            // Minute hand
            val minuteAngle = Math.toRadians((minute * 6f - 90f).toDouble())
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    center.x + (radius * 0.72f * cos(minuteAngle)).toFloat(),
                    center.y + (radius * 0.72f * sin(minuteAngle)).toFloat()
                ),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )

            drawCircle(color = AuraAmber, radius = 4.5f, center = center)
        }

        Text(
            text = "CUPERTINO",
            color = AuraAmber,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        )
    }
}

@Composable
private fun BatteryRingWidget(telemetry: DeviceTelemetry) {
    val ringColor = when {
        telemetry.isCharging -> AuraTeal
        telemetry.batteryLevel <= 20 -> AuraCoral
        else -> AuraTeal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3161E2E))
            .border(0.8.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 7.dp.toPx()
                        drawArc(
                            color = Color.White.copy(alpha = 0.16f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke)
                        )
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (telemetry.batteryLevel / 100f),
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                            topLeft = Offset(stroke / 2f, stroke / 2f),
                            size = Size(size.width - stroke, size.height - stroke)
                        )
                    }
                    if (telemetry.isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = "Charging",
                            tint = AuraTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "${telemetry.batteryLevel}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Phone",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (telemetry.isCharging) "Charging" else "Healthy",
                        color = ringColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column {
                Text(
                    text = "Device Battery",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${telemetry.batteryLevel}% remaining",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

@Composable
private fun CalendarTodayWidget(currentDateText: String) {
    val parts = currentDateText.split(",")
    val dayOfWeek = parts.firstOrNull()?.uppercase() ?: "TODAY"
    val dateLabel = parts.getOrNull(1)?.trim() ?: "November 18"

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6FFFFFF))
            .border(0.8.dp, Color.White, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(0.42f)) {
            Text(
                text = dayOfWeek,
                color = AuraCoral,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Text(
                text = dateLabel.split(" ").lastOrNull() ?: "18",
                color = Color(0xFF0F172A),
                fontSize = 40.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 44.sp
            )
            Text(
                text = dateLabel.split(" ").firstOrNull() ?: "Nov",
                color = Color(0xFF475569),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier.weight(0.58f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalendarEventRow("Product Sync", "10:30 AM", AuraBlue)
            CalendarEventRow("Design Critique", "2:00 PM", AuraIndigo)
            CalendarEventRow("Evening Run", "6:00 PM", AuraTeal)
        }
    }
}

@Composable
private fun CalendarEventRow(title: String, time: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = time,
                color = Color(0xFF475569),
                fontSize = 10.5.sp
            )
        }
    }
}

@Composable
private fun PhotoMemoryWidget(
    phase: TimeOfDayPhase,
    photoWidgetUri: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
    ) {
        if (!photoWidgetUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(photoWidgetUri),
                contentDescription = "Selected memory photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = phase.wallpaperRes),
                contentDescription = "Featured scenic memory",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.56f))
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = "Pick photo",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (photoWidgetUri != null) "Memory" else "Tap to Pick",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuickNotesWidget(
    notes: List<QuickNoteItem>,
    onToggleNote: (String) -> Unit,
    onAddNoteRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC182032))
            .border(0.8.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reminders",
                color = AuraAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AuraAmber.copy(alpha = 0.22f))
                    .clickable(onClick = onAddNoteRequested)
                    .testTag("widget_add_note_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add reminder",
                    tint = AuraAmber,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            notes.take(3).forEach { note ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleNote(note.id) }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (note.isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (note.isDone) "Completed" else "Incomplete",
                        tint = if (note.isDone) AuraAmber else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.text,
                        color = if (note.isDone) Color.White.copy(alpha = 0.5f) else Color.White,
                        fontSize = 12.5.sp,
                        textDecoration = if (note.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusModeWidget(
    controlCenter: ControlCenterState,
    silencedCount: Int,
    onToggleDnd: () -> Unit,
    onCycleFocusMode: () -> Unit,
    onToggleFocusGroup: (String) -> Unit,
    onOpenDndPolicySettings: () -> Unit
) {
    val isActive = controlCenter.focusMode != FocusMode.OFF
    val accent = if (isActive) controlCenter.focusMode.tint else AuraPurple
    val bgBrush = if (isActive) {
        Brush.linearGradient(
            listOf(accent.copy(alpha = 0.72f), Color(0xD914182B))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xCC161C2E), Color(0xCC1C2438))
        )
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .border(0.8.dp, Color.White.copy(alpha = if (isActive) 0.36f else 0.22f), RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color.White else accent.copy(alpha = 0.28f))
                        .clickable(onClick = onToggleDnd)
                        .testTag("focus_widget_dnd_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Rounded.DoNotDisturbOn else Icons.Rounded.NightsStay,
                        contentDescription = "Toggle Do Not Disturb",
                        tint = if (isActive) accent else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isActive) controlCenter.focusMode.label else "Do Not Disturb",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isActive) {
                            if (silencedCount > 0) "$silencedCount alerts silenced" else "Filter active"
                        } else {
                            "Off • All alerts visible"
                        },
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onCycleFocusMode)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("focus_widget_cycle_mode")
                ) {
                    Text(
                        text = if (isActive) "Mode ↻" else "Turn On",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (!controlCenter.isDndPolicyGranted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onOpenDndPolicySettings)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("focus_widget_os_dnd_link")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Grant System DND Access",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Notification Group Filters
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isActive) "Allowed Notifications" else "Tap to filter apps with DND",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NotificationFilterGroup.entries.forEach { group ->
                    val allowed = group.groupKey in controlCenter.focusAllowedGroups
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (allowed && isActive) Color.White.copy(alpha = 0.92f)
                                else if (allowed) Color.White.copy(alpha = 0.16f)
                                else Color.Black.copy(alpha = 0.28f)
                            )
                            .border(
                                width = 0.75.dp,
                                color = if (allowed) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleFocusGroup(group.groupKey) }
                            .padding(vertical = 7.dp)
                            .testTag("focus_filter_${group.groupKey}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (allowed) group.label else "${group.label} ✕",
                            color = if (allowed && isActive) Color(0xFF0F172A) else Color.White.copy(alpha = if (allowed) 0.95f else 0.55f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
