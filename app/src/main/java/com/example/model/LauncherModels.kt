package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraCoral
import com.example.ui.theme.AuraGold
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraTeal
import com.example.ui.theme.ClockCoral
import com.example.ui.theme.ClockIceBlue
import com.example.ui.theme.ClockMint
import com.example.ui.theme.ClockObsidian
import com.example.ui.theme.ClockWarmGold
import com.example.ui.theme.ClockWhite
import com.example.ui.theme.DawnBottom
import com.example.ui.theme.DawnTop
import com.example.ui.theme.DayBottom
import com.example.ui.theme.DayTop
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NightBottom
import com.example.ui.theme.NightTop
import com.example.ui.theme.OutfitFamily
import com.example.ui.theme.PacificoFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.SunsetBottom
import com.example.ui.theme.SunsetTop

enum class AppCategory(val title: String) {
    SUGGESTIONS("Suggestions"),
    SOCIAL("Social"),
    PRODUCTIVITY("Productivity"),
    MEDIA("Entertainment"),
    UTILITIES("Utilities"),
    CREATIVITY("Creativity")
}

enum class BuiltInAction {
    NONE,
    OPEN_WALLPAPER_STUDIO,
    OPEN_DOCK_SETTINGS,
    OPEN_LOCK_CUSTOMIZER,
    OPEN_WIDGET_PICKER,
    TOGGLE_CONTROL_CENTER,
    LOCK_DEVICE_SCREEN,
    DIAL_PHONE,
    OPEN_MESSAGES,
    OPEN_CAMERA,
    OPEN_BROWSER,
    OPEN_CALCULATOR,
    OPEN_CALENDAR,
    OPEN_SETTINGS,
    OPEN_PHOTOS_PICKER,
    SET_DEFAULT_LAUNCHER
}

data class LauncherApp(
    val id: String,
    val label: String,
    val packageName: String? = null,
    val category: AppCategory = AppCategory.UTILITIES,
    val iconKey: String = "apps",
    val accentColor: Color = AuraBlue,
    val secondaryColor: Color = AuraIndigo,
    val badgeCount: Int = 0,
    val builtInAction: BuiltInAction = BuiltInAction.NONE
)

data class HomeFolder(
    val id: String,
    val title: String,
    val appIds: List<String>,
    val pageIndex: Int = 0
)

enum class WidgetType(
    val title: String,
    val subtitle: String,
    val defaultSize: WidgetSize
) {
    WEATHER_SUN("Weather", "Live sky & sun arc", WidgetSize.SMALL_2X2),
    CLOCK_WORLD("Clock", "Analog timepiece", WidgetSize.SMALL_2X2),
    BATTERY_RING("Batteries", "Device power rings", WidgetSize.SMALL_2X2),
    CALENDAR_TODAY("Calendar", "Today's schedule", WidgetSize.MEDIUM_4X2),
    PHOTO_FRAME("Photos", "Memory showcase", WidgetSize.SMALL_2X2),
    QUICK_NOTES("Reminders", "Quick checklist", WidgetSize.MEDIUM_4X2),
    FOCUS_MODE("Focus Mode", "DND & notification filter", WidgetSize.MEDIUM_4X2)
}

enum class WidgetSize {
    SMALL_2X2,
    MEDIUM_4X2
}

data class HomeWidget(
    val id: String,
    val type: WidgetType,
    val size: WidgetSize = type.defaultSize,
    val pageIndex: Int = 0
)

data class QuickNoteItem(
    val id: String,
    val text: String,
    val isDone: Boolean
)

enum class TimeOfDayPhase(
    val label: String,
    val timeWindow: String,
    @DrawableRes val wallpaperRes: Int,
    val topColor: Color,
    val bottomColor: Color,
    val isDarkBackdrop: Boolean
) {
    DAWN(
        label = "Dawn Mist",
        timeWindow = "5:00 AM – 8:59 AM",
        wallpaperRes = R.drawable.img_wall_dawn,
        topColor = DawnTop,
        bottomColor = DawnBottom,
        isDarkBackdrop = false
    ),
    DAY(
        label = "Daylight Sky",
        timeWindow = "9:00 AM – 4:59 PM",
        wallpaperRes = R.drawable.img_wall_day,
        topColor = DayTop,
        bottomColor = DayBottom,
        isDarkBackdrop = false
    ),
    SUNSET(
        label = "Golden Sunset",
        timeWindow = "5:00 PM – 7:59 PM",
        wallpaperRes = R.drawable.img_wall_sunset,
        topColor = SunsetTop,
        bottomColor = SunsetBottom,
        isDarkBackdrop = true
    ),
    NIGHT(
        label = "Midnight Aurora",
        timeWindow = "8:00 PM – 4:59 AM",
        wallpaperRes = R.drawable.img_wall_night,
        topColor = NightTop,
        bottomColor = NightBottom,
        isDarkBackdrop = true
    );

    companion object {
        fun fromHour(hour: Int): TimeOfDayPhase = when (hour) {
            in 5..8 -> DAWN
            in 9..16 -> DAY
            in 17..19 -> SUNSET
            else -> NIGHT
        }
    }
}

