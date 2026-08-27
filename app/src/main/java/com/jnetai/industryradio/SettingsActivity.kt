package com.jnetai.industryradio

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_settings)

            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { finish() }

            val etSiteUrl = findViewById<TextInputEditText>(R.id.etSiteUrl)
            etSiteUrl.setText(SettingsManager.getSiteUrl())

            findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
                val url = etSiteUrl.text?.toString()?.trim()?.trimEnd('/') ?: ""
                when {
                    url.isBlank() -> {
                        Toast.makeText(this, R.string.url_required, Toast.LENGTH_SHORT).show()
                        DebugLogger.w("${ErrorCodes.SETTINGS_SAVE} Save rejected: blank URL")
                    }
                    !SettingsManager.isValidUrl(url) -> {
                        Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_LONG).show()
                        DebugLogger.w("${ErrorCodes.SETTINGS_SAVE} Save rejected: invalid URL $url")
                    }
                    else -> {
                        SettingsManager.setSiteUrl(url)
                        Toast.makeText(this, R.string.url_saved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

            findViewById<MaterialButton>(R.id.btnReset).setOnClickListener {
                SettingsManager.resetSiteUrl()
                etSiteUrl.setText(SettingsManager.getSiteUrl())
                Toast.makeText(this, R.string.url_reset, Toast.LENGTH_SHORT).show()
                DebugLogger.i("Site URL reset by user")
            }

            DebugLogger.i("SettingsActivity created")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.SETTINGS_UI} Settings onCreate failed: ${e.message}", e)
            Toast.makeText(this, "Settings failed to open", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}