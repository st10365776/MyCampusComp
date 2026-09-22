package com.example.mycampuscomp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mycampuscomp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val TAG = "UserRepository"

    // Live-updates whenever the signed-in user's profile document changes
    // in Firestore (e.g. their study streak ticks up), instead of a
    // one-shot fetch. Null means "no signed-in user" / "not loaded yet".
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private var listener: ListenerRegistration? = null

    // Each signed-in user's profile lives at users/{uid}, the same
    // document written to by RegisterActivity / LoginActivity.
    fun startListening() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot listen for profile changes")
            _user.value = null
            return
        }

        listener?.remove()
        listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user profile", error)
                    return@addSnapshotListener
                }
                _user.value = snapshot?.toObject(User::class.java)
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    suspend fun updateUserProfile(name: String, profileImageUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val updates = mapOf(
                "name" to name,
                "profileImageUrl" to profileImageUrl
            )
            firestore.collection("users").document(uid).update(updates)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
        }
    }
}