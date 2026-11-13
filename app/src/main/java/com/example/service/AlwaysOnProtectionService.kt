package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service that keeps the Notifier process pinned in memory with high priority,
 * preventing OEM task killers and Android Doze from putting the notification listener to sleep.
 * This ensures zero-lag, real-time dismissal of unwanted Instagram notifications even when
 * the app is closed or the device is locked.
 */
class AlwaysOnProtectionService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var watchdogJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        promoteToForeground()
        _isServiceRunning.value = true
        startWatchdog()
        InvisibleOverlayManager.attachOverlay(this)
        Log.d(TAG, "AlwaysOnProtectionService created with active watchdog and overlay")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            Log.d(TAG, "Stop action received for AlwaysOnProtectionService")
            watchdogJob?.cancel()
            InvisibleOverlayManager.detachOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            _isServiceRunning.value = false
            return START_NOT_STICKY
        }

        promoteToForeground()
        _isServiceRunning.value = true
        startWatchdog()
        InvisibleOverlayManager.attachOverlay(this)
        Log.d(TAG, "AlwaysOnProtectionService started in foreground")

        return START_STICKY
    }

    private fun promoteToForeground() {
        try {
            val notification = buildPersistentNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in promoteToForeground, fallback to legacy startForeground", e)
            try {
                val notification = buildPersistentNotification()
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                Log.e(TAG, "Fatal startForeground failure", e2)
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: app swiped from Recents. Re-binding listener & maintaining protection")
        InstagramNotificationListenerService.rebindService(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        watchdogJob?.cancel()
        serviceScope.cancel()
        InvisibleOverlayManager.detachOverlay()
        _isServiceRunning.value = false
        Log.d(TAG, "AlwaysOnProtectionService destroyed")
    }

    private fun startWatchdog() {
        if (watchdogJob?.isActive == true) return
        watchdogJob = serviceScope.launch {
            while (isActive) {
                delay(20000)
                try {
                    val isGranted = isNotificationListenerGranted(this@AlwaysOnProtectionService)
                    val isConnected = InstagramNotificationListenerService.isListenerConnected.value
                    if (isGranted && !isConnected) {
                        Log.d(TAG, "Watchdog: Listener disconnected by OEM power manager. Triggering rebind...")
                        InstagramNotificationListenerService.rebindService(this@AlwaysOnProtectionService)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Watchdog execution error", e)
                }
            }
        }
    }

    private fun isNotificationListenerGranted(context: Context): Boolean {
        return try {
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            flat?.contains(context.packageName) == true
        } catch (_: Exception) {
            false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifier Background Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Notifier running in the background for real-time Instagram notification filtering on HiOS."
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notifier Active Protection")
            .setContentText("Real-time Instagram filtering active in background")
            .setSmallIcon(R.drawable.ic_notification_shield)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val TAG = "AlwaysOnService"
        const val CHANNEL_ID = "notifier_always_on_shield"
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_SERVICE = "com.example.action.START_ALWAYS_ON"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_ALWAYS_ON"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, AlwaysOnProtectionService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AlwaysOnProtectionService", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlwaysOnProtectionService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop AlwaysOnProtectionService", e)
            }
        }
    }
}
