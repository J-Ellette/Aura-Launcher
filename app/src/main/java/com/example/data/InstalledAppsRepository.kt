package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.example.model.AppCategory
import com.example.model.BuiltInAction
import com.example.model.LauncherApp
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraCoral
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraTeal
import java.util.concurrent.ConcurrentHashMap

class InstalledAppsRepository(private val context: Context) {

    private val iconCache = ConcurrentHashMap<String, Drawable>()

    fun getDrawableForApp(appId: String): Drawable? = iconCache[appId]

    fun loadAllApps(): List<LauncherApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val myPackage = context.packageName
        val installedList = resolved
            .asSequence()
            .filter { it.activityInfo.packageName != myPackage }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val label = info.loadLabel(pm).toString().ifBlank { pkg }
                val id = "pkg:$pkg"
                runCatching {
                    iconCache[id] = info.loadIcon(pm)
                }
                val category = classifyApp(pkg, info.activityInfo.applicationInfo)
                LauncherApp(
                    id = id,
                    label = label,
                    packageName = pkg,
                    category = category,
                    iconKey = inferIconKey(pkg, label),
                    accentColor = categoryAccent(category),
                    secondaryColor = categorySecondary(category)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()

        // Built-in Aura Launcher studio utilities & standard Android zero-permission intent shortcuts
        // so the home screen, dock, and App Library always feel complete even on sparse emulators.
        val curatedShortcuts = buildCuratedApps(installedList)
        return (curatedShortcuts + installedList).distinctBy { it.id }
    }

