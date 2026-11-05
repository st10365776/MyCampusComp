package com.example.mycampuscomp.notifications

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Real-time updates: any document added to the Firestore "announcements" collection
 * (fields: title, message, optional timestamp) becomes a notification on every signed-in device.
 */
object AnnouncementListener {

    private const val TAG = "AnnouncementListener"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var registration: ListenerRegistration? = null

    fun start() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        registration?.remove()
        registration = null
        if (uid == null) return

        registration = FirebaseFirestore.getInstance().collection("announcements")
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Announcements unavailable: ${error.message}")
                    return@addSnapshotListener
                }
                val added = snapshot?.documentChanges?.filter { it.type == DocumentChange.Type.ADDED }
                    ?: return@addSnapshotListener
                scope.launch {
                    for (change in added) {
                        val doc = change.document
                        val title = doc.getString("title") ?: continue
                        val message = doc.getString("message") ?: ""
                        val time = when (val raw = doc.get("timestamp")) {
                            is Timestamp -> raw.toDate().time
                            is Number -> raw.toLong()
                            else -> System.currentTimeMillis()
                        }
                        // Older announcements are listed silently; only recent ones pop up.
                        val recent = System.currentTimeMillis() - time < TimeUnit.HOURS.toMillis(24)
                        NotificationCenter.post(
                            uid, title, message, "announcement",
                            id = "announcement_${doc.id}", showSystem = recent, timestamp = time
                        )
                    }
                }
            }
    }
}
