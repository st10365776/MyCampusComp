package com.example.mycampuscomp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.db.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dao = AppDatabase.getDatabase(this).notificationDao()

        val adapter = NotificationAdapter(mutableListOf()) { n ->
            if (!n.read) lifecycleScope.launch { dao.markRead(n.id) }
        }
        val list = findViewById<RecyclerView>(R.id.rvNotifications)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        val empty = findViewById<View>(R.id.emptyState)
        val actions = findViewById<View>(R.id.actionRow)

        dao.getForUser(uid).asLiveData().observe(this) { items ->
            adapter.updateNotifications(items)
            val none = items.isEmpty()
            empty.visibility = if (none) View.VISIBLE else View.GONE
            list.visibility = if (none) View.GONE else View.VISIBLE
            actions.visibility = if (none) View.GONE else View.VISIBLE
        }

        findViewById<View>(R.id.btnMarkAllRead).setOnClickListener {
            lifecycleScope.launch { dao.markAllRead(uid) }
        }
        findViewById<View>(R.id.btnClearAll).setOnClickListener {
            lifecycleScope.launch { dao.clearAll(uid) }
        }
    }
}
