package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)
        val txtForgotPassword = findViewById<TextView>(R.id.txtForgotPassword)

        btnLogin.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnGoogleSignIn.setOnClickListener {
            signInGoogle()
        }

        txtForgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            builder.setMessage("Enter your email to receive a reset link, or create a new account if you don't have one.")

            val input = EditText(this)
            input.hint = "Email Address"
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            val container = FrameLayout(this)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val margin = (20 * resources.displayMetrics.density).toInt()
            params.setMargins(margin, 10, margin, 10)
            input.layoutParams = params
            container.addView(input)
            builder.setView(container)

            builder.setPositiveButton("Send Link") { _, _ ->
                val emailText = input.text.toString().trim()
                if (emailText.isNotEmpty()) {
                    auth.sendPasswordResetEmail(emailText)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Reset link sent to $emailText", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNeutralButton("Register") { _, _ ->
                startActivity(Intent(this, RegisterActivity::class.java))
            }

            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message} (Status Code: ${e.statusCode})", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Google Sign-In Canceled or Failed (Result Code: ${result.resultCode})", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Authentication Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
