package com.jnetai.industryradio

/**
 * Central list of error codes used across the app so any failure, exception,
 * or unexpected state can be identified by a stable code and matched to its
 * cause quickly. These codes are intentionally permanent.
 */
object ErrorCodes {
    val SETTINGS_INIT = DebugLogger.getErrorCode("Settings", "Init")
    val SETTINGS_SAVE = DebugLogger.getErrorCode("Settings", "Save")
    val SETTINGS_LOAD = DebugLogger.getErrorCode("Settings", "Load")
    val SETTINGS_UI = DebugLogger.getErrorCode("SettingsUi", "Ui")

    val WEBVIEW_INIT = DebugLogger.getErrorCode("WebView", "Init")
    val WEBVIEW_LOAD = DebugLogger.getErrorCode("WebView", "Load")
    val WEBVIEW_GRANT = DebugLogger.getErrorCode("WebView", "PermissionGrant")

    val UPDATE_CHECK = DebugLogger.getErrorCode("Update", "Check")
    val UPDATE_PARSE = DebugLogger.getErrorCode("Update", "Parse")
    val SHARE_FAIL = DebugLogger.getErrorCode("Share", "Fail")
    val MAIN_UI = DebugLogger.getErrorCode("Main", "Ui")
    val ABOUT_UI = DebugLogger.getErrorCode("AboutUi", "Ui")
    val NAVIGATE = DebugLogger.getErrorCode("Nav", "Open")
    val EXIT_MENU = DebugLogger.getErrorCode("ExitMenu", "Show")
}