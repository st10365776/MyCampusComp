package com.example.mycampuscomp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme mode before layout inflation
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val savedThemeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedThemeMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({

            if(auth.currentUser != null){

                startActivity(Intent(this, MainActivity::class.java))

            }else{

                startActivity(Intent(this, OnboardingActivity::class.java))

            }

            finish()

        },5000)

    }
}