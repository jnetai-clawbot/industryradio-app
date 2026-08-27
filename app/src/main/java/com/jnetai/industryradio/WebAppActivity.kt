package com.jnetai.industryradio

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WebAppActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private var currentUrl: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_web_app)

            currentUrl = intent.getStringExtra("url")
                ?: SettingsManager.getSiteUrl()

            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            toolbar.title = getString(R.string.app_name)
            toolbar.setNavigationOnClickListener { goBackOrExit() }

            progressBar = findViewById(R.id.progressBar)
            tvError = findViewById(R.id.tvError)
            webView = findViewById(R.id.webView)

            configureWebView()

            if (currentUrl.isBlank()) {
                showError("No site URL configured")
                DebugLogger.e("${ErrorCodes.WEBVIEW_INIT} No URL provided to WebAppActivity")
                return
            }

            loadSite()
            DebugLogger.i("WebAppActivity created for $currentUrl")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.WEBVIEW_INIT} onCreate failed: ${e.message}", e)
            Toast.makeText(this, "Failed to open site", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.userAgentString = settings.userAgentString.replace("; wv", "")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                DebugLogger.i("${ErrorCodes.WEBVIEW_GRANT} Permission request from ${request.origin}")
                val requested = request.resources
                val grantable = requested.filter {
                    it == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
                        it == PermissionRequest.RESOURCE_AUDIO_CAPTURE ||
                        it == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID
                }
                if (grantable.isNotEmpty()) {
                    request.grant(grantable.toTypedArray())
                } else {
                    request.deny()
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult
            ): Boolean {
                MaterialAlertDialogBuilder(this@WebAppActivity)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(message)
                    .setPositiveButton("OK") { _, _ -> result.confirm() }
                    .setCancelable(false)
                    .show()
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false
                }
                if (url.startsWith("intent:") || url.startsWith("market://")) {
                    openExternal(url)
                    return true
                }
                return false
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                if (url.startsWith("http://") || url.startsWith("https://")) return false
                openExternal(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                tvError.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                DebugLogger.d("Page finished: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    val code = error?.errorCode ?: -1
                    val desc = error?.description?.toString() ?: "Unknown error"
                    DebugLogger.e("${ErrorCodes.WEBVIEW_LOAD} Main frame error $code: $desc")
                    showError("Failed to load site (error $code)\nCheck your connection or the site URL in Settings.")
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (failingUrl == currentUrl) {
                    DebugLogger.e("${ErrorCodes.WEBVIEW_LOAD} Error $errorCode: $description for $failingUrl")
                    showError("Failed to load site (error $errorCode)\nCheck your connection or the site URL in Settings.")
                }
            }
        }
    }

    private fun loadSite() {
        try {
            tvError.visibility = View.GONE
            webView.loadUrl(currentUrl)
            DebugLogger.i("Loading $currentUrl")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.WEBVIEW_LOAD} Failed to load $currentUrl", e)
            showError("Failed to load site")
        }
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun openExternal(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.NAVIGATE} Failed to open external $url", e)
            Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExitMenu() {
        try {
            val options = arrayOf(
                getString(R.string.goto_settings),
                getString(R.string.goto_about),
                getString(R.string.exit_app)
            )
            MaterialAlertDialogBuilder(this)
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
            DebugLogger.d("Exit menu shown in WebAppActivity")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.EXIT_MENU} Failed to show exit menu", e)
            super.onBackPressed()
        }
    }

    override fun onBackPressed() {
        goBackOrExit()
    }

    private fun goBackOrExit() {
        if (webView.canGoBack()) {
            webView.goBack()
            DebugLogger.d("WebView back to history")
        } else {
            showExitMenu()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            webView.destroy()
        } catch (e: Exception) {
            DebugLogger.w("WebView destroy error: ${e.message}")
        }
    }
}