    fun launchApp(
        app: LauncherApp,
        onBuiltInAction: (BuiltInAction) -> Unit
    ) {
        if (app.builtInAction != BuiltInAction.NONE) {
            when (app.builtInAction) {
                BuiltInAction.DIAL_PHONE -> launchSafeIntent(
                    Intent(Intent.ACTION_DIAL),
                    "Dialer"
                )
                BuiltInAction.OPEN_MESSAGES -> launchCategoryOrAction(
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING),
                    Intent(Intent.ACTION_VIEW).apply { type = "vnd.android-dir/mms-sms" },
                    "Messages"
                )
                BuiltInAction.OPEN_CAMERA -> launchSafeIntent(
                    Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                    "Camera"
                )
                BuiltInAction.OPEN_BROWSER -> launchCategoryOrAction(
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER),
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")),
                    "Browser"
                )
                BuiltInAction.OPEN_CALCULATOR -> launchSafeIntent(
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALCULATOR),
                    "Calculator"
                )
                BuiltInAction.OPEN_CALENDAR -> launchSafeIntent(
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR),
                    "Calendar"
                )
                BuiltInAction.OPEN_SETTINGS -> launchSafeIntent(
                    Intent(Settings.ACTION_SETTINGS),
                    "Settings"
                )
                else -> onBuiltInAction(app.builtInAction)
            }
            return
        }

        val pkg = app.packageName ?: return
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(launchIntent) }
                .onFailure {
                    Toast.makeText(context, "Unable to open ${app.label}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "${app.label} is not launchable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCategoryOrAction(primary: Intent, fallback: Intent, label: String) {
        primary.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val ok = runCatching { context.startActivity(primary) }.isSuccess
        if (!ok) {
            launchSafeIntent(fallback, label)
        }
    }

    private fun launchSafeIntent(intent: Intent, label: String) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, "$label app not found on device", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buildCuratedApps(installed: List<LauncherApp>): List<LauncherApp> {
        val hasLabel = { keyword: String ->
            installed.any { it.label.contains(keyword, ignoreCase = true) }
        }
        val list = mutableListOf(
            LauncherApp(
                id = "aura:wallpapers",
                label = "Wallpapers",
                category = AppCategory.CREATIVITY,
                iconKey = "wallpaper",
                accentColor = Color(0xFFFF6B8B),
                secondaryColor = Color(0xFF8B5CF6),
                builtInAction = BuiltInAction.OPEN_WALLPAPER_STUDIO
            ),
            LauncherApp(
                id = "aura:lock_style",
                label = "Lock Style",
                category = AppCategory.CREATIVITY,
                iconKey = "lock_style",
                accentColor = Color(0xFF38BDF8),
                secondaryColor = Color(0xFF6366F1),
                builtInAction = BuiltInAction.OPEN_LOCK_CUSTOMIZER
            ),
            LauncherApp(
                id = "aura:dock_setup",
                label = "Dock Studio",
                category = AppCategory.UTILITIES,
                iconKey = "dock",
                accentColor = Color(0xFF34D399),
                secondaryColor = Color(0xFF059669),
                builtInAction = BuiltInAction.OPEN_DOCK_SETTINGS
            ),
            LauncherApp(
                id = "aura:widgets",
                label = "Widgets",
                category = AppCategory.PRODUCTIVITY,
                iconKey = "widgets",
                accentColor = Color(0xFFF59E0B),
                secondaryColor = Color(0xFFEA580C),
                builtInAction = BuiltInAction.OPEN_WIDGET_PICKER
            ),
            LauncherApp(
                id = "aura:control_center",
                label = "Controls",
                category = AppCategory.UTILITIES,
                iconKey = "controls",
                accentColor = Color(0xFF64748B),
                secondaryColor = Color(0xFF334155),
                builtInAction = BuiltInAction.TOGGLE_CONTROL_CENTER
            ),
            LauncherApp(
                id = "aura:lock_screen",
                label = "Lock Screen",
                category = AppCategory.UTILITIES,
                iconKey = "lock",
                accentColor = Color(0xFF1E293B),
                secondaryColor = Color(0xFF475569),
                builtInAction = BuiltInAction.LOCK_DEVICE_SCREEN
            ),
            LauncherApp(
                id = "aura:photos",
                label = "Photos",
                category = AppCategory.CREATIVITY,
                iconKey = "photos",
                accentColor = Color(0xFFEC4899),
                secondaryColor = Color(0xFFF97316),
                builtInAction = BuiltInAction.OPEN_PHOTOS_PICKER
            ),
            LauncherApp(
                id = "aura:default_home",
                label = "Default Home",
                category = AppCategory.UTILITIES,
                iconKey = "home",
                accentColor = Color(0xFF0A84FF),
                secondaryColor = Color(0xFF5E5CE6),
                builtInAction = BuiltInAction.SET_DEFAULT_LAUNCHER
            )
        )

        if (!hasLabel("phone")) {
            list += LauncherApp(
                id = "sys:phone",
                label = "Phone",
                category = AppCategory.SOCIAL,
                iconKey = "phone",
                accentColor = Color(0xFF30D158),
                secondaryColor = Color(0xFF15803D),
                badgeCount = 1,
                builtInAction = BuiltInAction.DIAL_PHONE
            )
        }
        if (!hasLabel("message")) {
            list += LauncherApp(
                id = "sys:messages",
                label = "Messages",
                category = AppCategory.SOCIAL,
                iconKey = "messages",
                accentColor = Color(0xFF34C759),
                secondaryColor = Color(0xFF0EA5E9),
                badgeCount = 3,
                builtInAction = BuiltInAction.OPEN_MESSAGES
            )
        }
        if (!hasLabel("chrome") && !hasLabel("browser") && !hasLabel("safari")) {
            list += LauncherApp(
                id = "sys:safari",
                label = "Safari",
                category = AppCategory.SUGGESTIONS,
                iconKey = "safari",
                accentColor = Color(0xFF0A84FF),
                secondaryColor = Color(0xFF38BDF8),
                builtInAction = BuiltInAction.OPEN_BROWSER
            )
        }
        if (!hasLabel("camera")) {
            list += LauncherApp(
                id = "sys:camera",
                label = "Camera",
                category = AppCategory.CREATIVITY,
                iconKey = "camera",
                accentColor = Color(0xFF475569),
                secondaryColor = Color(0xFF1E293B),
                builtInAction = BuiltInAction.OPEN_CAMERA
            )
        }
        if (!hasLabel("calendar")) {
            list += LauncherApp(
                id = "sys:calendar",
                label = "Calendar",
                category = AppCategory.PRODUCTIVITY,
                iconKey = "calendar",
                accentColor = Color(0xFFFF453A),
                secondaryColor = Color(0xFFDC2626),
                builtInAction = BuiltInAction.OPEN_CALENDAR
            )
        }
        if (!hasLabel("calculator")) {
            list += LauncherApp(
                id = "sys:calculator",
                label = "Calculator",
                category = AppCategory.UTILITIES,
                iconKey = "calculator",
                accentColor = Color(0xFFFF9F0A),
                secondaryColor = Color(0xFF1C1C1E),
                builtInAction = BuiltInAction.OPEN_CALCULATOR
            )
        }
        if (!hasLabel("settings")) {
            list += LauncherApp(
                id = "sys:settings",
                label = "Settings",
                category = AppCategory.UTILITIES,
                iconKey = "settings",
                accentColor = Color(0xFF6366F1),
                secondaryColor = Color(0xFF334155),
                builtInAction = BuiltInAction.OPEN_SETTINGS
            )
        }
        return list
    }

    private fun classifyApp(pkg: String, appInfo: ApplicationInfo): AppCategory {
        val lower = pkg.lowercase()
        if (lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("instagram") ||
            lower.contains("message") || lower.contains("dialer") || lower.contains("contacts") ||
            lower.contains("discord") || lower.contains("slack")
        ) {
            return AppCategory.SOCIAL
        }
        if (lower.contains("music") || lower.contains("spotify") || lower.contains("youtube") ||
            lower.contains("netflix") || lower.contains("video") || lower.contains("podcast")
        ) {
            return AppCategory.MEDIA
        }
        if (lower.contains("camera") || lower.contains("photo") || lower.contains("gallery") ||
            lower.contains("draw") || lower.contains("canvas")
        ) {
            return AppCategory.CREATIVITY
        }
        if (lower.contains("docs") || lower.contains("notes") || lower.contains("calendar") ||
            lower.contains("mail") || lower.contains("gmail") || lower.contains("drive") ||
            lower.contains("keep")
        ) {
            return AppCategory.PRODUCTIVITY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_GAME -> AppCategory.MEDIA
                ApplicationInfo.CATEGORY_IMAGE -> AppCategory.CREATIVITY
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
                else -> AppCategory.UTILITIES
            }
        }
        return AppCategory.UTILITIES
    }

    private fun inferIconKey(pkg: String, label: String): String {
        val key = "$pkg $label".lowercase()
        return when {
            key.contains("phone") || key.contains("dialer") -> "phone"
            key.contains("message") || key.contains("sms") -> "messages"
            key.contains("chrome") || key.contains("browser") -> "safari"
            key.contains("camera") -> "camera"
            key.contains("calendar") -> "calendar"
            key.contains("music") || key.contains("spotify") -> "music"
            key.contains("photo") || key.contains("gallery") -> "photos"
            key.contains("settings") -> "settings"
            key.contains("mail") -> "mail"
            key.contains("map") -> "maps"
            key.contains("clock") -> "clock"
            else -> "apps"
        }
    }

    private fun categoryAccent(category: AppCategory): Color = when (category) {
        AppCategory.SUGGESTIONS -> AuraBlue
        AppCategory.SOCIAL -> AuraTeal
        AppCategory.PRODUCTIVITY -> AuraAmber
        AppCategory.MEDIA -> AuraCoral
        AppCategory.UTILITIES -> AuraIndigo
        AppCategory.CREATIVITY -> AuraPurple
    }

    private fun categorySecondary(category: AppCategory): Color = when (category) {
        AppCategory.SUGGESTIONS -> AuraIndigo
        AppCategory.SOCIAL -> AuraBlue
        AppCategory.PRODUCTIVITY -> AuraCoral
        AppCategory.MEDIA -> AuraPurple
        AppCategory.UTILITIES -> AuraBlue
        AppCategory.CREATIVITY -> AuraCoral
    }
}
