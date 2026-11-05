package com.example.mycampuscomp.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Campus Update"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        NotificationCenter.post(uid, title, body, "announcement")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
    }
}
