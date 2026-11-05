package com.example.mycampuscomp

import android.app.Application
import com.example.mycampuscomp.notifications.AnnouncementListener
import com.example.mycampuscomp.notifications.NotificationCenter
import com.example.mycampuscomp.sync.SyncManager
import com.google.firebase.auth.FirebaseAuth

class MyCampusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationCenter.init(this)
        SyncManager.init(this)
        FirebaseAuth.getInstance().addAuthStateListener {
            AnnouncementListener.start()
            NotificationCenter.registerPushToken()
            NotificationCenter.runRemindersNow()
        }
    }
}