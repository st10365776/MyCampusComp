package com.example.mycampuscomp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mycampuscomp.NotificationsActivity
import com.example.mycampuscomp.R
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

object NotificationCenter {
    private const val TAG = "NotificationCenter"
    private const val CHANNEL_ID = "announcements"
    private var appContext: Context? = null

    val currentUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun init(context: Context) {
        appContext = context.applicationContext
        createNotificationChannel(appContext!!)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Announcements & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Campus updates, assignment reminders, and badge notifications"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun describeDue(dueDate: String): String {
        return if (dueDate.isNotBlank()) "due $dueDate" else "due soon"
    }

    fun daysUntil(dueDate: String): Int? {
        if (dueDate.isBlank()) return null
        val formats = listOf(
            SimpleDateFormat("MMM d, yyyy", Locale.US),
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US)
        )
        var parsedDate: Date? = null
        for (fmt in formats) {
            try {
                parsedDate = fmt.parse(dueDate.trim())
                if (parsedDate != null) break
            } catch (_: Exception) { }
        }
        val target = parsedDate ?: return null

        val calToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calTarget = Calendar.getInstance().apply {
            time = target
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMs = calTarget.timeInMillis - calToday.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
    }

    fun runRemindersNow() {
        Log.d(TAG, "Running reminders check")
    }

    fun post(
        userId: String,
        title: String,
        message: String,
        type: String = "general",
        id: String = UUID.randomUUID().toString(),
        showSystem: Boolean = true,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val ctx = appContext ?: return
        val notif = NotificationModel(
            id = id,
            title = title,
            message = message,
            type = type,
            timestamp = timestamp,
            read = false,
            userId = userId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(ctx).notificationDao().insert(listOf(notif))
            } catch (e: Exception) {
                Log.e(TAG, "Error saving notification to DB", e)
            }
        }

        if (!showSystem) return

        try {
            val intent = Intent(ctx, NotificationsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                ctx,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification", e)
        }
    }
}
