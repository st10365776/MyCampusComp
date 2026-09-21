package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.adapter.TimetableAdapter
import com.example.mycampuscomp.adapter.TimetableWeekAdapter
import com.example.mycampuscomp.viewmodel.TimetableViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class TimetableActivity : AppCompatActivity() {

    private lateinit var dayStrip: LinearLayout
    private lateinit var dayMonday: LinearLayout
    private lateinit var dayTuesday: LinearLayout
    private lateinit var dayWednesday: LinearLayout
    private lateinit var dayThursday: LinearLayout
    private lateinit var dayFriday: LinearLayout

    private lateinit var rvTimetable: RecyclerView
    private lateinit var dayAdapter: TimetableAdapter
    private lateinit var weekAdapter: TimetableWeekAdapter
    private val viewModel: TimetableViewModel by viewModels()

    private lateinit var btnWeek: TextView
    private lateinit var btnDay: TextView
    private lateinit var fabAdd: FloatingActionButton

    // Tracks which tab is active so the classes observer knows which
    // adapter/shape to feed the incoming list into.
    private var currentMode = TimetableViewModel.ViewMode.WEEK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        initViews()
        setupRecyclerView()
        setupDaySelection()
        setupToggle()
        setupBottomNavigation()
        automateDates()
        selectMode(TimetableViewModel.ViewMode.WEEK)
        observeViewModel()
    }

    private fun initViews() {
        dayStrip = findViewById(R.id.dayStrip)
        dayMonday = findViewById(R.id.dayMonday)
        dayTuesday = findViewById(R.id.dayTuesday)
        dayWednesday = findViewById(R.id.dayWednesday)
        dayThursday = findViewById(R.id.dayThursday)
        dayFriday = findViewById(R.id.dayFriday)

        rvTimetable = findViewById(R.id.rvTimetable)
        btnWeek = findViewById(R.id.btnWeek)
        btnDay = findViewById(R.id.btnDay)
        fabAdd = findViewById(R.id.fabAdd)

        fabAdd.setOnClickListener {
            showAddClassDialog()
        }
    }

    private fun showAddClassDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etSubject = dialogView.findViewById<EditText>(R.id.etSubject)
        val etTime = dialogView.findViewById<EditText>(R.id.etTime)
        val etRoom = dialogView.findViewById<EditText>(R.id.etRoom)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val subject = etSubject.text.toString()
            val time = etTime.text.toString()
            val room = etRoom.text.toString()
            val day = spinnerDay.selectedItem.toString()
            val type = spinnerType.selectedItem.toString()

            if (subject.isNotEmpty() && time.isNotEmpty() && room.isNotEmpty()) {
                viewModel.addClass(subject, time, room, day, type)
                dialog.dismiss()
            } else {
                // Show error if needed
            }
        }

        dialog.show()
    }

    private fun setupRecyclerView() {
        dayAdapter = TimetableAdapter()
        weekAdapter = TimetableWeekAdapter()
        rvTimetable.layoutManager = LinearLayoutManager(this)
        // The active adapter is (re)assigned in selectMode().
    }

    private fun observeViewModel() {
        viewModel.classes.observe(this) { classes ->
            if (currentMode == TimetableViewModel.ViewMode.WEEK) {
                weekAdapter.submitList(TimetableWeekAdapter.buildWeekList(classes))
            } else {
                dayAdapter.submitList(classes)
            }
        }
    }

    private fun automateDates() {
        val calendar = Calendar.getInstance()
        // Set to Monday of the current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayNums = listOf(
            R.id.tvDayNumMon, R.id.tvDayNumTue, R.id.tvDayNumWed, R.id.tvDayNumThu, R.id.tvDayNumFri
        )

        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

        for (id in dayNums) {
            findViewById<TextView>(id).text = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Select current day automatically if it's a weekday
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayLayout = when (currentDayOfWeek) {
            Calendar.MONDAY -> dayMonday
            Calendar.TUESDAY -> dayTuesday
            Calendar.WEDNESDAY -> dayWednesday
            Calendar.THURSDAY -> dayThursday
            Calendar.FRIDAY -> dayFriday
            else -> dayMonday // Default to Monday on weekends
        }
        selectDay(todayLayout)
    }

    private fun setupToggle() {
        btnWeek.setOnClickListener {
            selectMode(TimetableViewModel.ViewMode.WEEK)
        }

        btnDay.setOnClickListener {
            selectMode(TimetableViewModel.ViewMode.DAY)
        }
    }

    // Switches both the visual toggle state and which adapter/data source
    // backs the RecyclerView.
    private fun selectMode(mode: TimetableViewModel.ViewMode) {
        currentMode = mode
        viewModel.setViewMode(mode)

        if (mode == TimetableViewModel.ViewMode.WEEK) {
            btnWeek.setBackgroundResource(R.drawable.bg_toggle_selected)
            btnWeek.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnWeek.setTypeface(null, android.graphics.Typeface.BOLD)

            btnDay.setBackgroundResource(0)
            btnDay.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            btnDay.setTypeface(null, android.graphics.Typeface.NORMAL)

            // Picking a single day doesn't apply to the week view.
            dayStrip.visibility = View.GONE
            rvTimetable.adapter = weekAdapter
        } else {
            btnDay.setBackgroundResource(R.drawable.bg_toggle_selected)
            btnDay.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnDay.setTypeface(null, android.graphics.Typeface.BOLD)

            btnWeek.setBackgroundResource(0)
            btnWeek.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            btnWeek.setTypeface(null, android.graphics.Typeface.NORMAL)

            dayStrip.visibility = View.VISIBLE
            rvTimetable.adapter = dayAdapter
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_timetable

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(android.content.Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_timetable -> true
                R.id.nav_map -> {
                    startActivity(android.content.Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_assignments -> {
                    startActivity(android.content.Intent(this, AssignmentsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_marketplace -> {
                    startActivity(
                        Intent(this, MarketplaceActivity::class.java)
                    )
                    finish()
                    true
                }
                R.id.nav_ai -> {
                    startActivity(
                        Intent(this, AIStudyAssistantActivity::class.java)
                    )
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun setupDaySelection() {
        dayMonday.setOnClickListener { selectDay(dayMonday) }
        dayTuesday.setOnClickListener { selectDay(dayTuesday) }
        dayWednesday.setOnClickListener { selectDay(dayWednesday) }
        dayThursday.setOnClickListener { selectDay(dayThursday) }
        dayFriday.setOnClickListener { selectDay(dayFriday) }
    }

    private fun selectDay(selectedDay: LinearLayout) {
        val days = listOf(dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday)
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

        val navyDark = ContextCompat.getColor(this, R.color.navy_dark)
        val textSecondary = ContextCompat.getColor(this, R.color.text_secondary)
        val white = ContextCompat.getColor(this, R.color.white)

        days.forEachIndexed { index, day ->
            if (day == selectedDay) {
                day.setBackgroundResource(R.drawable.day_selected)
                setDayColors(day, white, white)
                viewModel.setDay(dayNames[index])
            } else {
                day.setBackgroundResource(R.drawable.day_unselected)
                setDayColors(day, textSecondary, navyDark)
            }
        }
    }

    private fun setDayColors(day: LinearLayout, topTextColor: Int, bottomTextColor: Int) {
        if (day.childCount >= 2) {
            val topText = day.getChildAt(0) as TextView
            val bottomText = day.getChildAt(1) as TextView
            topText.setTextColor(topTextColor)
            bottomText.setTextColor(bottomTextColor)
        }
    }
}