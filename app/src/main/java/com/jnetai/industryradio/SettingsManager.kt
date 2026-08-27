package com.jnetai.industryradio

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Stores all app settings. Backed by SharedPreferences with sensible
 * defaults and full error logging so any failure is diagnosable.
 */
object SettingsManager {
    private const val PREFS_NAME = "industryradio_prefs"
    private const val KEY_SITE_URL = "site_url"
    private const val KEY_DARK_MODE = "dark_mode"

    const val DEFAULT_SITE_URL = "https://industryradio.co.uk"

    private lateinit var prefs: SharedPreferences
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        try {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            initialized = true
            applyTheme()
            DebugLogger.i("SettingsManager initialized")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.SETTINGS_INIT} Settings init failed", e)
        }
    }

    fun applyTheme() {
        try {
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            DebugLogger.d("Theme applied: darkMode=${isDarkMode()}")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.SETTINGS_INIT} Theme apply failed", e)
        }
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        applyTheme()
        DebugLogger.d("Dark mode set to: $enabled")
    }

    fun getSiteUrl(): String {
        val saved = prefs.getString(KEY_SITE_URL, null)
        if (saved.isNullOrBlank()) {
            DebugLogger.w("${ErrorCodes.SETTINGS_LOAD} No saved URL, using default")
            return DEFAULT_SITE_URL
        }
        return saved
    }

    fun setSiteUrl(url: String): Unit {
        val clean = url.trim().trimEnd('/')
        prefs.edit().putString(KEY_SITE_URL, clean).apply()
        DebugLogger.i("Site URL saved: $clean")
    }

    fun resetSiteUrl() {
        prefs.edit().remove(KEY_SITE_URL).apply()
        DebugLogger.i("Site URL reset to default")
    }

    fun isValidUrl(url: String): Boolean {
        return try {
            val u = android.net.Uri.parse(url)
            (u.scheme == "https" || u.scheme == "http") && u.host?.isNotBlank() == true
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.SETTINGS_LOAD} URL validation failed: $url", e)
            false
        }
    }
}