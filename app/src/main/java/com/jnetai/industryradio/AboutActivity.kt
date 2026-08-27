package com.jnetai.industryradio

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AboutActivity : AppCompatActivity() {

    private lateinit var tvVersion: TextView
    private lateinit var btnCheckUpdates: MaterialButton

    private val repoReleasesUrl = "https://github.com/jnetai-clawbot/industryradio-app/releases"
    private val apiReleasesUrl = "https://api.github.com/repos/jnetai-clawbot/industryradio-app/releases/latest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_about)

            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { finish() }

            tvVersion = findViewById(R.id.tvVersion)
            btnCheckUpdates = findViewById(R.id.btnCheckUpdates)

            val tvMadeBy = findViewById<TextView>(R.id.tvMadeBy)
            tvMadeBy.setOnClickListener {
                openBrowser("https://jnetai.com/")
            }

            findViewById<MaterialButton>(R.id.btnShare).setOnClickListener { shareApp() }
            findViewById<MaterialButton>(R.id.btnVisitRepo).setOnClickListener { openBrowser(repoReleasesUrl) }
            btnCheckUpdates.setOnClickListener { checkForUpdates() }

            loadVersion()
            DebugLogger.i("AboutActivity created")
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.ABOUT_UI} About onCreate failed: ${e.message}", e)
            Toast.makeText(this, "About failed to open", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadVersion() {
        try {
            val pkgInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersion.text = String.format(getString(R.string.version), pkgInfo.versionName ?: "1.0.0")
        } catch (e: PackageManager.NameNotFoundException) {
            tvVersion.text = String.format(getString(R.string.version), "1.0.0")
            DebugLogger.e("${ErrorCodes.ABOUT_UI} Failed to load version", e)
        }
    }

    private fun checkForUpdates() {
        btnCheckUpdates.isEnabled = false
        btnCheckUpdates.text = getString(R.string.checking)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL(apiReleasesUrl).openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val response = try {
                    conn.inputStream.bufferedReader().readText()
                } catch (e: Exception) {
                    DebugLogger.e("${ErrorCodes.UPDATE_CHECK} API call failed", e)
                    null
                }

                withContext(Dispatchers.Main) {
                    btnCheckUpdates.isEnabled = true
                    btnCheckUpdates.text = getString(R.string.check_updates)

                    if (response == null) {
                        toast(getString(R.string.update_server_fail))
                        openBrowser(repoReleasesUrl)
                        return@withContext
                    }

                    try {
                        val json = JSONObject(response)
                        val latestVersion = json.optString("tag_name", "v1.0.0").removePrefix("v")
                        val currentVersion = try {
                            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
                        } catch (e: Exception) {
                            "1.0.0"
                        }

                        if (latestVersion != currentVersion) {
                            toast(getString(R.string.update_available, latestVersion, currentVersion))
                            openBrowser(repoReleasesUrl)
                        } else {
                            toast(getString(R.string.up_to_date, currentVersion))
                        }
                    } catch (e: Exception) {
                        DebugLogger.e("${ErrorCodes.UPDATE_PARSE} Failed to parse update response", e)
                        toast(getString(R.string.update_parse_fail))
                        openBrowser(repoReleasesUrl)
                    }
                }
            } catch (e: Exception) {
                DebugLogger.e("${ErrorCodes.UPDATE_CHECK} Check for updates failed", e)
                withContext(Dispatchers.Main) {
                    btnCheckUpdates.isEnabled = true
                    btnCheckUpdates.text = getString(R.string.check_updates)
                    toast(getString(R.string.update_check_fail))
                    openBrowser(repoReleasesUrl)
                }
            }
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, repoReleasesUrl))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.SHARE_FAIL} Share failed", e)
            toast(getString(R.string.share_fail))
        }
    }

    private fun openBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            DebugLogger.e("${ErrorCodes.NAVIGATE} Failed to open $url", e)
            toast(getString(R.string.no_browser))
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}