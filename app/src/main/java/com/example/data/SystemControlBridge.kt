package com.example.data

import android.app.Activity
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import androidx.core.content.getSystemService

class SystemControlBridge(private val context: Context) {

    private val cameraManager: CameraManager? = context.getSystemService()
    private val audioManager: AudioManager? = context.getSystemService()
    private val connectivityManager: ConnectivityManager? = context.getSystemService()
    private val notificationManager: NotificationManager? = context.getSystemService()

    private var torchCameraId: String? = null

    init {
        torchCameraId = findTorchCameraId()
    }

    private fun findTorchCameraId(): String? {
        val cm = cameraManager ?: return null
        return runCatching {
            cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    fun isFlashlightAvailable(): Boolean = torchCameraId != null

    fun setFlashlight(enabled: Boolean): Boolean {
        val cm = cameraManager ?: return false
        val id = torchCameraId ?: return false
        return runCatching {
            cm.setTorchMode(id, enabled)
            true
        }.getOrDefault(false)
    }

    fun getNormalizedMusicVolume(): Float {
        val am = audioManager ?: return 0.5f
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    }

    fun setNormalizedMusicVolume(fraction: Float) {
        val am = audioManager ?: return
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val target = (fraction.coerceIn(0f, 1f) * max).toInt()
        runCatching {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }

    fun isMusicActive(): Boolean = runCatching {
        audioManager?.isMusicActive == true
    }.getOrDefault(false)

    fun dispatchMediaKey(keyCode: Int) {
        val am = audioManager ?: return
        val now = SystemClock.uptimeMillis()
        runCatching {
            am.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
            am.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
        }
    }

    fun applyWindowBrightness(activity: Activity?, fraction: Float) {
        val window = activity?.window ?: return
        val params = window.attributes
        params.screenBrightness = fraction.coerceIn(0.05f, 1.0f)
        window.attributes = params
    }

    fun readBatteryTelemetry(): Pair<Int, Boolean> {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val statusIntent = context.registerReceiver(null, intentFilter)
        val level = statusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = statusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 86
        val status = statusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        return pct to isCharging
    }

    fun readNetworkState(): Pair<Boolean, Boolean> {
        val cm = connectivityManager ?: return true to true
        val activeNetwork = cm.activeNetwork ?: return false to false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false to false
        val wifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val cellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        return wifi to cellular
    }

    fun openWifiPanel() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        launchPanel(intent)
    }

    fun openBluetoothSettings() {
        launchPanel(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun openDisplaySettings() {
        launchPanel(Intent(Settings.ACTION_DISPLAY_SETTINGS))
    }

    fun openCamera() {
        launchPanel(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
    }

    fun openCalculator() {
        launchPanel(
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALCULATOR)
        )
    }

    fun isDndPolicyAccessGranted(): Boolean = runCatching {
        notificationManager?.isNotificationPolicyAccessGranted == true
    }.getOrDefault(false)

    fun isSystemDndActive(): Boolean = runCatching {
        val filter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        filter != NotificationManager.INTERRUPTION_FILTER_ALL
    }.getOrDefault(false)

    fun setSystemDnd(enabled: Boolean): Boolean {
        val nm = notificationManager ?: return false
        if (!isDndPolicyAccessGranted()) return false
        return runCatching {
            val target = if (enabled) {
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            nm.setInterruptionFilter(target)
            true
        }.getOrDefault(false)
    }

    fun openDndPolicyAccessSettings() {
        launchPanel(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    fun isDefaultHomeApp(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleAvailableAndHeld = runCatching {
                val roleManager = context.getSystemService<RoleManager>()
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                } else {
                    null
                }
            }.getOrNull()
            if (roleAvailableAndHeld != null) return roleAvailableAndHeld
        }

        return runCatching {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val pm = context.packageManager
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.resolveActivity(
                    homeIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            resolveInfo?.activityInfo?.packageName == context.packageName
        }.getOrDefault(false)
    }

    fun createDefaultHomeRoleIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return runCatching {
                val roleManager = context.getSystemService<RoleManager>()
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                } else {
                    null
                }
            }.getOrNull()
        }
        return null
    }

    fun openDefaultHomeSettings(): Boolean {
        if (launchPanel(Intent(Settings.ACTION_HOME_SETTINGS))) return true
        return launchPanel(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }

    fun openTimer() {
        val intent = Intent(AlarmClock.ACTION_SHOW_TIMERS)
        if (!launchPanel(intent)) {
            launchPanel(Intent(AlarmClock.ACTION_SET_TIMER))
        }
    }

    private fun launchPanel(intent: Intent): Boolean {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
