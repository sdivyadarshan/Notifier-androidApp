package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.NotificationRepository

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootCompletedReceiver", "Received action $action, checking always-on service state")
            try {
                val database = AppDatabase.getDatabase(context)
                val repository = NotificationRepository(database.notificationDao(), context)
                if (repository.isAlwaysOnServiceEnabled()) {
                    Log.d("BootCompletedReceiver", "Always-on service is enabled, starting in background")
                    AlwaysOnProtectionService.start(context)
                }
            } catch (e: Exception) {
                Log.e("BootCompletedReceiver", "Failed to start AlwaysOnProtectionService on boot", e)
            }
        }
    }
}
