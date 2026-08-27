package com.jnetai.industryradio

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Persistent, in-memory diagnostic logging used across the whole app.
 * Every failure generates a timestamped, tagged log entry plus (where
 * applicable) a full stack trace so problems can be traced quickly.
 */
object DebugLogger {
    const val TAG = "IndustryRadio"
    private const val MAX_HISTORY = 500
    private val logHistory = mutableListOf<String>()

    fun log(level: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(java.util.Date())
        val sb = StringBuilder()
        sb.append("[$timestamp] [$level] $message")
        if (throwable != null) {
            sb.append("\n${getStackTraceString(throwable)}")
        }
        val entry = sb.toString()
        synchronized(logHistory) {
            logHistory.add(entry)
            if (logHistory.size > MAX_HISTORY) logHistory.removeAt(0)
        }
        when (level) {
            "ERROR" -> Log.e(TAG, entry)
            "WARN" -> Log.w(TAG, entry)
            "DEBUG" -> Log.d(TAG, entry)
            else -> Log.i(TAG, entry)
        }
    }

    fun d(message: String) = log("DEBUG", message)
    fun i(message: String) = log("INFO", message)
    fun w(message: String) = log("WARN", message)
    fun e(message: String, throwable: Throwable? = null) = log("ERROR", message, throwable)

    fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    /** Generates a stable-ish error code for a given scope/type. */
    fun getErrorCode(scope: String, type: String): String {
        return "ERR_${scope.uppercase(Locale.US)}_${type.uppercase(Locale.US)}"
    }

    fun getLogHistory(): List<String> = synchronized(logHistory) { logHistory.toList() }

    fun clearHistory() = synchronized(logHistory) { logHistory.clear() }
}