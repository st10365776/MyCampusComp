package com.example.mycampuscomp

import android.app.Application
import com.example.mycampuscomp.notifications.NotificationCenter
import com.example.mycampuscomp.sync.SyncManager

class MyCampusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationCenter.init(this)
        SyncManager.init(this)
    }
}
