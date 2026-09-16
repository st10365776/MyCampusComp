package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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