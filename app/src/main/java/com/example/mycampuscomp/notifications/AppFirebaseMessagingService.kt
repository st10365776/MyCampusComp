package com.example.mycampuscomp.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/** Receives FCM pushes while the app is running and records them on the Notifications screen. */
class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        NotificationCenter.currentUid?.let { NotificationCenter.saveToken(it, token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val uid = NotificationCenter.currentUid ?: return
        val title = message.notification?.title ?: message.data["title"] ?: "MyCampus"
        val body = message.notification?.body ?: message.data["message"] ?: return
        val type = message.data["type"] ?: "announcement"
        val id = message.messageId?.let { "fcm_$it" } ?: UUID.randomUUID().toString()
        scope.launch { NotificationCenter.post(uid, title, body, type, id) }
    }
}
