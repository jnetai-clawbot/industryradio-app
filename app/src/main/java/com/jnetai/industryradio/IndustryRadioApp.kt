package com.jnetai.industryradio

import android.app.Application

/**
 * Applies the theme (dark mode) before any activity is created so every
 * activity, dialog, and menu renders with the intended colors from the
 * very first frame with no mid-launch theme recreation glitches.
 */
class IndustryRadioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            SettingsManager.init(applicationContext)
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.SETTINGS_INIT} Application init failed", e)
        }
    }
}