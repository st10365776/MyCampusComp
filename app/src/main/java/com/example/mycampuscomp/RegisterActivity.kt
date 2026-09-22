package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.mycampuscomp.model.User
import com.example.mycampuscomp.repository.UserRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val name = findViewById<EditText>(R.id.etName)
        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoogleRegister = findViewById<Button>(R.id.btnGoogleRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        btnRegister.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val nameText = name.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty() || nameText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid == null) {
                        Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                        return@addOnSuccessListener
                    }
                    val newUser = User(uid = uid, name = nameText, email = emailText)
                    UserRepository.saveUserLocally(this@RegisterActivity, newUser)
                    // Also persist the user's profile in Firestore so classes and
                    // other data can be stored under this user's document.
                    firestore.collection("users").document(uid).set(newUser)
                        .addOnCompleteListener {
                            // Whether or not the Firestore write succeeds, the account
                            // itself was created, so let the user in and just warn
                            // if the profile failed to save.
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Account created, but saving your profile failed: ${it.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                            }
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnGoogleRegister.setOnClickListener {
            signInGoogle()
        }

        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message} (Status Code: ${e.statusCode})", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Google Sign-In Canceled or Failed (Result Code: ${result.resultCode})", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid == null) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }
                val photoUrl = account.photoUrl?.toString() ?: ""
                val newUser = User(
                    uid = uid,
                    name = account.displayName ?: "",
                    email = account.email ?: "",
                    profileImageUrl = photoUrl
                )
                UserRepository.saveUserLocally(this@RegisterActivity, newUser)
                // merge() so an existing user's doc (e.g. they've registered before)
                // isn't clobbered, while a brand-new user still gets a profile doc.
                firestore.collection("users").document(uid).set(newUser, SetOptions.merge())
                    .addOnCompleteListener {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Authentication Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}