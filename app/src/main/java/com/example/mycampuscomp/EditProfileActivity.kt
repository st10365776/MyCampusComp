package com.example.mycampuscomp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.mycampuscomp.repository.UserRepository
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private val userRepository = UserRepository()
    
    private lateinit var ivProfilePreview: ShapeableImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etPhotoUrl: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        ivProfilePreview = findViewById(R.id.ivProfilePreview)
        etName = findViewById(R.id.etName)
        etPhotoUrl = findViewById(R.id.etPhotoUrl)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        userRepository.startListening()
        userRepository.user.observe(this) { user ->
            if (user != null) {
                etName.setText(user.name)
                etPhotoUrl.setText(user.profileImageUrl)
                if (user.profileImageUrl.isNotEmpty()) {
                    ivProfilePreview.load(user.profileImageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.logo2)
                        error(R.drawable.logo2)
                    }
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val photoUrl = etPhotoUrl.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                userRepository.updateUserProfile(name, photoUrl)
                Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userRepository.stopListening()
    }
}