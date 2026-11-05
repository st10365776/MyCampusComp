package com.example.mycampuscomp.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mycampuscomp.NotificationsActivity
import com.example.mycampuscomp.R
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Single entry point for notifications: every notification is stored in Room (so it appears
 * on the Notifications screen, even when created offline) and shown in the system tray.
 */
object NotificationCenter {

    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_UPDATES = "updates"
    const val CHANNEL_ANNOUNCEMENTS = "announcements"

    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUid: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    fun init(context: Context) {
        appContext = context.applicationContext
        createChannels()
        scheduleReminders()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = appContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Assignment deadlines and today's classes"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_UPDATES, "Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Confirmations when you add or change assignments and classes, sync and badges"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ANNOUNCEMENTS, "Announcements", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Campus announcements and push messages"
            }
        )
    }

    private fun scheduleReminders() {
        val work = PeriodicWorkRequestBuilder<ReminderWorker>(3, TimeUnit.HOURS).build()
        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork("reminders", ExistingPeriodicWorkPolicy.KEEP, work)
    }

    /** Checks deadlines and today's classes right now (deduplicated per day). */
    fun runRemindersNow() {
        if (!::appContext.isInitialized) return
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "reminders_now", ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<ReminderWorker>().build()
        )
    }

    /** Fire-and-forget version for non-suspend callers. */
    fun postAsync(title: String, message: String, type: String, id: String = UUID.randomUUID().toString()) {
        val uid = currentUid ?: return
        scope.launch { post(uid, title, message, type, id) }
    }

    /** Returns false if a notification with this id already exists. */
    suspend fun post(
        userId: String,
        title: String,
        message: String,
        type: String,
        id: String = UUID.randomUUID().toString(),
        showSystem: Boolean = true,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        if (!::appContext.isInitialized) return false
        val row = AppDatabase.getDatabase(appContext).notificationDao().insert(
            NotificationModel(
                id = id, title = title, message = message, type = type,
                timestamp = timestamp, read = false, userId = userId
            )
        )
        if (row == -1L) return false
        if (showSystem) show(id, title, message, type)
        return true
    }

    @SuppressLint("MissingPermission")
    private fun show(id: String, title: String, message: String, type: String) {
        val manager = NotificationManagerCompat.from(appContext)
        if (!manager.areNotificationsEnabled()) return

        val intent = Intent(appContext, NotificationsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            appContext, id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channel = when (type) {
            "reminder" -> CHANNEL_REMINDERS
            "announcement" -> CHANNEL_ANNOUNCEMENTS
            else -> CHANNEL_UPDATES
        }
        val notification = NotificationCompat.Builder(appContext, channel)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (channel == CHANNEL_UPDATES) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH
            )
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        manager.notify(id.hashCode(), notification)
    }

    /** Stores this device's FCM token so a server (or the Firebase console) can push to the user. */
    fun registerPushToken() {
        val uid = currentUid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            saveToken(uid, token)
        }
    }

    fun saveToken(uid: String, token: String) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    // ---------- Due date helpers ----------

    private val dueFormat get() = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    /** Whole days from today until the due date, or null if it can't be parsed. */
    fun daysUntil(dueDate: String): Int? {
        val due = runCatching { dueFormat.parse(dueDate) }.getOrNull() ?: return null
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dueDay = Calendar.getInstance().apply {
            time = due
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return TimeUnit.MILLISECONDS.toDays(dueDay.timeInMillis - today.timeInMillis).toInt()
    }

    fun describeDue(dueDate: String): String {
        val days = daysUntil(dueDate) ?: return "due $dueDate"
        return when {
            days > 1 -> "due in $days days ($dueDate)"
            days == 1 -> "due tomorrow ($dueDate)"
            days == 0 -> "due today"
            days == -1 -> "overdue by 1 day"
            else -> "overdue by ${-days} days"
        }
    }
}
