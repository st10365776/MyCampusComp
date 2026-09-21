package com.example.mycampuscomp.repository

import android.util.Log
import com.example.mycampuscomp.db.TimetableDao
import com.example.mycampuscomp.model.TimetableClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class TimetableRepository(
    private val timetableDao: TimetableDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val TAG = "TimetableRepository"

    // Each signed-in user's classes live under their own document, e.g.
    // users/{uid}/timetable/{classId} - so one user never sees another's data.
    private fun userTimetableCollection(uid: String): CollectionReference {
        return firestore.collection("users").document(uid).collection("timetable")
    }

    fun getClassesForDayLocal(day: String): Flow<List<TimetableClass>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return timetableDao.getClassesForDay(day, uid)
    }

    // Used for the "Week" tab - every class for the signed-in user, Mon-Fri.
    fun getAllClassesLocal(): Flow<List<TimetableClass>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return timetableDao.getAllClassesForUser(uid)
    }

    suspend fun refreshClassesFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; skipping Firestore refresh")
            return
        }
        try {
            val snapshot = userTimetableCollection(uid).get().await()
            val classes = snapshot.toObjects(TimetableClass::class.java).map { it.copy(userId = uid) }
            Log.d(TAG, "Fetched ${classes.size} classes from Firestore for $uid")
            timetableDao.clearAllForUser(uid)
            if (classes.isNotEmpty()) {
                timetableDao.insertClasses(classes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing classes from Firestore", e)
        }
    }

    suspend fun saveClass(timetableClass: TimetableClass) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot save class")
            return
        }
        try {
            val collection = userTimetableCollection(uid)
            val docRef = if (timetableClass.id.isEmpty()) {
                collection.document()
            } else {
                collection.document(timetableClass.id)
            }
            val finalClass = timetableClass.copy(id = docRef.id, userId = uid)
            Log.d(TAG, "Saving class to Firestore: $finalClass")
            docRef.set(finalClass).await()
            Log.d(TAG, "Successfully saved to Firestore")
            // Local update will happen through Firestore sync or manual refresh,
            // but for immediate feedback, we can insert to DAO right away.
            timetableDao.insertClasses(listOf(finalClass))
        } catch (e: Exception) {
            Log.e(TAG, "Error saving class to Firestore", e)
        }
    }
}