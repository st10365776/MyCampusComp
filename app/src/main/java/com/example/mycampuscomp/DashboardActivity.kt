package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import coil.load
import com.example.mycampuscomp.viewmodel.DashboardViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var tvUserName: TextView
    private lateinit var tvTodayClassesCount: TextView
    private lateinit var tvNextClassTime: TextView
    private lateinit var tvNextClassName: TextView
    private lateinit var tvNextClassRoom: TextView
    private lateinit var tvAssignmentsCount: TextView
    private lateinit var tvApsScore: TextView
    private lateinit var tvStudyStreak: TextView
    private lateinit var ivProfile: com.google.android.material.imageview.ShapeableImageView
    private lateinit var tvUpcomingName: TextView
    private lateinit var tvUpcomingDue: TextView
    private lateinit var tvUpcomingStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupBottomNavigation()
        setupDrawerNavigation()
        observeViewModel()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        tvUserName = findViewById(R.id.tvUserName)
        tvTodayClassesCount = findViewById(R.id.tvTodayClassesCount)
        tvNextClassTime = findViewById(R.id.tvNextClassTime)
        tvNextClassName = findViewById(R.id.tvNextClassName)
        tvNextClassRoom = findViewById(R.id.tvNextClassRoom)
        tvAssignmentsCount = findViewById(R.id.tvAssignmentsCount)
        tvApsScore = findViewById(R.id.tvApsScore)
        tvStudyStreak = findViewById(R.id.tvStudyStreak)
        ivProfile = findViewById(R.id.ivProfile)
        tvUpcomingName = findViewById(R.id.tvUpcomingName)
        tvUpcomingDue = findViewById(R.id.tvUpcomingDue)
        tvUpcomingStatus = findViewById(R.id.tvUpcomingStatus)
    }

    private fun observeViewModel() {
        val headerView = navigationView.getHeaderView(0)
        val tvDrawerName = headerView.findViewById<TextView>(R.id.tvDrawerName)
        val tvDrawerEmail = headerView.findViewById<TextView>(R.id.tvDrawerEmail)
        val ivDrawerProfile = headerView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivDrawerProfile)

        viewModel.userName.observe(this) { name ->
            tvUserName.text = "$name 👋"
            tvDrawerName.text = name
        }

        viewModel.userEmail.observe(this) { email ->
            tvDrawerEmail.text = email
        }

        viewModel.profileImageUrl.observe(this) { url ->
            if (url.isNotEmpty()) {
                ivProfile.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.logo2)
                    error(R.drawable.logo2)
                }
                ivDrawerProfile.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.logo2)
                    error(R.drawable.logo2)
                }
            }
        }

        // We can observe the raw user model for the drawer email
        // Or add it to DashboardViewModel. For now let's just use what we have.
        // Assuming we might want to add email to DashboardViewModel later.
        
        viewModel.todayClasses.observe(this) { classes ->
            tvTodayClassesCount.text = classes.size.toString()
        }

        viewModel.nextClass.observe(this) { cls ->
            if (cls != null) {
                tvNextClassTime.text = cls.time
                tvNextClassName.text = cls.subject
                tvNextClassRoom.text = cls.room
            } else {
                tvNextClassTime.text = "--:--"
                tvNextClassName.text = "No Classes"
                tvNextClassRoom.text = "Free time"
            }
        }

        viewModel.assignmentsDueThisWeek.observe(this) { count ->
            tvAssignmentsCount.text = count.toString()
        }

        viewModel.apsScore.observe(this) { score ->
            tvApsScore.text = score.toString()
        }

        viewModel.studyStreak.observe(this) { streak ->
            tvStudyStreak.text = "$streak 🔥"
        }

        viewModel.nextAssignment.observe(this) { assignment ->
            if (assignment != null) {
                tvUpcomingName.text = assignment.title
                tvUpcomingDue.text = DashboardViewModel.relativeDueText(assignment.dueDate)
                tvUpcomingStatus.text = assignment.status
            } else {
                tvUpcomingName.text = "No Upcoming Assignments"
                tvUpcomingDue.text = "All caught up!"
                tvUpcomingStatus.text = "Done"
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_timetable -> {
                    startActivity(Intent(this, TimetableActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ai -> {
                    startActivity(Intent(this, AIStudyAssistantActivity::class.java))
                    true
                }
                R.id.nav_assignments -> {
                    startActivity(Intent(this, AssignmentsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_more -> {
                    drawerLayout.openDrawer(GravityCompat.START)
                    false // Don't check/select the 'More' tab visually in bottom bar
                }
                else -> false
            }
        }
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.drawer_marketplace -> {
                    startActivity(Intent(this, MarketplaceActivity::class.java))
                    true
                }
                R.id.drawer_map -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.drawer_attendance -> {
                    startActivity(Intent(this, AttendanceActivity::class.java))
                    true
                }
                R.id.drawer_qr_scanner -> {
                    startActivity(Intent(this, QrScannerActivity::class.java))
                    true
                }
                R.id.drawer_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.drawer_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.nav_home
        viewModel.refreshData()

        if (intent.getBooleanExtra("open_drawer", false)) {
            drawerLayout.openDrawer(GravityCompat.START)
            intent.removeExtra("open_drawer")
        }
    }
}