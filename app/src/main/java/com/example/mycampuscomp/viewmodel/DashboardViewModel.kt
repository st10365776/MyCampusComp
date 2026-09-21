package com.example.mycampuscomp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.TimetableClass
import com.example.mycampuscomp.repository.AssignmentRepository
import com.example.mycampuscomp.repository.TimetableRepository
import com.example.mycampuscomp.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "DashboardViewModel"

    private val userRepository = UserRepository()
    private val timetableRepository: TimetableRepository
    private val assignmentRepository: AssignmentRepository

    // Display name for the greeting card. Falls back to "Student" until the
    // Firestore profile loads (or if the user never set a name).
    val userName: LiveData<String>

    // Current study streak, straight from the user's Firestore profile.
    val studyStreak: LiveData<Int>

    // All of today's classes, used for the "TODAY" count.
    val todayClasses: LiveData<List<TimetableClass>>

    // The next class today that hasn't started yet (falls back to the
    // first class of the day if every class's time already passed / failed
    // to parse), or null if there are no classes today.
    val nextClass: LiveData<TimetableClass?>

    // All of the signed-in user's assignments.
    val assignments: LiveData<List<Assignment>>

    // Count of non-completed assignments due within the next 7 days.
    val assignmentsDueThisWeek: LiveData<Int>

    // The single soonest-due, non-completed assignment, for the
    // "Upcoming" card - or null if there's nothing outstanding.
    val nextAssignment: LiveData<Assignment?>

    init {
        val db = AppDatabase.getDatabase(application)
        timetableRepository = TimetableRepository(db.timetableDao())
        assignmentRepository = AssignmentRepository(db.assignmentDao())

        userRepository.startListening()
        userName = userRepository.user.map { user ->
            user?.name?.trim()?.takeIf { it.isNotEmpty() } ?: "Student"
        }
        studyStreak = userRepository.user.map { it?.studyStreak ?: 0 }

        todayClasses = timetableRepository.getClassesForDayLocal(todayAbbrev()).asLiveData()
        nextClass = todayClasses.map { classes -> pickNextClass(classes) }

        assignments = assignmentRepository.getAssignmentsLocal().asLiveData()
        assignmentsDueThisWeek = assignments.map { list -> countDueWithinDays(list, 7) }
        nextAssignment = assignments.map { list -> pickNextAssignment(list) }

        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            Log.d(TAG, "Refreshing dashboard data...")
            timetableRepository.refreshClassesFromFirestore()
            assignmentRepository.refreshAssignmentsFromFirestore()
        }
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.stopListening()
    }

    companion object {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dueDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        // Matches the 3-letter day labels ("Mon".."Fri") already used by
        // TimetableActivity / TimetableDao.
        fun todayAbbrev(): String =
            SimpleDateFormat("EEE", Locale.ENGLISH).format(Date())

        // Classes are user-entered free text for "time" (e.g. "09:00"), so
        // parsing is best-effort: pick the earliest class whose time hasn't
        // passed yet, otherwise fall back to the first class of the day.
        fun pickNextClass(classes: List<TimetableClass>): TimetableClass? {
            if (classes.isEmpty()) return null

            val now = Calendar.getInstance()
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            val upcoming = classes
                .mapNotNull { cls -> parseMinutes(cls.time)?.let { cls to it } }
                .filter { (_, minutes) -> minutes >= nowMinutes }
                .minByOrNull { (_, minutes) -> minutes }

            return upcoming?.first ?: classes.first()
        }

        // Soonest-due assignment that isn't marked Completed.
        fun pickNextAssignment(assignments: List<Assignment>): Assignment? {
            return assignments
                .filter { it.status != "Completed" }
                .mapNotNull { assignment -> parseDate(assignment.dueDate)?.let { assignment to it } }
                .minByOrNull { (_, date) -> date }
                ?.first
        }

        fun countDueWithinDays(assignments: List<Assignment>, days: Int): Int {
            val today = startOfDay(Date())
            val cutoff = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, days)
            }.time

            return assignments.count { assignment ->
                if (assignment.status == "Completed") return@count false
                val due = parseDate(assignment.dueDate) ?: return@count false
                !due.before(today) && !due.after(cutoff)
            }
        }

        // Turns a due date into short, human-friendly text for the
        // "Upcoming" card: "Due Today", "Due Tomorrow", or "Due <date>".
        fun relativeDueText(dueDate: String): String {
            val due = parseDate(dueDate) ?: return "Due $dueDate"
            val today = startOfDay(Date())
            val diffDays = ((due.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
            return when (diffDays) {
                0 -> "Due Today"
                1 -> "Due Tomorrow"
                else -> "Due $dueDate"
            }
        }

        private fun parseMinutes(time: String): Int? {
            val parsed = try {
                timeFormat.parse(time.trim())
            } catch (e: Exception) {
                null
            } ?: return null
            val cal = Calendar.getInstance().apply { this.time = parsed }
            return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }

        private fun parseDate(dateStr: String): Date? {
            return try {
                dueDateFormat.parse(dateStr.trim())
            } catch (e: Exception) {
                null
            }
        }

        private fun startOfDay(date: Date): Date {
            return Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }
    }
}