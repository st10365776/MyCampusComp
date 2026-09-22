package com.example.mycampuscomp.repository

import android.util.Log
import com.example.mycampuscomp.db.AssignmentDao
import com.example.mycampuscomp.model.Assignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class AssignmentRepository(
    private val assignmentDao: AssignmentDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val TAG = "AssignmentRepository"

    // Each signed-in user's assignments live under their own document, e.g.
    // users/{uid}/assignments/{assignmentId} - so one user never sees another's data.
    private fun userAssignmentCollection(uid: String): CollectionReference {
        return firestore.collection("users").document(uid).collection("assignments")
    }

    fun getAssignmentsLocal(): Flow<List<Assignment>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return assignmentDao.getAssignmentsForUser(uid)
    }

    suspend fun refreshAssignmentsFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; skipping Firestore refresh")
            return
        }
        try {
            val snapshot = userAssignmentCollection(uid).get().await()
            val assignments = snapshot.toObjects(Assignment::class.java).map { it.copy(userId = uid) }
            Log.d(TAG, "Fetched ${assignments.size} assignments from Firestore for $uid")
            assignmentDao.clearAllForUser(uid)
            if (assignments.isNotEmpty()) {
                assignmentDao.insertAssignments(assignments)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing assignments from Firestore", e)
        }
    }

    suspend fun saveAssignment(assignment: Assignment) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot save assignment")
            return
        }

        val collection = userAssignmentCollection(uid)
        val docRef = if (assignment.id.isEmpty()) {
            collection.document()
        } else {
            collection.document(assignment.id)
        }
        val finalAssignment = assignment.copy(id = docRef.id, userId = uid)

        // 1. Save to local Room DB FIRST so user actions work seamlessly offline
        try {
            assignmentDao.insertAssignments(listOf(finalAssignment))
            Log.d(TAG, "Saved assignment locally to Room: $finalAssignment")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving assignment locally to Room", e)
        }

        // 2. Sync with Firestore in try-catch so offline network errors won't prevent local save
        try {
            Log.d(TAG, "Syncing assignment to Firestore: $finalAssignment")
            docRef.set(finalAssignment).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore write offline/deferred: ${e.message}")
        }
    }

    suspend fun deleteAssignment(assignment: Assignment) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot delete assignment")
            return
        }

        // 1. Delete from local Room DB FIRST
        try {
            assignmentDao.deleteAssignment(assignment.id)
            Log.d(TAG, "Deleted assignment locally from Room: ${assignment.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting assignment locally from Room", e)
        }

        // 2. Sync deletion with Firestore
        try {
            userAssignmentCollection(uid).document(assignment.id).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore delete offline/deferred: ${e.message}")
        }
    }
}