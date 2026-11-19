package com.example.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ActiveSheet
import com.example.model.FocusMode
import com.example.model.HomeFolder
import com.example.model.HomeWidget
import com.example.model.LauncherApp
import com.example.model.WidgetSize
import com.example.ui.components.AppIconTile
import com.example.ui.components.AppLibraryPage
import com.example.ui.components.CategoryFolderModal
import com.example.ui.components.ControlCenterOverlay
import com.example.ui.components.CupertinoStatusBar
import com.example.ui.components.CustomizableDock
import com.example.ui.components.DefaultLauncherPromptOverlay
import com.example.ui.components.DynamicWallpaperBackground
import com.example.ui.components.FolderIconTile
import com.example.ui.components.FolderModalOverlay
import com.example.ui.components.HomeWidgetCard
import com.example.ui.components.LauncherBottomSheets
import com.example.ui.components.LockScreenOverlay
import com.example.ui.components.SpotlightSearchSheet
import com.example.ui.theme.AuraBlue
import com.example.viewmodel.LauncherUiState
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@Composable
fun LauncherRootScreen(
    viewModel: LauncherViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val indexSnapshot by viewModel.appIndexState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSystemDndStatus()
                viewModel.refreshDefaultHomeStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage(pagerState.currentPage)
    }

    LaunchedEffect(state.homeNavigationSignal) {
        if (state.homeNavigationSignal > 0 && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        }
    }

    val defaultHomeRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onDefaultHomeRoleResult()
    }

    val launchDefaultHomeFlow: () -> Unit = {
        val roleIntent = viewModel.buildDefaultHomeRoleIntent()
        if (roleIntent != null) {
            runCatching { defaultHomeRoleLauncher.launch(roleIntent) }
                .onFailure { viewModel.openFallbackHomeSettings() }
        } else {
            viewModel.openFallbackHomeSettings()
        }
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }

    // Zero-permission Android Photo Picker for Photo Frame widget
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.updatePhotoWidgetUri(uri.toString())
        }
    }

    // Back gesture handling across overlays, edit mode, and pages
    BackHandler(
        enabled = state.showDefaultLauncherPrompt ||
            state.activeSheet != ActiveSheet.NONE ||
            state.activeFolder != null ||
            state.activeCategoryFolder != null ||
            state.isControlCenterOpen ||
            state.isSpotlightOpen ||
            state.isEditMode ||
            state.isLockScreenVisible ||
            pagerState.currentPage != 0
    ) {
        when {
            state.showDefaultLauncherPrompt -> viewModel.dismissDefaultLauncherPrompt(dontAskAgain = false)
            state.activeSheet != ActiveSheet.NONE -> viewModel.closeSheet()
            state.activeFolder != null -> viewModel.openFolder(null)
            state.activeCategoryFolder != null -> viewModel.openCategoryFolder(null)
            state.isControlCenterOpen -> viewModel.closeControlCenter()
            state.isSpotlightOpen -> viewModel.closeSpotlight()
            state.isLockCustomizerOpen -> viewModel.setLockCustomizerOpen(false)
            state.isLockScreenVisible -> viewModel.unlockLockScreen()
            state.isEditMode -> viewModel.toggleEditMode(false)
            pagerState.currentPage != 0 -> scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    val allAppsById = remember(state.allApps) { state.allApps.associateBy { it.id } }
    val dockApps = remember(state.dockAppIds, allAppsById) {
        state.dockAppIds.mapNotNull { allAppsById[it] }
    }
    val homeApps = remember(state.homeAppIds, allAppsById) {
        state.homeAppIds.mapNotNull { allAppsById[it] }
    }

    val isBlurredOverlay = state.showDefaultLauncherPrompt ||
        state.isControlCenterOpen ||
        state.isSpotlightOpen ||
        state.activeFolder != null ||
        state.activeCategoryFolder != null

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("launcher_root")
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // 1. Dynamic Time-of-Day Wallpaper Backdrop
        DynamicWallpaperBackground(
            phase = state.activePhase,
            isDimmed = isBlurredOverlay,
            isBlurred = isBlurredOverlay,
            pageParallaxOffset = (pagerState.currentPage + pagerState.currentPageOffsetFraction) / 2f
        )

        // 2. Main Home Screen + App Library Pager
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.isLockScreenVisible, state.isControlCenterOpen, state.isSpotlightOpen) {
                    if (!state.isLockScreenVisible && !state.isControlCenterOpen && !state.isSpotlightOpen) {
                        var startX = 0f
                        var startY = 0f
                        var totalDragY = 0f
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                startX = offset.x
                                startY = offset.y
                                totalDragY = 0f
                            },
                            onDragEnd = {
                                if (totalDragY > 110f) {
                                    when {
                                        startY < screenHeightPx * 0.35f && startX > screenWidthPx * 0.60f -> {
                                            viewModel.openControlCenter()
                                        }
                                        startY < screenHeightPx * 0.35f && startX < screenWidthPx * 0.35f -> {
                                            viewModel.openLockScreen()
                                        }
                                        else -> {
                                            viewModel.openSpotlight()
                                        }
                                    }
                                }
                                totalDragY = 0f
                            },
                            onVerticalDrag = { _, dragAmount ->
                                totalDragY += dragAmount
                            }
                        )
                    }
                }
        ) {
            // Top Cupertino Status Bar & Dynamic Pill
            CupertinoStatusBar(
                timeText = state.currentTimeText,
                controlCenter = state.controlCenter,
                telemetry = state.telemetry,
                isDynamicIslandExpanded = state.isDynamicIslandExpanded,
                onToggleDynamicIsland = viewModel::toggleDynamicIsland,
                onTogglePlayPause = viewModel::togglePlayPause,
                onOpenControlCenter = viewModel::openControlCenter,
                onOpenLockScreen = { viewModel.openLockScreen() }
            )

            // Edit Mode Toolbar
            AnimatedVisibility(
                visible = state.isEditMode && pagerState.currentPage < 2,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                EditModeActionBar(
                    onAddWidget = { viewModel.openSheet(ActiveSheet.WIDGET_PICKER) },
                    onNewFolder = { viewModel.openSheet(ActiveSheet.CREATE_FOLDER) },
                    onWallpapers = { viewModel.openSheet(ActiveSheet.WALLPAPER_STUDIO) },
                    onDone = { viewModel.toggleEditMode(false) }
                )
            }

            // Pager Content (Pages 0..1 = Home Screens, Page 2 = App Library)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("home_pager")
            ) { page ->
                if (page < 2) {
                    val pageWidgets = state.homeWidgets.filter { it.pageIndex == page }
                    val pageFolders = state.homeFolders.filter { it.pageIndex == page }
                    val pageApps = if (page == 0) homeApps.take(12) else homeApps.drop(12)

                    HomeScreenPage(
                        widgets = pageWidgets,
                        folders = pageFolders,
                        apps = pageApps,
                        allAppsById = allAppsById,
                        state = state,
                        drawableResolver = viewModel::getAppDrawable,
                        onAppClick = viewModel::onAppClick,
                        onFolderClick = { viewModel.openFolder(it) },
                        onToggleEditMode = { viewModel.toggleEditMode(!state.isEditMode) },
                        onRemoveApp = viewModel::removeAppFromHome,
                        onRemoveFolder = viewModel::deleteFolder,
                        onRemoveWidget = viewModel::removeWidget,
                        onToggleNote = viewModel::toggleQuickNote,
                        onAddNoteRequested = { showAddNoteDialog = true },
                        onPickPhotoRequested = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onOpenWallpaperStudio = { viewModel.openSheet(ActiveSheet.WALLPAPER_STUDIO) },
                        onToggleDnd = viewModel::toggleDoNotDisturb,
                        onCycleFocusMode = viewModel::cycleFocusMode,
                        onToggleFocusGroup = viewModel::toggleFocusAllowedGroup,
                        onOpenDndPolicySettings = viewModel::openDndPolicySettings
                    )
                } else {
                    AppLibraryPage(
                        allApps = state.allApps,
                        indexSnapshot = indexSnapshot,
                        searchIndexedApps = { q -> viewModel.searchIndexedApps(q) },
                        drawableResolver = viewModel::getAppDrawable,
                        onAppClick = viewModel::onAppClick,
                        onAddToHome = viewModel::addAppToHome,
                        onOpenCategory = viewModel::openCategoryFolder,
                        onRefreshIndex = viewModel::refreshAppIndex,
                        onToggleDnd = viewModel::toggleDoNotDisturb,
                        onOpenControlCenter = viewModel::openControlCenter,
                        onOpenWallpaperStudio = { viewModel.openSheet(ActiveSheet.WALLPAPER_STUDIO) }
                    )
                }
            }

            // Page Indicator Dots + Search / Quick Controls Bar (hidden on App Library page)
            if (pagerState.currentPage < 2) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    // Search Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(0.75.dp, Color.White.copy(alpha = 0.34f), RoundedCornerShape(16.dp))
                            .clickable(onClick = viewModel::openSpotlight)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("home_search_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Spotlight Search",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Search",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Animated page indicator dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        repeat(3) { index ->
                            val selected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (selected) 16.dp else 7.dp,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "dot_width"
                            )
                            if (index == 2) {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = "App Library page",
                                    tint = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clickable { scope.launch { pagerState.animateScrollToPage(2) } }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(width = width, height = 7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) Color.White else Color.White.copy(alpha = 0.45f)
                                        )
                                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Quick Control Center pill button (great accessibility + discoverability)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(0.75.dp, Color.White.copy(alpha = 0.34f), RoundedCornerShape(16.dp))
                            .clickable(onClick = viewModel::openControlCenter)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("home_controls_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Open Control Center",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Customizable Floating Glass Dock
                CustomizableDock(
                    dockApps = dockApps,
                    dockStyle = state.dockStyle,
                    isEditMode = state.isEditMode,
                    drawableResolver = viewModel::getAppDrawable,
                    onAppClick = viewModel::onAppClick,
                    onAppLongClick = { viewModel.toggleEditMode(true) },
                    onRemoveFromDock = viewModel::toggleDockApp,
                    onOpenDockCustomizer = { viewModel.openSheet(ActiveSheet.DOCK_CUSTOMIZER) }
                )
            } else {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        // 3. Expanded Home Folder Modal
        FolderModalOverlay(
            folder = state.activeFolder,
            allAppsById = allAppsById,
            drawableResolver = viewModel::getAppDrawable,
            isEditMode = state.isEditMode,
            onDismiss = { viewModel.openFolder(null) },
            onAppClick = viewModel::onAppClick,
            onRenameFolder = viewModel::renameFolder,
            onDeleteFolder = viewModel::deleteFolder,
            onRemoveAppFromFolder = viewModel::removeAppFromFolder
        )

        // 4. Expanded App Library Category Folder Modal
        CategoryFolderModal(
            category = state.activeCategoryFolder,
            allApps = state.allApps,
            drawableResolver = viewModel::getAppDrawable,
            onDismiss = { viewModel.openCategoryFolder(null) },
            onAppClick = viewModel::onAppClick,
            onAddToHome = viewModel::addAppToHome
        )

        // 5. Spotlight Search Overlay
        SpotlightSearchSheet(
            visible = state.isSpotlightOpen,
            query = state.spotlightQuery,
            allApps = state.allApps,
            searchIndexedApps = { q -> viewModel.searchIndexedApps(q) },
            drawableResolver = viewModel::getAppDrawable,
            onQueryChange = viewModel::updateSpotlightQuery,
            onDismiss = viewModel::closeSpotlight,
            onAppClick = viewModel::onAppClick,
            onToggleDnd = viewModel::toggleDoNotDisturb,
            onToggleFlashlight = viewModel::toggleFlashlight,
            onOpenControlCenter = viewModel::openControlCenter,
            onOpenLockScreen = { viewModel.openLockScreen() },
            onOpenWallpaperStudio = { viewModel.openSheet(ActiveSheet.WALLPAPER_STUDIO) },
            onOpenWidgetPicker = { viewModel.openSheet(ActiveSheet.WIDGET_PICKER) },
            isDefaultHomeApp = state.isDefaultHomeApp,
            onOpenDefaultLauncherPrompt = viewModel::openDefaultLauncherPrompt
        )

        // 6. Control Center Overlay
        ControlCenterOverlay(
            visible = state.isControlCenterOpen,
            state = state.controlCenter,
            silencedNotificationCount = state.silencedNotificationCount,
            onDismiss = viewModel::closeControlCenter,
            onToggleWifi = viewModel::toggleWifi,
            onOpenWifiPanel = viewModel::openWifiSystemPanel,
            onToggleBluetooth = viewModel::toggleBluetooth,
            onOpenBluetoothSettings = viewModel::openBluetoothSystemSettings,
            onToggleAirplane = viewModel::toggleAirplaneMode,
            onToggleCellular = viewModel::toggleCellular,
            onTogglePlayPause = viewModel::togglePlayPause,
            onSkipNext = viewModel::skipNextTrack,
            onSkipPrev = viewModel::skipPreviousTrack,
            onBrightnessChange = { viewModel.setBrightness(activity, it) },
            onVolumeChange = viewModel::setMediaVolume,
            onSliderDetent = { viewModel.haptics.tapLight() },
            onToggleOrientation = viewModel::toggleOrientationLock,
            onCycleFocus = viewModel::cycleFocusMode,
            onToggleFlashlight = viewModel::toggleFlashlight,
            onOpenCamera = viewModel::launchSystemCamera,
            onOpenCalculator = viewModel::launchSystemCalculator,
            onOpenTimer = viewModel::launchSystemTimer,
            onOpenWallpaperStudio = { viewModel.openSheet(ActiveSheet.WALLPAPER_STUDIO) },
            onOpenLockScreen = { viewModel.openLockScreen() },
            isDefaultHomeApp = state.isDefaultHomeApp,
            onOpenDefaultHomePrompt = viewModel::openDefaultLauncherPrompt
        )

        // 7. Lock Screen Overlay (with Notification Stacks & Custom Clock Fonts)
        LockScreenOverlay(
            visible = state.isLockScreenVisible,
            timeText = state.currentTimeText,
            dateText = state.currentDateText,
            telemetry = state.telemetry,
            fontStyle = state.lockFontStyle,
            clockTint = state.lockClockTint,
            focusMode = state.controlCenter.focusMode,
            notifications = state.visibleNotifications,
            silencedCount = state.silencedNotificationCount,
            showingSilenced = state.showSilencedNotifications,
            expandedGroups = state.expandedNotificationGroups,
            isCustomizerOpen = state.isLockCustomizerOpen,
            showLockOnStart = state.showLockScreenOnStart,
            flashlightOn = state.controlCenter.flashlightOn,
            onUnlock = viewModel::unlockLockScreen,
            onToggleCustomizer = viewModel::setLockCustomizerOpen,
            onSelectFontStyle = viewModel::setLockFontStyle,
            onSelectClockTint = viewModel::setLockClockTint,
            onToggleShowOnStart = viewModel::setShowLockScreenOnStart,
            onToggleShowSilenced = viewModel::toggleShowSilencedNotifications,
            onToggleGroupExpanded = viewModel::toggleNotificationGroupExpanded,
            onDismissNotification = viewModel::dismissNotification,
            onClearGroup = viewModel::clearNotificationGroup,
            onAddSampleNotification = viewModel::addSampleNotification,
            onToggleFlashlight = viewModel::toggleFlashlight,
            onOpenCamera = viewModel::launchSystemCamera
        )

        // 8. Bottom Sheets (Wallpaper Studio, Widget Picker, Dock Customizer, Folder Creator)
        LauncherBottomSheets(
            activeSheet = state.activeSheet,
            wallpaperMode = state.wallpaperMode,
            activePhase = state.activePhase,
            dockStyle = state.dockStyle,
            dockAppIds = state.dockAppIds,
            allApps = state.allApps,
            drawableResolver = viewModel::getAppDrawable,
            onDismiss = viewModel::closeSheet,
            onSelectWallpaperMode = viewModel::setWallpaperMode,
            onAddWidget = viewModel::addWidget,
            onSelectDockStyle = viewModel::setDockStyle,
            onToggleDockApp = viewModel::toggleDockApp,
            onCreateFolder = viewModel::createFolder,
            onOpenCreateFolderSheet = { viewModel.openSheet(ActiveSheet.CREATE_FOLDER) }
        )

        // 8.5. Default Launcher / Home Replacement Prompt on Launch
        DefaultLauncherPromptOverlay(
            visible = state.showDefaultLauncherPrompt,
            onSetAsDefault = launchDefaultHomeFlow,
            onDismissSession = { viewModel.dismissDefaultLauncherPrompt(dontAskAgain = false) },
            onDontAskAgain = { viewModel.dismissDefaultLauncherPrompt(dontAskAgain = true) }
        )

        // 9. Transient Feedback Pill Banner
        AnimatedVisibility(
            visible = state.statusBannerMessage != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 84.dp)
        ) {
            state.statusBannerMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xE60F172A))
                        .border(0.8.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(22.dp))
                        .clickable(onClick = viewModel::dismissBanner)
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 10. Quick Note Dialog for Reminders Widget
        if (showAddNoteDialog) {
            AlertDialog(
                onDismissRequest = { showAddNoteDialog = false },
                title = { Text("New Reminder") },
                text = {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text("Enter quick reminder...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.addQuickNote(newNoteText)
                            newNoteText = ""
                            showAddNoteDialog = false
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddNoteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun EditModeActionBar(
    onAddWidget: () -> Unit,
    onNewFolder: () -> Unit,
    onWallpapers: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .testTag("edit_mode_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditActionPill(
                icon = Icons.Rounded.Add,
                label = "Widget",
                onClick = onAddWidget,
                testTag = "edit_add_widget_button"
            )
            EditActionPill(
                icon = Icons.Rounded.CreateNewFolder,
                label = "Folder",
                onClick = onNewFolder,
                testTag = "edit_new_folder_button"
            )
            EditActionPill(
                icon = Icons.Rounded.Wallpaper,
                label = "Backdrop",
                onClick = onWallpapers,
                testTag = "edit_wallpaper_button"
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .clickable(onClick = onDone)
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("edit_done_button")
        ) {
            Text(
                text = "Done",
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EditActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.24f))
            .border(0.75.dp, Color.White.copy(alpha = 0.36f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenPage(
    widgets: List<HomeWidget>,
    folders: List<HomeFolder>,
    apps: List<LauncherApp>,
    allAppsById: Map<String, LauncherApp>,
    state: LauncherUiState,
    drawableResolver: (String) -> Drawable?,
    onAppClick: (LauncherApp) -> Unit,
    onFolderClick: (HomeFolder) -> Unit,
    onToggleEditMode: () -> Unit,
    onRemoveApp: (String) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onToggleNote: (String) -> Unit,
    onAddNoteRequested: () -> Unit,
    onPickPhotoRequested: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onToggleDnd: () -> Unit,
    onCycleFocusMode: () -> Unit,
    onToggleFocusGroup: (String) -> Unit,
    onOpenDndPolicySettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (state.isEditMode) onToggleEditMode()
                },
                onLongClick = {
                    if (!state.isEditMode) onToggleEditMode()
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
        ) {
            // 1. Widgets (Small 2x2 spans 2 columns, Medium 4x2 spans 4 columns)
            itemsIndexed(
                items = widgets,
                key = { _, w -> w.id },
                span = { _, w ->
                    GridItemSpan(if (w.size == WidgetSize.MEDIUM_4X2) 4 else 2)
                }
            ) { _, widget ->
                HomeWidgetCard(
                    widget = widget,
                    phase = state.activePhase,
                    telemetry = state.telemetry,
                    quickNotes = state.quickNotes,
                    photoWidgetUri = state.photoWidgetUri,
                    currentHour = state.currentHour,
                    currentDateText = state.currentDateText,
                    controlCenter = state.controlCenter,
                    silencedNotificationCount = state.silencedNotificationCount,
                    isEditMode = state.isEditMode,
                    onLongClick = { if (!state.isEditMode) onToggleEditMode() },
                    onRemove = { onRemoveWidget(widget.id) },
                    onToggleNote = onToggleNote,
                    onAddNoteRequested = onAddNoteRequested,
                    onPickPhotoRequested = onPickPhotoRequested,
                    onOpenWallpaperStudio = onOpenWallpaperStudio,
                    onToggleDnd = onToggleDnd,
                    onCycleFocusMode = onCycleFocusMode,
                    onToggleFocusGroup = onToggleFocusGroup,
                    onOpenDndPolicySettings = onOpenDndPolicySettings
                )
            }

            // 2. Home Screen Folders
            itemsIndexed(
                items = folders,
                key = { _, f -> f.id }
            ) { _, folder ->
                val folderApps = folder.appIds.mapNotNull { allAppsById[it] }
                FolderIconTile(
                    folder = folder,
                    apps = folderApps,
                    drawableResolver = drawableResolver,
                    isEditMode = state.isEditMode,
                    onClick = { onFolderClick(folder) },
                    onLongClick = { if (!state.isEditMode) onToggleEditMode() },
                    onRemoveClick = { onRemoveFolder(folder.id) }
                )
            }

            // 3. Home Screen App Icons (dim notification badges when Focus Mode silences their group)
            itemsIndexed(
                items = apps,
                key = { _, a -> a.id }
            ) { idx, app ->
                val isFocusFiltered = state.controlCenter.focusMode != FocusMode.OFF &&
                    ((app.iconKey == "messages" && "messages" !in state.controlCenter.focusAllowedGroups) ||
                        (app.iconKey == "calendar" && "calendar" !in state.controlCenter.focusAllowedGroups) ||
                        (app.iconKey == "phone" && state.controlCenter.focusMode == FocusMode.DO_NOT_DISTURB))
                val displayedApp = if (isFocusFiltered && app.badgeCount > 0) {
                    app.copy(badgeCount = 0)
                } else {
                    app
                }

                AppIconTile(
                    app = displayedApp,
                    drawable = drawableResolver(app.id),
                    isEditMode = state.isEditMode,
                    jigglePhaseOffset = idx.toFloat(),
                    onClick = { onAppClick(app) },
                    onLongClick = { if (!state.isEditMode) onToggleEditMode() },
                    onRemoveClick = { onRemoveApp(app.id) }
                )
            }
        }
    }
}