enum class WallpaperMode(val label: String) {
    AUTO_DYNAMIC("Auto (Time of Day)"),
    MANUAL_DAWN("Dawn Mist"),
    MANUAL_DAY("Daylight Sky"),
    MANUAL_SUNSET("Golden Sunset"),
    MANUAL_NIGHT("Midnight Aurora");

    fun resolvePhase(currentHour: Int): TimeOfDayPhase = when (this) {
        AUTO_DYNAMIC -> TimeOfDayPhase.fromHour(currentHour)
        MANUAL_DAWN -> TimeOfDayPhase.DAWN
        MANUAL_DAY -> TimeOfDayPhase.DAY
        MANUAL_SUNSET -> TimeOfDayPhase.SUNSET
        MANUAL_NIGHT -> TimeOfDayPhase.NIGHT
    }
}

enum class DockStyle(val label: String) {
    FROSTED_GLASS("Frosted Glass"),
    CLEAR_TINT("Crystal Clear"),
    DARK_OBSIDIAN("Midnight Smoke")
}

enum class LockFontStyle(
    val displayName: String,
    val fontFamily: FontFamily,
    val letterSpacingSp: Float
) {
    MODERN_SANS("Modern", PlusJakartaSansFamily, -2.5f),
    CLASSIC_SERIF("Editorial", PlayfairDisplayFamily, -1.5f),
    ROUNDED_EDITORIAL("Rounded", OutfitFamily, -2.0f),
    GEOMETRIC_BOLD("Geometric", SpaceGroteskFamily, -3.0f),
    MONO_TECH("Mono", JetBrainsMonoFamily, -2.0f),
    SCRIPT_SIGNATURE("Signature", PacificoFamily, 0.0f)
}

enum class LockClockTint(val label: String, val color: Color) {
    GLASS_WHITE("Frosted White", ClockWhite),
    WARM_GOLD("Champagne Gold", ClockWarmGold),
    CORAL_SUNSET("Sunset Coral", ClockCoral),
    ICE_BLUE("Glacier Blue", ClockIceBlue),
    MINT_AURORA("Aurora Mint", ClockMint),
    OBSIDIAN("Deep Ink", ClockObsidian)
}

enum class NotificationFilterGroup(val groupKey: String, val label: String) {
    MESSAGES("messages", "Messages"),
    CALENDAR("calendar", "Calendar"),
    AURA("aura", "System")
}

enum class FocusMode(val label: String, val tint: Color) {
    OFF("Focus", AuraIndigo),
    DO_NOT_DISTURB("Do Not Disturb", AuraPurple),
    WORK("Work", AuraBlue),
    SLEEP("Sleep", AuraTeal),
    PERSONAL("Personal", AuraAmber);

    fun defaultAllowedGroups(): Set<String> = when (this) {
        OFF -> setOf("messages", "calendar", "aura")
        DO_NOT_DISTURB -> emptySet()
        WORK -> setOf("calendar", "aura")
        SLEEP -> emptySet()
        PERSONAL -> setOf("messages", "aura")
    }
}

data class LockNotification(
    val id: String,
    val groupKey: String,
    val appName: String,
    val title: String,
    val body: String,
    val timeLabel: String,
    val accentColor: Color,
    val iconKey: String
)

data class ControlCenterState(
    val wifiConnected: Boolean = true,
    val wifiLabel: String = "Aura-5G",
    val bluetoothEnabled: Boolean = true,
    val airplaneMode: Boolean = false,
    val cellularConnected: Boolean = true,
    val orientationLocked: Boolean = false,
    val focusMode: FocusMode = FocusMode.OFF,
    val focusAllowedGroups: Set<String> = setOf("messages", "calendar", "aura"),
    val isDndPolicyGranted: Boolean = false,
    val systemDndActive: Boolean = false,
    val flashlightOn: Boolean = false,
    val flashlightAvailable: Boolean = true,
    val brightness: Float = 0.72f,
    val mediaVolume: Float = 0.6f,
    val isPlayingMusic: Boolean = false,
    val currentTrackTitle: String = "Midnight Cityscapes",
    val currentTrackArtist: String = "Aura Ambient"
)

data class DeviceTelemetry(
    val batteryLevel: Int = 86,
    val isCharging: Boolean = false,
    val weatherTempF: Int = 68,
    val weatherCondition: String = "Clear",
    val weatherHighF: Int = 73,
    val weatherLowF: Int = 56
)

enum class ActiveSheet {
    NONE,
    WIDGET_PICKER,
    DOCK_CUSTOMIZER,
    WALLPAPER_STUDIO,
    LOCK_CUSTOMIZER,
    CREATE_FOLDER
}
