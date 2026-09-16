package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Map
        mapView = findViewById(R.id.mapView)

        // Logout button
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Bottom navigation
        val bottomNavigation =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Highlight Map because this is the Map Activity
        bottomNavigation.selectedItemId = R.id.nav_map

        // Bottom navigation
        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                // HOME
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

                // TIMETABLE
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

                // MAP
                R.id.nav_map -> {

                    true
                }

                // ASSIGNMENTS
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

                // MORE
                R.id.nav_more -> {
                    /*
                    startActivity(
                        Intent(
                            this,
                            SettingsActivity::class.java
                        )
                    )
                    finish()
                    */
                    true
                }

                else -> false
            }
        }

        // --------------------------------
        // MAP CONFIGURATION
        // --------------------------------

        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(
                    Point.fromLngLat(
                        25.57717897908619,
                        -33.951466489045124
                    )
                )
                .pitch(0.0)
                .zoom(18.0)
                .bearing(0.0)
                .build()
        )

        // Map overlays

        mapView.scalebar.marginTop = 200f

        mapView.logo.marginBottom = 140f

        mapView.attribution.marginBottom = 140f

        // --------------------------------
        // LOGOUT
        // --------------------------------

        btnLogout.setOnClickListener {

            FirebaseAuth
                .getInstance()
                .signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
                finish()
            }
        }
    }
}