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

    // Used for both creating a new assignment (id is empty) and editing an
    // existing one (id already set) - it's an upsert either way, so the
    // same Firestore document just gets overwritten when editing.
    suspend fun saveAssignment(assignment: Assignment) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot save assignment")
            return
        }
        try {
            val collection = userAssignmentCollection(uid)
            val docRef = if (assignment.id.isEmpty()) {
                collection.document()
            } else {
                collection.document(assignment.id)
            }
            val finalAssignment = assignment.copy(id = docRef.id, userId = uid)
            Log.d(TAG, "Saving assignment to Firestore: $finalAssignment")
            docRef.set(finalAssignment).await()
            // Immediate local update for fast UI feedback; the next
            // refreshAssignmentsFromFirestore() call will reconcile fully.
            assignmentDao.insertAssignments(listOf(finalAssignment))
        } catch (e: Exception) {
            Log.e(TAG, "Error saving assignment to Firestore", e)
        }
    }

    suspend fun deleteAssignment(assignment: Assignment) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot delete assignment")
            return
        }
        try {
            userAssignmentCollection(uid).document(assignment.id).delete().await()
            assignmentDao.deleteAssignment(assignment.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting assignment from Firestore", e)
        }
    }
}