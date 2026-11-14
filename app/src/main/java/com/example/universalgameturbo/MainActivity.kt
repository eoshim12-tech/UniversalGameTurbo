
package com.example.universalgameturbo

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.os.Build

class MainActivity : Activity() {

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 40, 32, 40)
            setBackgroundColor(Color.rgb(15, 18, 28))
        }

        val title = TextView(this).apply {
            text = "UNIVERSAL GAME TURBO"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val description = TextView(this).apply {
            text = "Gaming tools for your Tecno Spark 10 Pro"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 35)
        }

        val startButton = Button(this).apply {
            text = "ENABLE FLOATING SIDEBAR"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    val serviceIntent = Intent(
                        this@MainActivity,
                        FloatingSidebarService::class.java
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }

                    showMessage("Starting Game Turbo sidebar")
                }
            }
        }

        val stopButton = Button(this).apply {
            text = "STOP FLOATING SIDEBAR"
            setOnClickListener {
                stopService(
                    Intent(
                        this@MainActivity,
                        FloatingSidebarService::class.java
                    )
                )
                showMessage("Sidebar stopped")
            }
        }

        layout.addView(title)
        layout.addView(description)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }
}
