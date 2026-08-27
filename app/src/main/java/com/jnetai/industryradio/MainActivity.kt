package com.jnetai.industryradio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private var autoStartConsumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            SettingsManager.init(applicationContext)
            setContentView(R.layout.activity_main)

            if (savedInstanceState != null) {
                autoStartConsumed = savedInstanceState.getBoolean("auto_start_consumed", false)
            }
            handleAutoStart()

            findViewById<MaterialButton>(R.id.btnOpenRadio).setOnClickListener {
                openRadio()
            }
            findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            findViewById<MaterialButton>(R.id.btnAbout).setOnClickListener {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            DebugLogger.i("MainActivity created")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.MAIN_UI} MainActivity onCreate failed: ${e.message}", e)
            Toast.makeText(this, "App failed to start", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun handleAutoStart() {
        if (autoStartConsumed) return
        autoStartConsumed = true
        try {
            DebugLogger.i("Auto-start opening radio site")
            openRadio()
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.MAIN_UI} Auto-start handling failed: ${e.message}", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("auto_start_consumed", autoStartConsumed)
    }

    private fun openRadio() {
        try {
            val url = SettingsManager.getSiteUrl()
            Intent(this, WebAppActivity::class.java).apply {
                putExtra("url", url)
            }.let { startActivity(it) }
            DebugLogger.i("Opening radio site: $url")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.NAVIGATE} Failed to open radio site", e)
            Toast.makeText(this, "Failed to open the site", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExitMenu() {
        try {
            val options = arrayOf(
                getString(R.string.goto_settings),
                getString(R.string.goto_about),
                getString(R.string.exit_app)
            )
            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_IndustryRadio_Dialog)
                .setTitle(R.string.app_name)
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            dialog.dismiss()
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }
                        1 -> {
                            dialog.dismiss()
                            startActivity(Intent(this, AboutActivity::class.java))
                        }
                        2 -> {
                            dialog.dismiss()
                            DebugLogger.i("User chose to exit app")
                            finishAffinity()
                        }
                    }
                }
                .setNegativeButton(R.string.back_to_app, null)
                .show()
            DebugLogger.d("Exit menu shown")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.EXIT_MENU} Failed to show exit menu", e)
            super.onBackPressed()
        }
    }

    override fun onBackPressed() {
        showExitMenu()
    }
}