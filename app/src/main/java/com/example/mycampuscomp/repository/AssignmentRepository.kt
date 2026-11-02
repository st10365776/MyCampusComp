package com.example.mycampuscomp.repository

import android.util.Log
import com.example.mycampuscomp.db.AssignmentDao
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.SyncState
import com.example.mycampuscomp.notifications.NotificationCenter
import com.example.mycampuscomp.sync.SyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

/**
 * Room is the source of truth for the UI. Writes are saved locally and flagged as
 * pending; SyncManager uploads them as soon as the device is online.
 */
class AssignmentRepository(
    private val assignmentDao: AssignmentDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val TAG = "AssignmentRepository"

    fun getAssignmentsLocal(): Flow<List<Assignment>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return assignmentDao.getAssignmentsForUser(uid)
    }

    suspend fun refreshAssignmentsFromFirestore() {
        SyncManager.syncAll()
    }

    suspend fun saveAssignment(assignment: Assignment) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot save assignment")
            return
        }
        val id = assignment.id.ifEmpty { UUID.randomUUID().toString() }
        val previous = assignmentDao.getById(id)
        assignmentDao.insertAssignments(
            listOf(assignment.copy(id = id, userId = uid, syncState = SyncState.PENDING_UPSERT))
        )
        SyncManager.requestSync()

        val due = NotificationCenter.describeDue(assignment.dueDate)
        when {
            previous == null -> NotificationCenter.post(
                uid, "Assignment added", "\"${assignment.title}\" is $due.", "assignment"
            )
            previous.status != "Completed" && assignment.status == "Completed" -> NotificationCenter.post(
                uid, "Assignment completed", "Well done! You finished \"${assignment.title}\".", "assignment"
            )
            else -> NotificationCenter.post(
                uid, "Assignment updated", "\"${assignment.title}\" is $due.", "assignment"
            )
        }
    }

    suspend fun deleteAssignment(assignment: Assignment) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No signed-in user; cannot delete assignment")
            return
        }
        // Hidden from the UI immediately; removed for good once the server confirms.
        assignmentDao.markDeleted(assignment.id)
        SyncManager.requestSync()
        NotificationCenter.post(uid, "Assignment removed", "\"${assignment.title}\" was deleted.", "assignment")
    }
}
