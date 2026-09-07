package com.example.mycampuscomp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class TimetableActivity : AppCompatActivity() {

    private lateinit var dayMonday: LinearLayout
    private lateinit var dayTuesday: LinearLayout
    private lateinit var dayWednesday: LinearLayout
    private lateinit var dayThursday: LinearLayout
    private lateinit var dayFriday: LinearLayout

    private lateinit var btnWeek: TextView
    private lateinit var btnDay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_timetable)

        dayMonday = findViewById(R.id.dayMonday)
        dayTuesday = findViewById(R.id.dayTuesday)
        dayWednesday = findViewById(R.id.dayWednesday)
        dayThursday = findViewById(R.id.dayThursday)
        dayFriday = findViewById(R.id.dayFriday)

        btnWeek = findViewById(R.id.btnWeek)
        btnDay = findViewById(R.id.btnDay)

        setupDaySelection()
        setupToggle()
        setupBottomNavigation()
    }

    private fun setupToggle() {
        btnWeek.setOnClickListener {
            btnWeek.setBackgroundResource(R.drawable.bg_toggle_selected)
            btnWeek.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnWeek.setTypeface(null, android.graphics.Typeface.BOLD)

            btnDay.setBackgroundResource(0)
            btnDay.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            btnDay.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        btnDay.setOnClickListener {
            btnDay.setBackgroundResource(R.drawable.bg_toggle_selected)
            btnDay.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnDay.setTypeface(null, android.graphics.Typeface.BOLD)

            btnWeek.setBackgroundResource(0)
            btnWeek.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            btnWeek.setTypeface(null, android.graphics.Typeface.NORMAL)
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
                else -> false
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

        val navyDark = ContextCompat.getColor(this, R.color.navy_dark)
        val textSecondary = ContextCompat.getColor(this, R.color.text_secondary)
        val white = ContextCompat.getColor(this, R.color.white)

        for (day in days) {
            if (day == selectedDay) {
                day.setBackgroundResource(R.drawable.day_selected)
                setDayColors(day, white, white)
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