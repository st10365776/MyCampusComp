package com.example.mycampuscomp.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.room.withTransaction
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.SyncState
import com.example.mycampuscomp.model.TimetableClass
import com.example.mycampuscomp.notifications.NotificationCenter
import com.example.mycampuscomp.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Pushes locally queued changes (rows flagged PENDING_*) to Firestore whenever the
 * device is online, then pulls the latest server data without touching unsynced rows.
 */
object SyncManager {

    private const val TAG = "SyncManager"
    private const val TIMEOUT_MS = 20_000L

    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private const val RETRY_DELAY_MS = 15_000L
    private var retryJob: Job? = null
    // Set when changes had to wait (offline / failed upload), so we only announce a catch-up sync.
    @Volatile private var announceSync = false
    @Volatile private var lastPending = 0

    private val db get() = AppDatabase.getDatabase(appContext)
    private val firestore get() = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid

    fun init(context: Context) {
        appContext = context.applicationContext
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                requestSync()
            }

            override fun onLost(network: Network) {
                announceSync = true
            }
        })
        requestSync()
    }

    fun requestSync() {
        if (!::appContext.isInitialized) return
        scope.launch { syncAll() }
    }

    /** Number of local changes (adds/edits/deletes) still waiting to reach the server. */
    fun pendingCount(): Flow<Int> {
        val userId = uid ?: return flowOf(0)
        return combine(
            db.assignmentDao().countPending(userId),
            db.timetableDao().countPending(userId)
        ) { a, t -> a + t }
    }

    suspend fun syncAll() {
        val userId = uid ?: return
        if (!NetworkUtils.isOnline(appContext)) return
        mutex.withLock {
            try {
                pushAssignments(userId)
                pushTimetable(userId)
                pullAssignments(userId)
                pullTimetable(userId)
            } catch (e: Exception) {
                Log.w(TAG, "Sync failed, will retry: ${e.message}")
            }
            // The network can report "connected" while the server is still unreachable
            // (e.g. DNS not ready), so keep retrying until nothing is pending.
            val pendingBefore = pendingTotal(userId)
            if (pendingBefore > 0) lastPending = pendingBefore
            if (pendingBefore > 0) {
                announceSync = true
                scheduleRetry()
            } else {
                retryJob?.cancel()
                if (announceSync && lastPending > 0) {
                    val n = lastPending
                    NotificationCenter.post(
                        userId,
                        "Back online - changes synced",
                        if (n == 1) "1 change was uploaded to the cloud." else "$n changes were uploaded to the cloud.",
                        "sync"
                    )
                }
                announceSync = false
                lastPending = 0
            }
        }
    }

    private suspend fun pendingTotal(userId: String): Int =
        db.assignmentDao().getPending(userId).size + db.timetableDao().getPending(userId).size

    private fun scheduleRetry() {
        if (retryJob?.isActive == true) return
        retryJob = scope.launch {
            delay(RETRY_DELAY_MS)
            retryJob = null
            // A failed DNS lookup can leave Firestore's connection stuck; reconnect it.
            runCatching {
                firestore.disableNetwork().await()
                firestore.enableNetwork().await()
            }
            syncAll()
        }
    }

    private fun assignmentsRef(userId: String) =
        firestore.collection("users").document(userId).collection("assignments")

    private fun timetableRef(userId: String) =
        firestore.collection("users").document(userId).collection("timetable")

    private suspend fun pushAssignments(userId: String) {
        val dao = db.assignmentDao()
        for (item in dao.getPending(userId)) {
            try {
                if (item.syncState == SyncState.PENDING_DELETE) {
                    withTimeout(TIMEOUT_MS) { assignmentsRef(userId).document(item.id).delete().await() }
                    dao.deleteAssignment(item.id)
                } else {
                    withTimeout(TIMEOUT_MS) { assignmentsRef(userId).document(item.id).set(item).await() }
                    // Only mark synced if the user didn't edit it again while uploading.
                    if (dao.getById(item.id) == item) {
                        dao.insertAssignments(listOf(item.copy(syncState = SyncState.SYNCED)))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Assignment ${item.id} not synced yet: ${e.message}")
            }
        }
    }

    private suspend fun pushTimetable(userId: String) {
        val dao = db.timetableDao()
        for (item in dao.getPending(userId)) {
            try {
                if (item.syncState == SyncState.PENDING_DELETE) {
                    withTimeout(TIMEOUT_MS) { timetableRef(userId).document(item.id).delete().await() }
                    dao.deleteClass(item.id)
                } else {
                    withTimeout(TIMEOUT_MS) { timetableRef(userId).document(item.id).set(item).await() }
                    if (dao.getById(item.id) == item) {
                        dao.insertClasses(listOf(item.copy(syncState = SyncState.SYNCED)))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Class ${item.id} not synced yet: ${e.message}")
            }
        }
    }

    private suspend fun pullAssignments(userId: String) {
        val dao = db.assignmentDao()
        val remote = withTimeout(TIMEOUT_MS) {
            assignmentsRef(userId).get(Source.SERVER).await()
        }.toObjects(Assignment::class.java).map { it.copy(userId = userId, syncState = SyncState.SYNCED) }
        db.withTransaction {
            val pendingIds = dao.getPending(userId).map { it.id }.toSet()
            dao.deleteSyncedForUser(userId)
            dao.insertAssignments(remote.filter { it.id !in pendingIds })
        }
    }

    private suspend fun pullTimetable(userId: String) {
        val dao = db.timetableDao()
        val remote = withTimeout(TIMEOUT_MS) {
            timetableRef(userId).get(Source.SERVER).await()
        }.toObjects(TimetableClass::class.java).map { it.copy(userId = userId, syncState = SyncState.SYNCED) }
        db.withTransaction {
            val pendingIds = dao.getPending(userId).map { it.id }.toSet()
            dao.deleteSyncedForUser(userId)
            dao.insertClasses(remote.filter { it.id !in pendingIds })
        }
    }
}
