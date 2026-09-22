package com.example.mycampuscomp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvThemeStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val layoutEditProfile = findViewById<LinearLayout>(R.id.layoutEditProfile)
        val layoutNotifications = findViewById<LinearLayout>(R.id.layoutNotifications)
        val layoutTheme = findViewById<LinearLayout>(R.id.layoutTheme)
        tvThemeStatus = findViewById(R.id.tvThemeStatus)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        // Sync current selected theme status text
        updateThemeStatusText()

        btnBack.setOnClickListener {
            onBackPressed()
        }

        layoutEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        layoutNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        layoutTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun updateThemeStatusText() {
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        tvThemeStatus.text = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            else -> "System Default"
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val currentThemeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val checkedItem = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedMode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                // Save selected mode in SharedPreferences
                sharedPreferences.edit().putInt("theme_mode", selectedMode).apply()

                // Apply theme mode dynamically
                AppCompatDelegate.setDefaultNightMode(selectedMode)
                
                updateThemeStatusText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
