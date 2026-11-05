package com.example.mycampuscomp.sync

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mycampuscomp.utils.NetworkUtils

object SyncStatusBanner {
    fun bind(activity: AppCompatActivity, bannerView: View, offlineText: String) {
        NetworkUtils.NetworkStateLiveData(activity).observe(activity) { isConnected ->
            if (bannerView is TextView) {
                bannerView.text = if (isConnected) "" else offlineText
            }
            bannerView.visibility = if (isConnected) View.GONE else View.VISIBLE
        }
    }
}
