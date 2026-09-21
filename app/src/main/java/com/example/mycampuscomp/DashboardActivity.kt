package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.TimetableClass
import com.example.mycampuscomp.viewmodel.DashboardViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var tvUserName: TextView
    private lateinit var tvTodayClassesCount: TextView
    private lateinit var tvNextClassLabel: TextView
    private lateinit var tvNextClassTime: TextView
    private lateinit var tvNextClassName: TextView
    private lateinit var tvNextClassRoom: TextView
    private lateinit var tvAssignmentsCount: TextView
    private lateinit var tvStudyStreakCount: TextView
    private lateinit var tvUpcomingTitle: TextView
    private lateinit var cardUpcomingItem: MaterialCardView
    private lateinit var tvUpcomingName: TextView
    private lateinit var tvUpcomingDue: TextView
    private lateinit var cardUpcomingStatus: MaterialCardView
    private lateinit var tvUpcomingStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        initViews()
        setupBottomNavigation()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Pick up anything added/changed on other screens (e.g. a new
        // assignment or class) since the dashboard was last shown.
        viewModel.refreshData()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)

        tvTodayClassesCount = findViewById(R.id.tvTodayClassesCount)
        tvNextClassLabel = findViewById(R.id.tvNextClassLabel)
        tvNextClassTime = findViewById(R.id.tvNextClassTime)
        tvNextClassName = findViewById(R.id.tvNextClassName)
        tvNextClassRoom = findViewById(R.id.tvNextClassRoom)

        tvAssignmentsCount = findViewById(R.id.tvAssignmentsCount)
        tvStudyStreakCount = findViewById(R.id.tvStudyStreakCount)

        tvUpcomingTitle = findViewById(R.id.tvUpcomingTitle)
        cardUpcomingItem = findViewById(R.id.cardUpcomingItem)
        tvUpcomingName = findViewById(R.id.tvUpcomingName)
        tvUpcomingDue = findViewById(R.id.tvUpcomingDue)
        cardUpcomingStatus = findViewById(R.id.cardUpcomingStatus)
        tvUpcomingStatus = findViewById(R.id.tvUpcomingStatus)

        // Avatar stays a static placeholder image for now - no profile
        // photo upload exists yet.
        findViewById<ShapeableImageView>(R.id.ivProfile)
    }

    private fun observeViewModel() {
        viewModel.userName.observe(this) { name ->
            tvUserName.text = getString(R.string.dashboard_greeting_name, name)
        }

        viewModel.studyStreak.observe(this) { streak ->
            tvStudyStreakCount.text = getString(R.string.dashboard_study_streak, streak)
        }

        viewModel.todayClasses.observe(this) { classes ->
            tvTodayClassesCount.text = classes.size.toString()
        }

        viewModel.nextClass.observe(this) { nextClass ->
            bindNextClass(nextClass)
        }

        viewModel.assignmentsDueThisWeek.observe(this) { count ->
            tvAssignmentsCount.text = count.toString()
        }

        viewModel.nextAssignment.observe(this) { assignment ->
            bindUpcomingAssignment(assignment)
        }
    }

    private fun bindNextClass(nextClass: TimetableClass?) {
        if (nextClass == null) {
            tvNextClassLabel.text = getString(R.string.dashboard_next_class_label)
            tvNextClassTime.text = ""
            tvNextClassName.text = getString(R.string.dashboard_no_classes_today)
            tvNextClassRoom.text = ""
        } else {
            tvNextClassLabel.text = getString(R.string.dashboard_next_class_label)
            tvNextClassTime.text = nextClass.time
            tvNextClassName.text = nextClass.subject
            tvNextClassRoom.text = nextClass.room
        }
    }

    private fun bindUpcomingAssignment(assignment: Assignment?) {
        if (assignment == null) {
            cardUpcomingItem.visibility = android.view.View.VISIBLE
            tvUpcomingName.text = getString(R.string.dashboard_no_upcoming_assignments)
            tvUpcomingDue.text = ""
            cardUpcomingStatus.visibility = android.view.View.GONE
            return
        }

        cardUpcomingStatus.visibility = android.view.View.VISIBLE
        tvUpcomingName.text = assignment.title
        tvUpcomingDue.text = DashboardViewModel.relativeDueText(assignment.dueDate)
        tvUpcomingStatus.text = assignment.status

        val (bgColor, textColor) = when (assignment.status) {
            "Urgent" -> R.color.status_urgent_bg to R.color.status_urgent_text
            "Completed" -> R.color.status_completed_bg to R.color.status_completed_text
            else -> R.color.status_pending_bg to R.color.status_pending_text
        }
        cardUpcomingStatus.setCardBackgroundColor(ContextCompat.getColor(this, bgColor))
        tvUpcomingStatus.setTextColor(ContextCompat.getColor(this, textColor))
    }

    private fun setupBottomNavigation() {
        val bottomNavigation =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Keep Home selected when Dashboard opens
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_timetable -> {
                    startActivity(Intent(this, TimetableActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_assignments -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_marketplace -> {
                    startActivity(Intent(this, MarketplaceActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ai -> {
                    startActivity(Intent(this, AIStudyAssistantActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}