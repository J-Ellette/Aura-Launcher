package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DockStyle
import com.example.model.FocusMode
import com.example.model.HomeFolder
import com.example.model.HomeWidget
import com.example.model.LockClockTint
import com.example.model.LockFontStyle
import com.example.model.QuickNoteItem
import com.example.model.WallpaperMode
import com.example.model.WidgetSize
import com.example.model.WidgetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.auraDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_launcher_prefs")

data class LauncherPrefsSnapshot(
    val dockAppIds: List<String>?,
    val dockStyle: DockStyle,
    val homeAppIds: List<String>?,
    val homeFolders: List<HomeFolder>?,
    val homeWidgets: List<HomeWidget>?,
    val wallpaperMode: WallpaperMode,
    val lockFontStyle: LockFontStyle,
    val lockClockTint: LockClockTint,
    val showLockScreenOnStart: Boolean,
    val photoWidgetUri: String?,
    val quickNotes: List<QuickNoteItem>?,
    val focusMode: FocusMode,
    val focusAllowedGroups: Set<String>?,
    val appLaunchCounts: Map<String, Int>,
    val suppressDefaultLauncherPrompt: Boolean
)

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val DOCK_APP_IDS = stringPreferencesKey("dock_app_ids")
        val DOCK_STYLE = stringPreferencesKey("dock_style")
        val HOME_APP_IDS = stringPreferencesKey("home_app_ids")
        val HOME_FOLDERS = stringPreferencesKey("home_folders")
        val HOME_WIDGETS = stringPreferencesKey("home_widgets")
        val WALLPAPER_MODE = stringPreferencesKey("wallpaper_mode")
        val LOCK_FONT_STYLE = stringPreferencesKey("lock_font_style")
        val LOCK_CLOCK_TINT = stringPreferencesKey("lock_clock_tint")
        val SHOW_LOCK_ON_START = booleanPreferencesKey("show_lock_on_start")
        val PHOTO_WIDGET_URI = stringPreferencesKey("photo_widget_uri")
        val QUICK_NOTES = stringPreferencesKey("quick_notes")
        val FOCUS_MODE = stringPreferencesKey("focus_mode")
        val FOCUS_ALLOWED_GROUPS = stringPreferencesKey("focus_allowed_groups")
        val APP_LAUNCH_COUNTS = stringPreferencesKey("app_launch_counts")
        val SUPPRESS_DEFAULT_LAUNCHER_PROMPT = booleanPreferencesKey("suppress_default_launcher_prompt")
    }

    val prefsFlow: Flow<LauncherPrefsSnapshot> = context.auraDataStore.data.map { prefs ->
        LauncherPrefsSnapshot(
            dockAppIds = prefs[Keys.DOCK_APP_IDS]?.let(::decodeStringList),
            dockStyle = enumOrDefault(prefs[Keys.DOCK_STYLE], DockStyle.FROSTED_GLASS),
            homeAppIds = prefs[Keys.HOME_APP_IDS]?.let(::decodeStringList),
            homeFolders = prefs[Keys.HOME_FOLDERS]?.let(::decodeFolders),
            homeWidgets = prefs[Keys.HOME_WIDGETS]?.let(::decodeWidgets),
            wallpaperMode = enumOrDefault(prefs[Keys.WALLPAPER_MODE], WallpaperMode.AUTO_DYNAMIC),
            lockFontStyle = enumOrDefault(prefs[Keys.LOCK_FONT_STYLE], LockFontStyle.ROUNDED_EDITORIAL),
            lockClockTint = enumOrDefault(prefs[Keys.LOCK_CLOCK_TINT], LockClockTint.GLASS_WHITE),
            showLockScreenOnStart = prefs[Keys.SHOW_LOCK_ON_START] ?: false,
            photoWidgetUri = prefs[Keys.PHOTO_WIDGET_URI],
            quickNotes = prefs[Keys.QUICK_NOTES]?.let(::decodeNotes),
            focusMode = enumOrDefault(prefs[Keys.FOCUS_MODE], FocusMode.OFF),
            focusAllowedGroups = prefs[Keys.FOCUS_ALLOWED_GROUPS]?.let { decodeStringList(it).toSet() },
            appLaunchCounts = prefs[Keys.APP_LAUNCH_COUNTS]?.let(::decodeIntMap) ?: emptyMap(),
            suppressDefaultLauncherPrompt = prefs[Keys.SUPPRESS_DEFAULT_LAUNCHER_PROMPT] ?: false
        )
    }

    suspend fun saveDockApps(ids: List<String>) {
        context.auraDataStore.edit { it[Keys.DOCK_APP_IDS] = encodeStringList(ids) }
    }

    suspend fun saveDockStyle(style: DockStyle) {
        context.auraDataStore.edit { it[Keys.DOCK_STYLE] = style.name }
    }

    suspend fun saveHomeApps(ids: List<String>) {
        context.auraDataStore.edit { it[Keys.HOME_APP_IDS] = encodeStringList(ids) }
    }

    suspend fun saveHomeFolders(folders: List<HomeFolder>) {
        context.auraDataStore.edit { it[Keys.HOME_FOLDERS] = encodeFolders(folders) }
    }

    suspend fun saveHomeWidgets(widgets: List<HomeWidget>) {
        context.auraDataStore.edit { it[Keys.HOME_WIDGETS] = encodeWidgets(widgets) }
    }

    suspend fun saveWallpaperMode(mode: WallpaperMode) {
        context.auraDataStore.edit { it[Keys.WALLPAPER_MODE] = mode.name }
    }

    suspend fun saveLockFontStyle(style: LockFontStyle) {
        context.auraDataStore.edit { it[Keys.LOCK_FONT_STYLE] = style.name }
    }

    suspend fun saveLockClockTint(tint: LockClockTint) {
        context.auraDataStore.edit { it[Keys.LOCK_CLOCK_TINT] = tint.name }
    }

    suspend fun saveShowLockOnStart(show: Boolean) {
        context.auraDataStore.edit { it[Keys.SHOW_LOCK_ON_START] = show }
    }

    suspend fun savePhotoWidgetUri(uri: String?) {
        context.auraDataStore.edit {
            if (uri == null) it.remove(Keys.PHOTO_WIDGET_URI)
            else it[Keys.PHOTO_WIDGET_URI] = uri
        }
    }

    suspend fun saveQuickNotes(notes: List<QuickNoteItem>) {
        context.auraDataStore.edit { it[Keys.QUICK_NOTES] = encodeNotes(notes) }
    }

    suspend fun saveFocusState(mode: FocusMode, allowedGroups: Set<String>) {
        context.auraDataStore.edit {
            it[Keys.FOCUS_MODE] = mode.name
            it[Keys.FOCUS_ALLOWED_GROUPS] = encodeStringList(allowedGroups.toList())
        }
    }

    suspend fun saveAppLaunchCounts(counts: Map<String, Int>) {
        context.auraDataStore.edit { it[Keys.APP_LAUNCH_COUNTS] = encodeIntMap(counts) }
    }

    suspend fun saveSuppressDefaultLauncherPrompt(suppress: Boolean) {
        context.auraDataStore.edit { it[Keys.SUPPRESS_DEFAULT_LAUNCHER_PROMPT] = suppress }
    }

    private fun encodeIntMap(map: Map<String, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    private fun decodeIntMap(raw: String): Map<String, Int> = runCatching {
        val obj = JSONObject(raw)
        val out = mutableMapOf<String, Int>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            out[k] = obj.optInt(k, 0)
        }
        out
    }.getOrDefault(emptyMap())

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T {
        if (raw.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
    }

    private fun encodeStringList(list: List<String>): String =
        JSONArray().apply { list.forEach(::put) }.toString()

    private fun decodeStringList(raw: String): List<String> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx -> arr.getString(idx) }
    }.getOrDefault(emptyList())

    private fun encodeFolders(folders: List<HomeFolder>): String {
        val arr = JSONArray()
        folders.forEach { folder ->
            arr.put(JSONObject().apply {
                put("id", folder.id)
                put("title", folder.title)
                put("pageIndex", folder.pageIndex)
                put("appIds", JSONArray().apply { folder.appIds.forEach(::put) })
            })
        }
        return arr.toString()
    }

    private fun decodeFolders(raw: String): List<HomeFolder> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            val appArr = obj.optJSONArray("appIds") ?: JSONArray()
            HomeFolder(
                id = obj.getString("id"),
                title = obj.getString("title"),
                pageIndex = obj.optInt("pageIndex", 0),
                appIds = List(appArr.length()) { j -> appArr.getString(j) }
            )
        }
    }.getOrDefault(emptyList())

    private fun encodeWidgets(widgets: List<HomeWidget>): String {
        val arr = JSONArray()
        widgets.forEach { w ->
            arr.put(JSONObject().apply {
                put("id", w.id)
                put("type", w.type.name)
                put("size", w.size.name)
                put("pageIndex", w.pageIndex)
            })
        }
        return arr.toString()
    }

    private fun decodeWidgets(raw: String): List<HomeWidget> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            val type = enumOrDefault(obj.getString("type"), WidgetType.WEATHER_SUN)
            val size = enumOrDefault(obj.optString("size"), type.defaultSize)
            HomeWidget(
                id = obj.getString("id"),
                type = type,
                size = size,
                pageIndex = obj.optInt("pageIndex", 0)
            )
        }
    }.getOrDefault(emptyList())

    private fun encodeNotes(notes: List<QuickNoteItem>): String {
        val arr = JSONArray()
        notes.forEach { n ->
            arr.put(JSONObject().apply {
                put("id", n.id)
                put("text", n.text)
                put("isDone", n.isDone)
            })
        }
        return arr.toString()
    }

    private fun decodeNotes(raw: String): List<QuickNoteItem> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            QuickNoteItem(
                id = obj.getString("id"),
                text = obj.getString("text"),
                isDone = obj.optBoolean("isDone", false)
            )
        }
    }.getOrDefault(emptyList())
}
