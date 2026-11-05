package com.example.mycampuscomp.sync

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.example.mycampuscomp.utils.NetworkUtils

/** Shows offline / pending-sync state in a banner TextView. */
object SyncStatusBanner {

    fun bind(activity: AppCompatActivity, banner: TextView, offlineLabel: String) {
        val online = NetworkUtils.NetworkStateLiveData(activity)
        val pending = SyncManager.pendingCount().asLiveData()

        val state = MediatorLiveData<Pair<Boolean?, Int?>>()
        state.addSource(online) { state.value = it to pending.value }
        state.addSource(pending) { state.value = online.value to it }

        state.observe(activity) { (isOnline, count) ->
            val changes = count ?: 0
            val changesText = if (changes == 1) "1 change" else "$changes changes"
            when {
                isOnline == false -> {
                    banner.text = if (changes > 0) {
                        "Offline - $changesText will sync when you reconnect"
                    } else {
                        offlineLabel
                    }
                    banner.visibility = View.VISIBLE
                }
                changes > 0 -> {
                    banner.text = "Syncing $changesText..."
                    banner.visibility = View.VISIBLE
                }
                else -> banner.visibility = View.GONE
            }
        }
    }
}
