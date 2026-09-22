package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mapWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // --------------------------------------------------
        // MAP
        // --------------------------------------------------

        mapWebView = findViewById(R.id.mapWebView)

        setupWebView()


        // --------------------------------------------------
        // BOTTOM NAVIGATION
        // --------------------------------------------------

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_timetable -> {
                    startActivity(Intent(this, TimetableActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ai -> {
                    startActivity(Intent(this, AIStudyAssistantActivity::class.java))
                    true
                }
                R.id.nav_assignments -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_more -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("open_drawer", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }


    // ======================================================
    // MAPPEDIN WEBVIEW
    // ======================================================

    private fun setupWebView() {

        mapWebView.settings.apply {

            javaScriptEnabled = true

            domStorageEnabled = true

            loadWithOverviewMode = true

            useWideViewPort = true
        }


        mapWebView.webViewClient =
            object : WebViewClient() {

                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: android.webkit.RenderProcessGoneDetail?
                ): Boolean {

                    /*
                     * Reload the Mappedin map if the WebView
                     * renderer crashes.
                     */

                    view?.loadUrl(
                        "https://app.mappedin.com/map/6a6fa4cb81d0f1000af1aaf4?embedded=true"
                    )

                    return true
                }
            }


        mapWebView.loadUrl(
            "https://app.mappedin.com/map/6a6fa4cb81d0f1000af1aaf4?embedded=true"
        )
    }
}