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
        // LOGOUT
        // --------------------------------------------------

        val btnLogout =
            findViewById<Button>(R.id.btnLogout)


        btnLogout.setOnClickListener {

            FirebaseAuth
                .getInstance()
                .signOut()

            val gso =
                GoogleSignInOptions
                    .Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                    )
                    .requestIdToken(
                        getString(
                            R.string.default_web_client_id
                        )
                    )
                    .requestEmail()
                    .build()

            GoogleSignIn
                .getClient(
                    this,
                    gso
                )
                .signOut()
                .addOnCompleteListener {

                    val intent =
                        Intent(
                            this,
                            LoginActivity::class.java
                        )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    finish()
                }
        }


        // --------------------------------------------------
        // BOTTOM NAVIGATION
        // --------------------------------------------------

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )


        // Highlight Map

        bottomNavigation.selectedItemId =
            R.id.nav_map


        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                // ------------------------------------------
                // HOME
                // ------------------------------------------

                R.id.nav_home -> {

                    startActivity(
                        Intent(
                            this,
                            DashboardActivity::class.java
                        )
                    )

                    finish()

                    true
                }


                // ------------------------------------------
                // TIMETABLE
                // ------------------------------------------

                R.id.nav_timetable -> {

                    startActivity(
                        Intent(
                            this,
                            TimetableActivity::class.java
                        )
                    )

                    finish()

                    true
                }


                // ------------------------------------------
                // MAP
                // ------------------------------------------

                R.id.nav_map -> {

                    true
                }


                // ------------------------------------------
                // ASSIGNMENTS
                // ------------------------------------------

                R.id.nav_assignments -> {

                    startActivity(
                        Intent(
                            this,
                            AssignmentsActivity::class.java
                        )
                    )

                    finish()

                    true
                }


                // ------------------------------------------
                // MARKETPLACE
                // ------------------------------------------

                R.id.nav_marketplace -> {

                    startActivity(
                        Intent(
                            this,
                            MarketplaceActivity::class.java
                        )
                    )

                    finish()

                    true
                }


                // ------------------------------------------
                // AI STUDY ASSISTANT
                // ------------------------------------------

                R.id.nav_ai -> {

                    startActivity(
                        Intent(
                            this,
                            AIStudyAssistantActivity::class.java
                        )
                    )

                    true
                }


                else -> {

                    false
                }
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