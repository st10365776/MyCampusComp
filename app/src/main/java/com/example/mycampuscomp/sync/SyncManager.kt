package com.example.mycampuscomp.sync

import android.content.Context
import android.util.Log

object SyncManager {
    private const val TAG = "SyncManager"

    fun init(context: Context) {
        Log.d(TAG, "SyncManager initialized")
    }

    suspend fun syncAll() {
        Log.d(TAG, "Syncing all data with remote server")
    }

    fun requestSync() {
        Log.d(TAG, "Sync requested")
    }
}
