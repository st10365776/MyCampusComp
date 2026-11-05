package com.example.mycampuscomp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.mycampuscomp.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository

    private lateinit var ivProfilePreview: ShapeableImageView
    private lateinit var btnChoosePhoto: MaterialButton
    private lateinit var etName: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton

    private var currentPhotoUrl: String = ""
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            ivProfilePreview.load(uri) {
                crossfade(true)
                placeholder(R.drawable.logo2)
                error(R.drawable.logo2)
            }
            Toast.makeText(this@EditProfileActivity, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userRepository = UserRepository(context = applicationContext)

        ivProfilePreview = findViewById(R.id.ivProfilePreview)
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto)
        etName = findViewById(R.id.etName)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        val openPhotoPicker = {
            pickImageLauncher.launch("image/*")
        }

        ivProfilePreview.setOnClickListener { openPhotoPicker() }
        btnChoosePhoto.setOnClickListener { openPhotoPicker() }

        // Load cached local image first
        val savedLocalUrl = UserRepository.getLocalProfileImageUrl(this)
        if (savedLocalUrl.isNotEmpty()) {
            currentPhotoUrl = savedLocalUrl
            ivProfilePreview.load(savedLocalUrl) {
                crossfade(true)
                placeholder(R.drawable.logo2)
                error(R.drawable.logo2)
            }
        }

        userRepository.startListening()
        userRepository.user.observe(this) { user ->
            if (user != null) {
                if (user.name.isNotEmpty() && etName.text.isNullOrEmpty()) etName.setText(user.name)
                if (user.profileImageUrl.isNotEmpty()) {
                    currentPhotoUrl = user.profileImageUrl
                    if (selectedPhotoUri == null) {
                        ivProfilePreview.load(user.profileImageUrl) {
                            crossfade(true)
                            placeholder(R.drawable.logo2)
                            error(R.drawable.logo2)
                        }
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            finish()
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }

            btnSave.isEnabled = false

            lifecycleScope.launch(Dispatchers.IO) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                var finalPhoto = currentPhotoUrl

                val pickedUri = selectedPhotoUri
                if (pickedUri != null) {
                    val fileUri = UserRepository.saveProfilePhotoToLocalFile(this@EditProfileActivity, pickedUri, uid)
                    if (fileUri != null) {
                        finalPhoto = fileUri
                    }
                }

                // Save locally immediately so DashboardActivity reads it onResume
                UserRepository.saveProfileImageUrlLocally(this@EditProfileActivity, finalPhoto)

                // Sync to Firestore database
                userRepository.updateUserProfile(name, finalPhoto, this@EditProfileActivity)

                withContext(Dispatchers.Main) {
                    btnSave.isEnabled = true
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userRepository.stopListening()
    }
}
