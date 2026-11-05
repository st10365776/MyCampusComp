package com.example.mycampuscomp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.SyncState
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Posts deadline reminders for pending assignments and a summary of today's classes.
 * Ids contain today's date, so each reminder is only delivered once per day.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = NotificationCenter.currentUid ?: return Result.success()
        val db = AppDatabase.getDatabase(applicationContext)
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

        db.assignmentDao().getAssignmentsForUser(uid).first()
            .filter { it.status != "Completed" && it.syncState != SyncState.PENDING_DELETE }
            .forEach { assignment ->
                val days = NotificationCenter.daysUntil(assignment.dueDate) ?: return@forEach
                when {
                    days in 0..3 -> NotificationCenter.post(
                        uid,
                        if (days == 0) "Assignment due today" else "Assignment due soon",
                        "\"${assignment.title}\" is ${NotificationCenter.describeDue(assignment.dueDate)}.",
                        "reminder",
                        id = "due_${assignment.id}_$today"
                    )
                    days < 0 && days >= -7 -> NotificationCenter.post(
                        uid,
                        "Assignment overdue",
                        "\"${assignment.title}\" is ${NotificationCenter.describeDue(assignment.dueDate)}.",
                        "reminder",
                        id = "overdue_${assignment.id}_$today"
                    )
                }
            }

        val dayName = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            else -> null
        }
        if (dayName != null) {
            val classes = db.timetableDao().getClassesForDay(dayName, uid).first()
            if (classes.isNotEmpty()) {
                val first = classes.first()
                val count = if (classes.size == 1) "1 class" else "${classes.size} classes"
                NotificationCenter.post(
                    uid,
                    "Today's classes",
                    "You have $count today. First: ${first.subject} at ${first.time} in ${first.room}.",
                    "reminder",
                    id = "classes_$today"
                )
            }
        }
        return Result.success()
    }
}
