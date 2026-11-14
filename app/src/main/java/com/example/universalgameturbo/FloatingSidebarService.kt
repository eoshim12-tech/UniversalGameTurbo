
package com.example.universalgameturbo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Context

class FloatingSidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubble: View? = null
    private var panel: View? = null

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(1001, createNotification())

        showBubble()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "game_turbo_channel",
                "Game Turbo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager =
                getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, "game_turbo_channel")
            } else {
                Notification.Builder(this)
            }

        return builder
            .setContentTitle("Universal Game Turbo")
            .setContentText("Floating gaming tools are running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    private fun overlayParams(width: Int, height: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            width,
            height,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = dp(180)
        }
    }

    private fun showBubble() {
        if (bubble != null) return

        val view = TextView(this).apply {
            text = "GT"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(35, 110, 230))
            elevation = dp(8).toFloat()
        }

        val params = overlayParams(dp(55), dp(55))

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()

                    if (kotlin.math.abs(dx) > dp(5) ||
                        kotlin.math.abs(dy) > dp(5)) {
                        moved = true
                    }

                    params.x = startX + dx
                    params.y = startY + dy
                    windowManager.updateViewLayout(view, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) showPanel()
                    true
                }

                else -> true
            }
        }

        bubble = view
        windowManager.addView(view, params)
    }

    private fun showPanel() {
        if (panel != null) {
            closePanel()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(Color.rgb(20, 25, 40))
            elevation = dp(12).toFloat()
        }

        val heading = TextView(this).apply {
            text = "UNIVERSAL GAME TURBO"
            textSize = 19f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, dp(14))
        }

        val info = TextView(this).apply {
            text = "Gaming sidebar active\n\nRAM and CPU information can be added in a future update."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(12))
        }

        val closeButton = Button(this).apply {
            text = "HIDE PANEL"
            setOnClickListener { closePanel() }
        }

        val stopButton = Button(this).apply {
            text = "STOP GAME TURBO"
            setOnClickListener { stopSelf() }
        }

        layout.addView(heading)
        layout.addView(info)
        layout.addView(closeButton)
        layout.addView(stopButton)

        val params = overlayParams(dp(270), WindowManager.LayoutParams.WRAP_CONTENT)
        params.x = dp(12)
        params.y = dp(240)

        panel = layout
        windowManager.addView(layout, params)
    }

    private fun closePanel() {
        panel?.let {
            windowManager.removeView(it)
        }
        panel = null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        closePanel()

        bubble?.let {
            windowManager.removeView(it)
        }
        bubble = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
