package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppIndexService
import com.example.data.AppIndexSnapshot
import com.example.data.AppSearchHit
import com.example.data.InstalledAppsRepository
import com.example.data.LauncherPreferencesRepository
import com.example.data.SystemControlBridge
import com.example.haptic.AuraHapticEngine
import com.example.model.ActiveSheet
import com.example.model.AppCategory
import com.example.model.BuiltInAction
import com.example.model.ControlCenterState
import com.example.model.DeviceTelemetry
import com.example.model.DockStyle
import com.example.model.FocusMode
import com.example.model.HomeFolder
import com.example.model.HomeWidget
import com.example.model.LauncherApp
import com.example.model.LockClockTint
import com.example.model.LockFontStyle
import com.example.model.LockNotification
import com.example.model.QuickNoteItem
import com.example.model.TimeOfDayPhase
import com.example.model.WallpaperMode
import com.example.model.WidgetType
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraBlue
import com.example.ui.theme.AuraCoral
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraTeal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<LauncherApp> = emptyList(),
    val homeAppIds: List<String> = emptyList(),
    val dockAppIds: List<String> = emptyList(),
    val dockStyle: DockStyle = DockStyle.FROSTED_GLASS,
    val homeFolders: List<HomeFolder> = emptyList(),
    val homeWidgets: List<HomeWidget> = emptyList(),
    val quickNotes: List<QuickNoteItem> = emptyList(),
    val photoWidgetUri: String? = null,
    val wallpaperMode: WallpaperMode = WallpaperMode.AUTO_DYNAMIC,
    val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val currentTimeText: String = "",
    val currentDateText: String = "",
    val activePhase: TimeOfDayPhase = TimeOfDayPhase.fromHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)),
    val isEditMode: Boolean = false,
    val activeFolder: HomeFolder? = null,
    val activeCategoryFolder: AppCategory? = null,
    val isControlCenterOpen: Boolean = false,
    val isSpotlightOpen: Boolean = false,
    val spotlightQuery: String = "",
    val isLockScreenVisible: Boolean = false,
    val isLockCustomizerOpen: Boolean = false,
    val showLockScreenOnStart: Boolean = false,
    val lockFontStyle: LockFontStyle = LockFontStyle.ROUNDED_EDITORIAL,
    val lockClockTint: LockClockTint = LockClockTint.GLASS_WHITE,
    val notifications: List<LockNotification> = emptyList(),
    val expandedNotificationGroups: Set<String> = emptySet(),
    val controlCenter: ControlCenterState = ControlCenterState(),
    val telemetry: DeviceTelemetry = DeviceTelemetry(),
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val currentPageIndex: Int = 0,
    val isDynamicIslandExpanded: Boolean = false,
    val statusBannerMessage: String? = null,
    val appLaunchCounts: Map<String, Int> = emptyMap(),
    val showSilencedNotifications: Boolean = false,
    val isDefaultHomeApp: Boolean = false,
    val showDefaultLauncherPrompt: Boolean = false,
    val suppressDefaultLauncherPrompt: Boolean = false,
    val homeNavigationSignal: Int = 0
) {
    val visibleNotifications: List<LockNotification>
        get() = if (controlCenter.focusMode == FocusMode.OFF || showSilencedNotifications) {
            notifications
        } else {
            notifications.filter { it.groupKey in controlCenter.focusAllowedGroups }
        }

    val silencedNotificationCount: Int
        get() = if (controlCenter.focusMode == FocusMode.OFF) {
            0
        } else {
            notifications.count { it.groupKey !in controlCenter.focusAllowedGroups }
        }
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appsRepo = InstalledAppsRepository(application)
    private val prefsRepo = LauncherPreferencesRepository(application)
    private val systemBridge = SystemControlBridge(application)
    private val appIndexService = AppIndexService()
    val haptics = AuraHapticEngine(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()
    val appIndexState: StateFlow<AppIndexSnapshot> = appIndexService.snapshot

    init {
        loadInitialData()
        startClockAndTelemetryTicker()
    }

    fun getAppDrawable(appId: String): Drawable? = appsRepo.getDrawableForApp(appId)

    fun searchIndexedApps(query: String, limit: Int = 24): List<AppSearchHit> =
        appIndexService.search(query, _uiState.value.appLaunchCounts, limit)

    fun refreshAppIndex() {
        haptics.tapLight()
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { appsRepo.loadAllApps() }
            val snapshot = appIndexService.rebuildIndex(apps)
            _uiState.update { it.copy(allApps = apps) }
            showBanner("Indexed ${snapshot.indexedCount} apps for Spotlight")
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { appsRepo.loadAllApps() }
            appIndexService.rebuildIndex(apps)
            val prefs = prefsRepo.prefsFlow.first()
            val (battery, charging) = systemBridge.readBatteryTelemetry()
            val (wifi, cell) = systemBridge.readNetworkState()
            val volume = systemBridge.getNormalizedMusicVolume()
            val torchAvail = systemBridge.isFlashlightAvailable()
            val musicActive = systemBridge.isMusicActive()
            val dndGranted = systemBridge.isDndPolicyAccessGranted()
            val sysDndActive = systemBridge.isSystemDndActive()
            val isDefaultHome = systemBridge.isDefaultHomeApp()

            val appById = apps.associateBy { it.id }

            val defaultDock = listOf("sys:phone", "sys:safari", "sys:messages", "aura:wallpapers")
                .filter { it in appById }
                .ifEmpty { apps.take(4).map { it.id } }

            val dockIds = (prefs.dockAppIds ?: defaultDock)
                .filter { it in appById }
                .take(5)
                .ifEmpty { defaultDock }

            val defaultHomeApps = apps
                .map { it.id }
                .filterNot { it in dockIds }
                .take(16)

            val homeIds = (prefs.homeAppIds ?: defaultHomeApps)
                .filter { it in appById }
                .ifEmpty { defaultHomeApps }

            val defaultFolders = listOf(
                HomeFolder(
                    id = "folder_aura_studio",
                    title = "Aura Studio",
                    appIds = listOf(
                        "aura:wallpapers",
                        "aura:lock_style",
                        "aura:dock_setup",
                        "aura:widgets",
                        "aura:control_center",
                        "aura:lock_screen"
                    ).filter { it in appById },
                    pageIndex = 0
                )
            ).filter { it.appIds.isNotEmpty() }

            val defaultWidgets = listOf(
                HomeWidget(id = "w_weather", type = WidgetType.WEATHER_SUN, pageIndex = 0),
                HomeWidget(id = "w_battery", type = WidgetType.BATTERY_RING, pageIndex = 0),
                HomeWidget(id = "w_focus", type = WidgetType.FOCUS_MODE, pageIndex = 0),
                HomeWidget(id = "w_calendar", type = WidgetType.CALENDAR_TODAY, pageIndex = 1),
                HomeWidget(id = "w_notes", type = WidgetType.QUICK_NOTES, pageIndex = 1)
            )

            val defaultNotes = listOf(
                QuickNoteItem("n1", "Customize Lock Screen clock font", false),
                QuickNoteItem("n2", "Swipe left to open App Library", false),
                QuickNoteItem("n3", "Swipe down top-right for Controls", true)
            )

            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val resolvedPhase = prefs.wallpaperMode.resolvePhase(hour)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    allApps = apps,
                    homeAppIds = homeIds,
                    dockAppIds = dockIds,
                    dockStyle = prefs.dockStyle,
                    homeFolders = prefs.homeFolders ?: defaultFolders,
                    homeWidgets = prefs.homeWidgets ?: defaultWidgets,
                    quickNotes = prefs.quickNotes ?: defaultNotes,
                    photoWidgetUri = prefs.photoWidgetUri,
                    wallpaperMode = prefs.wallpaperMode,
                    currentHour = hour,
                    currentTimeText = formatClock(cal),
                    currentDateText = formatDate(cal),
                    activePhase = resolvedPhase,
                    lockFontStyle = prefs.lockFontStyle,
                    lockClockTint = prefs.lockClockTint,
                    showLockScreenOnStart = prefs.showLockScreenOnStart,
                    isLockScreenVisible = prefs.showLockScreenOnStart,
                    notifications = createInitialNotifications(),
                    appLaunchCounts = prefs.appLaunchCounts,
                    isDefaultHomeApp = isDefaultHome,
                    suppressDefaultLauncherPrompt = prefs.suppressDefaultLauncherPrompt,
                    showDefaultLauncherPrompt = !isDefaultHome && !prefs.suppressDefaultLauncherPrompt,
                    controlCenter = state.controlCenter.copy(
                        wifiConnected = wifi,
                        cellularConnected = cell,
                        mediaVolume = volume,
                        flashlightAvailable = torchAvail,
                        isPlayingMusic = musicActive,
                        focusMode = prefs.focusMode,
                        focusAllowedGroups = prefs.focusAllowedGroups ?: prefs.focusMode.defaultAllowedGroups(),
                        isDndPolicyGranted = dndGranted,
                        systemDndActive = sysDndActive
                    ),
                    telemetry = computeTelemetry(hour, battery, charging)
                )
            }
        }
    }

    private fun startClockAndTelemetryTicker() {
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val (battery, charging) = systemBridge.readBatteryTelemetry()
                val (wifi, cell) = systemBridge.readNetworkState()

                _uiState.update { state ->
                    val phase = state.wallpaperMode.resolvePhase(hour)
                    state.copy(
                        currentHour = hour,
                        currentTimeText = formatClock(cal),
                        currentDateText = formatDate(cal),
                        activePhase = phase,
                        telemetry = computeTelemetry(hour, battery, charging),
                        controlCenter = state.controlCenter.copy(
                            wifiConnected = if (state.controlCenter.airplaneMode) false else wifi,
                            cellularConnected = if (state.controlCenter.airplaneMode) false else cell
                        )
                    )
                }
                delay(15_000L)
            }
        }
    }

    fun onAppClick(app: LauncherApp) {
        if (_uiState.value.isEditMode) return
        haptics.tapLight()
        val updatedCounts = _uiState.value.appLaunchCounts.toMutableMap().apply {
            this[app.id] = (this[app.id] ?: 0) + 1
        }
        _uiState.update { it.copy(appLaunchCounts = updatedCounts) }
        viewModelScope.launch { prefsRepo.saveAppLaunchCounts(updatedCounts) }

        appsRepo.launchApp(app) { action ->
            when (action) {
                BuiltInAction.OPEN_WALLPAPER_STUDIO -> openSheet(ActiveSheet.WALLPAPER_STUDIO)
                BuiltInAction.OPEN_DOCK_SETTINGS -> openSheet(ActiveSheet.DOCK_CUSTOMIZER)
                BuiltInAction.OPEN_LOCK_CUSTOMIZER -> {
                    _uiState.update {
                        it.copy(
                            isLockScreenVisible = true,
                            isLockCustomizerOpen = true,
                            isControlCenterOpen = false,
                            isSpotlightOpen = false
                        )
                    }
                }
                BuiltInAction.OPEN_WIDGET_PICKER -> openSheet(ActiveSheet.WIDGET_PICKER)
                BuiltInAction.TOGGLE_CONTROL_CENTER -> openControlCenter()
                BuiltInAction.LOCK_DEVICE_SCREEN -> openLockScreen()
                BuiltInAction.OPEN_PHOTOS_PICKER -> {
                    showBanner("Tap the Photos widget to choose a memory photo")
                }
                BuiltInAction.SET_DEFAULT_LAUNCHER -> openDefaultLauncherPrompt()
                BuiltInAction.NONE,
                BuiltInAction.DIAL_PHONE,
                BuiltInAction.OPEN_MESSAGES,
                BuiltInAction.OPEN_CAMERA,
                BuiltInAction.OPEN_BROWSER,
                BuiltInAction.OPEN_CALCULATOR,
                BuiltInAction.OPEN_CALENDAR,
                BuiltInAction.OPEN_SETTINGS -> Unit
            }
        }
    }

    fun setCurrentPage(pageIndex: Int) {
        if (_uiState.value.currentPageIndex != pageIndex) {
            haptics.navigationSwipe()
            _uiState.update { it.copy(currentPageIndex = pageIndex) }
        }
    }

    fun toggleEditMode(enabled: Boolean) {
        if (enabled && !_uiState.value.isEditMode) {
            haptics.longPressPop()
        } else if (!enabled && _uiState.value.isEditMode) {
            haptics.tapLight()
        }
        _uiState.update { it.copy(isEditMode = enabled) }
    }

    fun openFolder(folder: HomeFolder?) {
        if (folder != null || _uiState.value.activeFolder != null) {
            haptics.folderOpen()
        }
        _uiState.update { it.copy(activeFolder = folder) }
    }

    fun openCategoryFolder(category: AppCategory?) {
        if (category != null || _uiState.value.activeCategoryFolder != null) {
            haptics.folderOpen()
        }
        _uiState.update { it.copy(activeCategoryFolder = category) }
    }

    fun openControlCenter() {
        haptics.navigationSwipe()
        val vol = systemBridge.getNormalizedMusicVolume()
        val dndGranted = systemBridge.isDndPolicyAccessGranted()
        val sysDndActive = systemBridge.isSystemDndActive()
        _uiState.update {
            it.copy(
                isControlCenterOpen = true,
                isSpotlightOpen = false,
                activeFolder = null,
                controlCenter = it.controlCenter.copy(
                    mediaVolume = vol,
                    isDndPolicyGranted = dndGranted,
                    systemDndActive = sysDndActive
                )
            )
        }
    }

    fun closeControlCenter() {
        haptics.navigationSwipe()
        _uiState.update { it.copy(isControlCenterOpen = false) }
    }

    fun openSpotlight() {
        haptics.navigationSwipe()
        _uiState.update {
            it.copy(
                isSpotlightOpen = true,
                isControlCenterOpen = false,
                activeFolder = null
            )
        }
    }

    fun closeSpotlight() {
        _uiState.update { it.copy(isSpotlightOpen = false, spotlightQuery = "") }
    }

    fun updateSpotlightQuery(query: String) {
        _uiState.update { it.copy(spotlightQuery = query) }
    }

    fun openLockScreen(openCustomizer: Boolean = false) {
        haptics.navigationSwipe()
        _uiState.update {
            it.copy(
                isLockScreenVisible = true,
                isLockCustomizerOpen = openCustomizer,
                isControlCenterOpen = false,
                isSpotlightOpen = false,
                activeFolder = null,
                isEditMode = false
            )
        }
    }

    fun unlockLockScreen() {
        haptics.navigationSwipe()
        _uiState.update {
            it.copy(
                isLockScreenVisible = false,
                isLockCustomizerOpen = false,
                showSilencedNotifications = false
            )
        }
    }

    fun setLockCustomizerOpen(open: Boolean) {
        _uiState.update { it.copy(isLockCustomizerOpen = open) }
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.update {
            it.copy(
                activeSheet = sheet,
                isControlCenterOpen = false,
                isSpotlightOpen = false
            )
        }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = ActiveSheet.NONE) }
    }

    fun toggleDynamicIsland() {
        haptics.tapLight()
        _uiState.update { it.copy(isDynamicIslandExpanded = !it.isDynamicIslandExpanded) }
    }

    // --- Home & Dock Customization ---

    fun addAppToHome(appId: String) {
        val current = _uiState.value.homeAppIds
        if (appId in current) return
        val updated = current + appId
        _uiState.update { it.copy(homeAppIds = updated) }
        viewModelScope.launch { prefsRepo.saveHomeApps(updated) }
        showBanner("Added to Home Screen")
    }

    fun removeAppFromHome(appId: String) {
        val updated = _uiState.value.homeAppIds.filterNot { it == appId }
        _uiState.update { it.copy(homeAppIds = updated) }
        viewModelScope.launch { prefsRepo.saveHomeApps(updated) }
    }

    fun toggleDockApp(appId: String) {
        val current = _uiState.value.dockAppIds
        val updated = if (appId in current) {
            if (current.size <= 1) {
                showBanner("Keep at least 1 app in the Dock")
                return
            }
            current.filterNot { it == appId }
        } else {
            if (current.size >= 5) {
                showBanner("Dock supports up to 5 pinned apps")
                return
            }
            current + appId
        }
        _uiState.update { it.copy(dockAppIds = updated) }
        viewModelScope.launch { prefsRepo.saveDockApps(updated) }
    }

    fun setDockStyle(style: DockStyle) {
        _uiState.update { it.copy(dockStyle = style) }
        viewModelScope.launch { prefsRepo.saveDockStyle(style) }
    }

    fun addWidget(type: WidgetType) {
        val page = _uiState.value.currentPageIndex.coerceAtMost(1)
        val newWidget = HomeWidget(
            id = "w_${UUID.randomUUID().toString().take(8)}",
            type = type,
            size = type.defaultSize,
            pageIndex = page
        )
        val updated = _uiState.value.homeWidgets + newWidget
        _uiState.update { it.copy(homeWidgets = updated, activeSheet = ActiveSheet.NONE) }
        viewModelScope.launch { prefsRepo.saveHomeWidgets(updated) }
        showBanner("Added ${type.title} widget")
    }

    fun removeWidget(widgetId: String) {
        val updated = _uiState.value.homeWidgets.filterNot { it.id == widgetId }
        _uiState.update { it.copy(homeWidgets = updated) }
        viewModelScope.launch { prefsRepo.saveHomeWidgets(updated) }
    }

    fun createFolder(title: String, appIds: List<String>) {
        if (appIds.isEmpty()) return
        val cleanTitle = title.trim().ifBlank { "Folder" }
        val page = _uiState.value.currentPageIndex.coerceAtMost(1)
        val folder = HomeFolder(
            id = "folder_${UUID.randomUUID().toString().take(8)}",
            title = cleanTitle,
            appIds = appIds,
            pageIndex = page
        )
        val updatedFolders = _uiState.value.homeFolders + folder
        _uiState.update { it.copy(homeFolders = updatedFolders, activeSheet = ActiveSheet.NONE) }
        viewModelScope.launch { prefsRepo.saveHomeFolders(updatedFolders) }
        showBanner("Created \"$cleanTitle\" folder")
    }

    fun renameFolder(folderId: String, newTitle: String) {
        val clean = newTitle.trim().ifBlank { "Folder" }
        val updated = _uiState.value.homeFolders.map {
            if (it.id == folderId) it.copy(title = clean) else it
        }
        val active = _uiState.value.activeFolder?.let {
            if (it.id == folderId) it.copy(title = clean) else it
        }
        _uiState.update { it.copy(homeFolders = updated, activeFolder = active) }
        viewModelScope.launch { prefsRepo.saveHomeFolders(updated) }
    }

    fun deleteFolder(folderId: String) {
        val updated = _uiState.value.homeFolders.filterNot { it.id == folderId }
        _uiState.update { it.copy(homeFolders = updated, activeFolder = null) }
        viewModelScope.launch { prefsRepo.saveHomeFolders(updated) }
    }

    fun removeAppFromFolder(folderId: String, appId: String) {
        val updated = _uiState.value.homeFolders.mapNotNull { folder ->
            if (folder.id != folderId) folder
            else {
                val remaining = folder.appIds.filterNot { it == appId }
                if (remaining.isEmpty()) null else folder.copy(appIds = remaining)
            }
        }
        val active = updated.firstOrNull { it.id == folderId }
        _uiState.update { it.copy(homeFolders = updated, activeFolder = active) }
        viewModelScope.launch { prefsRepo.saveHomeFolders(updated) }
    }

    // --- Quick Notes & Photo Picker ---

    fun toggleQuickNote(noteId: String) {
        val updated = _uiState.value.quickNotes.map {
            if (it.id == noteId) it.copy(isDone = !it.isDone) else it
        }
        _uiState.update { it.copy(quickNotes = updated) }
        viewModelScope.launch { prefsRepo.saveQuickNotes(updated) }
    }

    fun addQuickNote(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        val updated = listOf(
            QuickNoteItem("n_${System.currentTimeMillis()}", clean, false)
        ) + _uiState.value.quickNotes
        _uiState.update { it.copy(quickNotes = updated.take(6)) }
        viewModelScope.launch { prefsRepo.saveQuickNotes(updated.take(6)) }
    }

    fun updatePhotoWidgetUri(uriString: String?) {
        _uiState.update { it.copy(photoWidgetUri = uriString) }
        viewModelScope.launch { prefsRepo.savePhotoWidgetUri(uriString) }
    }

    // --- Dynamic Wallpaper & Lock Screen Style ---

    fun setWallpaperMode(mode: WallpaperMode) {
        val hour = _uiState.value.currentHour
        val phase = mode.resolvePhase(hour)
        _uiState.update {
            it.copy(
                wallpaperMode = mode,
                activePhase = phase,
                telemetry = computeTelemetry(
                    phaseHourHint(phase, hour),
                    it.telemetry.batteryLevel,
                    it.telemetry.isCharging
                )
            )
        }
        viewModelScope.launch { prefsRepo.saveWallpaperMode(mode) }
    }

    fun setLockFontStyle(style: LockFontStyle) {
        haptics.tapLight()
        _uiState.update { it.copy(lockFontStyle = style) }
        viewModelScope.launch { prefsRepo.saveLockFontStyle(style) }
    }

    fun setLockClockTint(tint: LockClockTint) {
        haptics.tapLight()
        _uiState.update { it.copy(lockClockTint = tint) }
        viewModelScope.launch { prefsRepo.saveLockClockTint(tint) }
    }

    fun setShowLockScreenOnStart(show: Boolean) {
        haptics.controlToggle(show)
        _uiState.update { it.copy(showLockScreenOnStart = show) }
        viewModelScope.launch { prefsRepo.saveShowLockOnStart(show) }
    }

    // --- Lock Screen Notification Stacks ---

    fun toggleShowSilencedNotifications() {
        haptics.tapLight()
        _uiState.update { it.copy(showSilencedNotifications = !it.showSilencedNotifications) }
    }

    fun toggleNotificationGroupExpanded(groupKey: String) {
        haptics.folderOpen()
        _uiState.update { state ->
            val next = state.expandedNotificationGroups.toMutableSet()
            if (groupKey in next) next.remove(groupKey) else next.add(groupKey)
            state.copy(expandedNotificationGroups = next)
        }
    }

    fun dismissNotification(notificationId: String) {
        haptics.tapLight()
        _uiState.update { state ->
            state.copy(notifications = state.notifications.filterNot { it.id == notificationId })
        }
    }

    fun clearNotificationGroup(groupKey: String) {
        haptics.folderOpen()
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.filterNot { it.groupKey == groupKey },
                expandedNotificationGroups = state.expandedNotificationGroups - groupKey
            )
        }
    }

    fun addSampleNotification() {
        val samples = listOf(
            LockNotification(
                id = "notif_${System.currentTimeMillis()}",
                groupKey = "messages",
                appName = "Messages",
                title = "Maya Chen",
                body = "Just sent the sunset shots from Marin Headlands!",
                timeLabel = "now",
                accentColor = AuraTeal,
                iconKey = "messages"
            ),
            LockNotification(
                id = "notif_${System.currentTimeMillis()}",
                groupKey = "calendar",
                appName = "Calendar",
                title = "Design Review",
                body = "Starts in 15 min • Studio B",
                timeLabel = "now",
                accentColor = AuraCoral,
                iconKey = "calendar"
            ),
            LockNotification(
                id = "notif_${System.currentTimeMillis()}",
                groupKey = "aura",
                appName = "Aura Weather",
                title = "Golden Hour Advisory",
                body = "Sky gradient shifts to Sunset in 20 minutes.",
                timeLabel = "now",
                accentColor = AuraAmber,
                iconKey = "weather"
            )
        )
        val pick = samples.random()
        _uiState.update { state ->
            state.copy(notifications = listOf(pick) + state.notifications)
        }
    }

    // --- Control Center Hardware & Quick Controls ---

    fun toggleWifi() {
        val next = !_uiState.value.controlCenter.wifiConnected
        haptics.controlToggle(next)
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    wifiConnected = next,
                    airplaneMode = false
                )
            )
        }
    }

    fun openWifiSystemPanel() = systemBridge.openWifiPanel()

    fun toggleBluetooth() {
        val next = !_uiState.value.controlCenter.bluetoothEnabled
        haptics.controlToggle(next)
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    bluetoothEnabled = next,
                    airplaneMode = false
                )
            )
        }
    }

    fun openBluetoothSystemSettings() = systemBridge.openBluetoothSettings()

    fun toggleAirplaneMode() {
        _uiState.update { state ->
            val next = !state.controlCenter.airplaneMode
            haptics.controlToggle(next)
            state.copy(
                controlCenter = state.controlCenter.copy(
                    airplaneMode = next,
                    wifiConnected = if (next) false else true,
                    cellularConnected = if (next) false else true,
                    bluetoothEnabled = if (next) false else state.controlCenter.bluetoothEnabled
                )
            )
        }
    }

    fun toggleCellular() {
        val next = !_uiState.value.controlCenter.cellularConnected
        haptics.controlToggle(next)
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    cellularConnected = next,
                    airplaneMode = false
                )
            )
        }
    }

    fun toggleOrientationLock() {
        val next = !_uiState.value.controlCenter.orientationLocked
        haptics.controlToggle(next)
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    orientationLocked = next
                )
            )
        }
    }

    fun cycleFocusMode() {
        val all = FocusMode.entries
        val current = _uiState.value.controlCenter.focusMode
        val next = all[(all.indexOf(current) + 1) % all.size]
        setFocusMode(next)
    }

    fun toggleDoNotDisturb() {
        val current = _uiState.value.controlCenter.focusMode
        val next = if (current == FocusMode.OFF) FocusMode.DO_NOT_DISTURB else FocusMode.OFF
        setFocusMode(next)
    }

    fun setFocusMode(mode: FocusMode) {
        val allowed = mode.defaultAllowedGroups()
        val dndEnabled = mode != FocusMode.OFF
        haptics.controlToggle(dndEnabled)
        val systemApplied = systemBridge.setSystemDnd(dndEnabled)
        val dndGranted = systemBridge.isDndPolicyAccessGranted()
        _uiState.update {
            it.copy(
                showSilencedNotifications = false,
                controlCenter = it.controlCenter.copy(
                    focusMode = mode,
                    focusAllowedGroups = allowed,
                    isDndPolicyGranted = dndGranted,
                    systemDndActive = if (systemApplied) dndEnabled else systemBridge.isSystemDndActive()
                )
            )
        }
        viewModelScope.launch { prefsRepo.saveFocusState(mode, allowed) }
    }

    fun toggleFocusAllowedGroup(groupKey: String) {
        haptics.tapLight()
        val currentMode = _uiState.value.controlCenter.focusMode
        val targetMode = if (currentMode == FocusMode.OFF) FocusMode.DO_NOT_DISTURB else currentMode
        val nextGroups = _uiState.value.controlCenter.focusAllowedGroups.toMutableSet().apply {
            if (groupKey in this) remove(groupKey) else add(groupKey)
        }
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    focusMode = targetMode,
                    focusAllowedGroups = nextGroups
                )
            )
        }
        viewModelScope.launch { prefsRepo.saveFocusState(targetMode, nextGroups) }
    }

    fun openDndPolicySettings() {
        haptics.tapLight()
        systemBridge.openDndPolicyAccessSettings()
    }

    fun refreshSystemDndStatus() {
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    isDndPolicyGranted = systemBridge.isDndPolicyAccessGranted(),
                    systemDndActive = systemBridge.isSystemDndActive()
                )
            )
        }
    }

    fun buildDefaultHomeRoleIntent(): Intent? {
        haptics.tapLight()
        return systemBridge.createDefaultHomeRoleIntent()
    }

    fun openFallbackHomeSettings() {
        haptics.tapLight()
        _uiState.update { it.copy(showDefaultLauncherPrompt = false) }
        val opened = systemBridge.openDefaultHomeSettings()
        if (opened) {
            showBanner("Select Aura Launcher as your Home app")
        } else {
            showBanner("Open Android Default Apps settings to choose Aura")
        }
    }

    fun onDefaultHomeRoleResult() {
        val isNowDefault = systemBridge.isDefaultHomeApp()
        val wasDefault = _uiState.value.isDefaultHomeApp
        _uiState.update {
            it.copy(
                isDefaultHomeApp = isNowDefault,
                showDefaultLauncherPrompt = false
            )
        }
        if (isNowDefault && !wasDefault) {
            haptics.controlToggle(true)
            showBanner("Aura Launcher is now your default Home screen")
        }
    }

    fun refreshDefaultHomeStatus() {
        val isNowDefault = systemBridge.isDefaultHomeApp()
        val wasDefault = _uiState.value.isDefaultHomeApp
        _uiState.update { state ->
            state.copy(
                isDefaultHomeApp = isNowDefault,
                showDefaultLauncherPrompt = if (isNowDefault) false else state.showDefaultLauncherPrompt
            )
        }
        if (isNowDefault && !wasDefault) {
            haptics.controlToggle(true)
            showBanner("Aura Launcher is now your default Home screen")
        }
    }

    fun openDefaultLauncherPrompt() {
        haptics.tapLight()
        val isNowDefault = systemBridge.isDefaultHomeApp()
        if (isNowDefault) {
            _uiState.update { it.copy(isDefaultHomeApp = true, showDefaultLauncherPrompt = false) }
            systemBridge.openDefaultHomeSettings()
        } else {
            _uiState.update {
                it.copy(
                    isDefaultHomeApp = false,
                    showDefaultLauncherPrompt = true,
                    isControlCenterOpen = false,
                    isSpotlightOpen = false
                )
            }
        }
    }

    fun dismissDefaultLauncherPrompt(dontAskAgain: Boolean = false) {
        haptics.tapLight()
        _uiState.update {
            it.copy(
                showDefaultLauncherPrompt = false,
                suppressDefaultLauncherPrompt = if (dontAskAgain) true else it.suppressDefaultLauncherPrompt
            )
        }
        if (dontAskAgain) {
            viewModelScope.launch { prefsRepo.saveSuppressDefaultLauncherPrompt(true) }
            showBanner("You can set Default Home anytime in Control Center")
        }
    }

    fun onHomeButtonPressed() {
        haptics.navigationSwipe()
        _uiState.update {
            it.copy(
                isControlCenterOpen = false,
                isSpotlightOpen = false,
                activeFolder = null,
                activeCategoryFolder = null,
                activeSheet = ActiveSheet.NONE,
                isEditMode = false,
                isLockCustomizerOpen = false,
                isDynamicIslandExpanded = false,
                homeNavigationSignal = it.homeNavigationSignal + 1
            )
        }
    }

    fun toggleFlashlight() {
        val next = !_uiState.value.controlCenter.flashlightOn
        haptics.controlToggle(next)
        val success = systemBridge.setFlashlight(next)
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    flashlightOn = if (success) next else next
                )
            )
        }
        if (!success && !systemBridge.isFlashlightAvailable()) {
            showBanner("Camera torch unavailable on this device")
        }
    }

    fun setBrightness(activity: Activity?, fraction: Float) {
        val clamped = fraction.coerceIn(0.05f, 1.0f)
        systemBridge.applyWindowBrightness(activity, clamped)
        _uiState.update { it.copy(controlCenter = it.controlCenter.copy(brightness = clamped)) }
    }

    fun setMediaVolume(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        systemBridge.setNormalizedMusicVolume(clamped)
        _uiState.update { it.copy(controlCenter = it.controlCenter.copy(mediaVolume = clamped)) }
    }

    fun togglePlayPause() {
        haptics.tapLight()
        systemBridge.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    isPlayingMusic = !it.controlCenter.isPlayingMusic
                )
            )
        }
    }

    fun skipNextTrack() {
        haptics.tapLight()
        systemBridge.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        val tracks = listOf(
            "Midnight Cityscapes" to "Aura Ambient",
            "Coastal Horizon" to "Kaito Takahashi",
            "Glass & Rain" to "Solstice Quartet",
            "Dawn Over Alpine" to "Nordic Waves"
        )
        val current = _uiState.value.controlCenter.currentTrackTitle
        val idx = tracks.indexOfFirst { it.first == current }
        val next = tracks[(idx + 1) % tracks.size]
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    currentTrackTitle = next.first,
                    currentTrackArtist = next.second,
                    isPlayingMusic = true
                )
            )
        }
    }

    fun skipPreviousTrack() {
        haptics.tapLight()
        systemBridge.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        val tracks = listOf(
            "Midnight Cityscapes" to "Aura Ambient",
            "Coastal Horizon" to "Kaito Takahashi",
            "Glass & Rain" to "Solstice Quartet",
            "Dawn Over Alpine" to "Nordic Waves"
        )
        val current = _uiState.value.controlCenter.currentTrackTitle
        val idx = tracks.indexOfFirst { it.first == current }
        val prev = tracks[if (idx <= 0) tracks.lastIndex else idx - 1]
        _uiState.update {
            it.copy(
                controlCenter = it.controlCenter.copy(
                    currentTrackTitle = prev.first,
                    currentTrackArtist = prev.second,
                    isPlayingMusic = true
                )
            )
        }
    }

    fun launchSystemCamera() = systemBridge.openCamera()
    fun launchSystemCalculator() = systemBridge.openCalculator()
    fun launchSystemTimer() = systemBridge.openTimer()
    fun launchDisplaySettings() = systemBridge.openDisplaySettings()

    fun dismissBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    private fun showBanner(message: String) {
        _uiState.update { it.copy(statusBannerMessage = message) }
        viewModelScope.launch {
            delay(2600L)
            _uiState.update {
                if (it.statusBannerMessage == message) it.copy(statusBannerMessage = null) else it
            }
        }
    }

    private fun formatClock(cal: Calendar): String =
        SimpleDateFormat("h:mm", Locale.getDefault()).format(cal.time)

    private fun formatDate(cal: Calendar): String =
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(cal.time)

    private fun phaseHourHint(phase: TimeOfDayPhase, fallbackHour: Int): Int = when (phase) {
        TimeOfDayPhase.DAWN -> 6
        TimeOfDayPhase.DAY -> 13
        TimeOfDayPhase.SUNSET -> 18
        TimeOfDayPhase.NIGHT -> 22
    }.takeIf { _uiState.value.wallpaperMode != WallpaperMode.AUTO_DYNAMIC } ?: fallbackHour

    private fun computeTelemetry(hour: Int, battery: Int, charging: Boolean): DeviceTelemetry {
        val (temp, cond) = when (TimeOfDayPhase.fromHour(hour)) {
            TimeOfDayPhase.DAWN -> 59 to "Crisp Dawn"
            TimeOfDayPhase.DAY -> 72 to "Mostly Sunny"
            TimeOfDayPhase.SUNSET -> 67 to "Golden Sky"
            TimeOfDayPhase.NIGHT -> 55 to "Clear Night"
        }
        return DeviceTelemetry(
            batteryLevel = battery,
            isCharging = charging,
            weatherTempF = temp,
            weatherCondition = cond,
            weatherHighF = 74,
            weatherLowF = 54
        )
    }

    private fun createInitialNotifications(): List<LockNotification> = listOf(
        LockNotification(
            id = "n_msg_1",
            groupKey = "messages",
            appName = "Messages",
            title = "Elena Rostova",
            body = "Are we still on for coffee at 10?",
            timeLabel = "4m ago",
            accentColor = AuraTeal,
            iconKey = "messages"
        ),
        LockNotification(
            id = "n_msg_2",
            groupKey = "messages",
            appName = "Messages",
            title = "Alex Mercer",
            body = "Shared a new album: Weekend Coastline",
            timeLabel = "18m ago",
            accentColor = AuraTeal,
            iconKey = "messages"
        ),
        LockNotification(
            id = "n_msg_3",
            groupKey = "messages",
            appName = "Messages",
            title = "Studio Group",
            body = "3 new photos added to the moodboard",
            timeLabel = "42m ago",
            accentColor = AuraTeal,
            iconKey = "messages"
        ),
        LockNotification(
            id = "n_cal_1",
            groupKey = "calendar",
            appName = "Calendar",
            title = "Product Sync",
            body = "10:30 AM – 11:15 AM • Main Conference",
            timeLabel = "in 25m",
            accentColor = AuraCoral,
            iconKey = "calendar"
        ),
        LockNotification(
            id = "n_cal_2",
            groupKey = "calendar",
            appName = "Calendar",
            title = "Evening Run",
            body = "6:00 PM • Embarcadero Trail",
            timeLabel = "Today",
            accentColor = AuraCoral,
            iconKey = "calendar"
        ),
        LockNotification(
            id = "n_aura_1",
            groupKey = "aura",
            appName = "Aura Launcher",
            title = "Dynamic Wallpaper Active",
            body = "Shifts automatically across Dawn, Day, Sunset & Night.",
            timeLabel = "1h ago",
            accentColor = AuraIndigo,
            iconKey = "wallpaper"
        )
    )
}
