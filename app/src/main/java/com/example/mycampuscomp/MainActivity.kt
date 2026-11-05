package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            finish()
        }

        showEvents()

        // --------------------------------------------------
        // BOTTOM NAVIGATION
        // --------------------------------------------------

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
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
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("open_drawer", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private data class CampusEvent(
        val date: String,
        val title: String,
        val location: String,
        val description: String
    )

    private fun showEvents() {
        val events = listOf(
            CampusEvent("MON • 09:00", "Orientation Welcome Day", "Main Auditorium", "Meet lecturers, collect your student pack and tour the campus."),
            CampusEvent("WED • 12:30", "Career & Internship Fair", "Student Centre", "Meet employers and learn about internship and graduate opportunities."),
            CampusEvent("THU • 16:00", "Inter-Campus Sports Day", "Sports Grounds", "Soccer, netball and relay races. Everyone is welcome to cheer."),
            CampusEvent("FRI • 18:00", "Movie & Chill Night", "Student Lounge", "Free popcorn and a movie voted by students."),
            CampusEvent("SAT • 10:00", "Community Clean-Up Drive", "Campus Gardens", "Join clubs and societies to keep our campus clean and green.")
        )

        val container = findViewById<LinearLayout>(R.id.eventsContainer)
        val inflater = LayoutInflater.from(this)
        events.forEach { event ->
            val card = inflater.inflate(R.layout.item_event, container, false)
            card.findViewById<TextView>(R.id.tvEventDate).text = event.date
            card.findViewById<TextView>(R.id.tvEventTitle).text = event.title
            card.findViewById<TextView>(R.id.tvEventLocation).text = event.location
            card.findViewById<TextView>(R.id.tvEventDescription).text = event.description
            container.addView(card)
        }
    }
}