package com.example.mycampuscomp.repository

import android.util.Log
import com.example.mycampuscomp.db.TimetableDao
import com.example.mycampuscomp.model.SyncState
import com.example.mycampuscomp.notifications.NotificationCenter
import com.example.mycampuscomp.model.TimetableClass
import com.example.mycampuscomp.sync.SyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

/**
 * Room is the source of truth for the UI. Writes are saved locally and flagged as
 * pending; SyncManager uploads them as soon as the device is online.
 */
class TimetableRepository(
    private val timetableDao: TimetableDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val TAG = "TimetableRepository"

    fun getClassesForDayLocal(day: String): Flow<List<TimetableClass>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return timetableDao.getClassesForDay(day, uid)
    }

    fun getAllClassesLocal(): Flow<List<TimetableClass>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return timetableDao.getAllClasses(uid)
    }

    suspend fun refreshClassesFromFirestore() {
        SyncManager.syncAll()
    }

    suspend fun saveClass(timetableClass: TimetableClass) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot save class")
            return
        }
        val id = timetableClass.id.ifEmpty { UUID.randomUUID().toString() }
        timetableDao.insertClasses(
            listOf(timetableClass.copy(id = id, userId = uid, syncState = SyncState.PENDING_UPSERT))
        )
        SyncManager.requestSync()
        NotificationCenter.post(
            uid,
            "Class added",
            "${timetableClass.subject} on ${timetableClass.day} at ${timetableClass.time}.",
            "class"
        )
    }
}
