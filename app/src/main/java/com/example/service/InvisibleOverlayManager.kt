package com.example.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager

object InvisibleOverlayManager {
    private const val TAG = "InvisibleOverlay"
    private var windowManager: WindowManager? = null
    private var invisibleView: View? = null

    fun attachOverlay(context: Context) {
        try {
            if (!Settings.canDrawOverlays(context)) {
                Log.w(TAG, "Cannot attach overlay: SYSTEM_ALERT_WINDOW permission denied")
                return
            }

            if (invisibleView != null) return

            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            invisibleView = View(context).apply {
                alpha = 0f // completely invisible
                isFocusable = false
                isClickable = false
            }

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                1, 1, // 1x1 pixel
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }

            windowManager?.addView(invisibleView, params)
            Log.i(TAG, "Attached 1x1 invisible overlay to trick OEM aggressive battery managers")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach invisible overlay", e)
        }
    }

    fun detachOverlay() {
        try {
            if (invisibleView != null && windowManager != null) {
                windowManager?.removeView(invisibleView)
                invisibleView = null
                Log.i(TAG, "Detached invisible overlay")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detach invisible overlay", e)
        }
    }
}
