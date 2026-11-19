package com.example

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppIndexService
import com.example.haptic.AuraHapticEngine
import com.example.model.AppCategory
import com.example.model.FocusMode
import com.example.model.LauncherApp
import com.example.model.LockFontStyle
import com.example.model.TimeOfDayPhase
import com.example.model.WallpaperMode
import com.example.model.WidgetType
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private fun awaitLoaded(vm: LauncherViewModel) {
        val looper = Shadows.shadowOf(Looper.getMainLooper())
        val deadline = System.currentTimeMillis() + 5_000L
        while (vm.uiState.value.isLoading && System.currentTimeMillis() < deadline) {
            looper.idle()
            Thread.sleep(25)
        }
        looper.idle()
        assertFalse("LauncherViewModel remained in loading state", vm.uiState.value.isLoading)
    }

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aura Launcher", appName)
    }

    @Test
    fun `time of day phases resolve across 24h cycle`() {
        assertEquals(TimeOfDayPhase.DAWN, TimeOfDayPhase.fromHour(6))
        assertEquals(TimeOfDayPhase.DAY, TimeOfDayPhase.fromHour(12))
        assertEquals(TimeOfDayPhase.SUNSET, TimeOfDayPhase.fromHour(18))
        assertEquals(TimeOfDayPhase.NIGHT, TimeOfDayPhase.fromHour(23))
    }

    @Test
    fun `view model handles control center lock screen fonts and wallpaper phases`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = LauncherViewModel(app)
        awaitLoaded(vm)

        vm.openControlCenter()
        assertTrue(vm.uiState.value.isControlCenterOpen)
        vm.closeControlCenter()
        assertFalse(vm.uiState.value.isControlCenterOpen)

        vm.openLockScreen(openCustomizer = true)
        assertTrue(vm.uiState.value.isLockScreenVisible)
        assertTrue(vm.uiState.value.isLockCustomizerOpen)

        vm.setLockFontStyle(LockFontStyle.CLASSIC_SERIF)
        assertEquals(LockFontStyle.CLASSIC_SERIF, vm.uiState.value.lockFontStyle)

        vm.toggleNotificationGroupExpanded("messages")
        assertTrue("messages" in vm.uiState.value.expandedNotificationGroups)

        vm.setWallpaperMode(WallpaperMode.MANUAL_SUNSET)
        assertEquals(TimeOfDayPhase.SUNSET, vm.uiState.value.activePhase)

        vm.unlockLockScreen()
        assertFalse(vm.uiState.value.isLockScreenVisible)
    }

    @Test
    fun `focus mode toggles dnd filters notifications and updates control center`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = LauncherViewModel(app)
        awaitLoaded(vm)

        assertTrue(vm.uiState.value.homeWidgets.any { it.type == WidgetType.FOCUS_MODE })
        assertEquals(FocusMode.OFF, vm.uiState.value.controlCenter.focusMode)
        val totalNotifs = vm.uiState.value.notifications.size
        assertTrue(totalNotifs >= 6)
        assertEquals(totalNotifs, vm.uiState.value.visibleNotifications.size)
        assertEquals(0, vm.uiState.value.silencedNotificationCount)

        // Toggle Do Not Disturb ON -> silences all 3 groups by default
        vm.toggleDoNotDisturb()
        assertEquals(FocusMode.DO_NOT_DISTURB, vm.uiState.value.controlCenter.focusMode)
        assertEquals(0, vm.uiState.value.visibleNotifications.size)
        assertEquals(totalNotifs, vm.uiState.value.silencedNotificationCount)

        // Allow Messages while in DND -> only Messages notifications appear
        vm.toggleFocusAllowedGroup("messages")
        assertTrue("messages" in vm.uiState.value.controlCenter.focusAllowedGroups)
        assertTrue(vm.uiState.value.visibleNotifications.isNotEmpty())
        assertTrue(vm.uiState.value.visibleNotifications.all { it.groupKey == "messages" })

        // Show silenced temporarily overrides visibility without clearing Focus Mode
        vm.toggleShowSilencedNotifications()
        assertEquals(totalNotifs, vm.uiState.value.visibleNotifications.size)

        // Switch to Work focus -> allows calendar + aura
        vm.setFocusMode(FocusMode.WORK)
        assertEquals(FocusMode.WORK, vm.uiState.value.controlCenter.focusMode)
        assertFalse(vm.uiState.value.showSilencedNotifications)
        assertTrue(vm.uiState.value.visibleNotifications.all { it.groupKey in setOf("calendar", "aura") })

        // Toggle DND back OFF -> restores all notifications
        vm.toggleDoNotDisturb()
        assertEquals(FocusMode.OFF, vm.uiState.value.controlCenter.focusMode)
        assertEquals(totalNotifs, vm.uiState.value.visibleNotifications.size)
        assertEquals(0, vm.uiState.value.silencedNotificationCount)
    }

    @Test
    fun `app index service catalogs apps and ranks exact acronym fuzzy and keyword hits`() = runBlocking {
        val indexService = AppIndexService()
        val apps = listOf(
            LauncherApp(
                id = "aura:wallpapers",
                label = "Aura Wallpapers",
                packageName = "com.example.wallpapers",
                category = AppCategory.CREATIVITY,
                iconKey = "wallpaper"
            ),
            LauncherApp(
                id = "sys:messages",
                label = "Messages",
                packageName = "com.google.android.apps.messaging",
                category = AppCategory.SOCIAL,
                iconKey = "messages"
            ),
            LauncherApp(
                id = "aura:control_center",
                label = "Control Center",
                packageName = "com.example.controls",
                category = AppCategory.UTILITIES,
                iconKey = "controls"
            ),
            LauncherApp(
                id = "sys:calculator",
                label = "Calculator",
                packageName = "com.android.calculator2",
                category = AppCategory.UTILITIES,
                iconKey = "calculator"
            )
        )

        val snapshot = indexService.rebuildIndex(apps)
        assertEquals(4, snapshot.indexedCount)
        assertFalse(snapshot.isIndexing)

        // Exact & prefix match
        val exactHits = indexService.search("Messages")
        assertEquals("sys:messages", exactHits.first().app.id)

        // Acronym match ("cc" -> Control Center)
        val acronymHits = indexService.search("cc")
        assertEquals("aura:control_center", acronymHits.first().app.id)
        assertTrue(acronymHits.first().matchReason.contains("Acronym"))

        // Fuzzy match ("Mesages" -> Messages)
        val fuzzyHits = indexService.search("Mesages")
        assertEquals("sys:messages", fuzzyHits.first().app.id)

        // Keyword match ("math" -> Calculator)
        val keywordHits = indexService.search("math")
        assertEquals("sys:calculator", keywordHits.first().app.id)

        // Blank query sorts by launch frequency
        val frequentHits = indexService.search("", launchCounts = mapOf("aura:wallpapers" to 5))
        assertEquals("aura:wallpapers", frequentHits.first().app.id)
        assertEquals("Frequently used", frequentHits.first().matchReason)
    }

    @Test
    fun `haptic engine executes all ios patterns safely without exceptions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val haptics = AuraHapticEngine(context)

        haptics.tapLight()
        haptics.longPressPop()
        haptics.folderOpen()
        haptics.navigationSwipe()
        haptics.controlToggle(true)
        haptics.controlToggle(false)
    }

    @Test
    fun `default launcher prompt displays when not default and home intent resets overlays`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val roleManager = app.getSystemService(RoleManager::class.java)
        val shadowRole = Shadows.shadowOf(roleManager)
        shadowRole.addAvailableRole(RoleManager.ROLE_HOME)

        val vm = LauncherViewModel(app)
        awaitLoaded(vm)

        // When ROLE_HOME is available and not held, Aura prompts to become default Home launcher
        assertFalse(vm.uiState.value.isDefaultHomeApp)
        assertTrue(vm.uiState.value.showDefaultLauncherPrompt)
        assertTrue(vm.uiState.value.allApps.any { it.id == "aura:default_home" })

        // Session dismiss ("Not Now") closes modal without permanently suppressing
        vm.dismissDefaultLauncherPrompt(dontAskAgain = false)
        assertFalse(vm.uiState.value.showDefaultLauncherPrompt)
        assertFalse(vm.uiState.value.suppressDefaultLauncherPrompt)

        // Re-opening manually shows prompt again
        vm.openDefaultLauncherPrompt()
        assertTrue(vm.uiState.value.showDefaultLauncherPrompt)

        // Accepting ROLE_HOME updates default launcher status and closes prompt
        shadowRole.addHeldRole(RoleManager.ROLE_HOME)
        vm.onDefaultHomeRoleResult()
        assertTrue(vm.uiState.value.isDefaultHomeApp)
        assertFalse(vm.uiState.value.showDefaultLauncherPrompt)

        // Permanent suppression ("Don't Ask Again") sets suppress flag
        vm.dismissDefaultLauncherPrompt(dontAskAgain = true)
        assertTrue(vm.uiState.value.suppressDefaultLauncherPrompt)

        // Pressing Home while Control Center / Edit mode are open resets to clean home state
        vm.openControlCenter()
        vm.toggleEditMode(true)
        val prevSignal = vm.uiState.value.homeNavigationSignal
        vm.onHomeButtonPressed()
        assertFalse(vm.uiState.value.isControlCenterOpen)
        assertFalse(vm.uiState.value.isEditMode)
        assertEquals(prevSignal + 1, vm.uiState.value.homeNavigationSignal)
    }
}
