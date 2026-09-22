package com.example.mycampuscomp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.adapter.BadgeAdapter
import com.example.mycampuscomp.model.Badge
import com.example.mycampuscomp.repository.UserRepository
import com.example.mycampuscomp.utils.NetworkUtils

class GamificationActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var layoutOfflineBanner: LinearLayout
    private lateinit var tvUserLevelTitle: TextView
    private lateinit var tvUserPoints: TextView
    private lateinit var tvLevelProgressText: TextView
    private lateinit var pbLevelProgress: ProgressBar
    private lateinit var rvBadges: RecyclerView

    private lateinit var userRepository: UserRepository
    private val badgeAdapter = BadgeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamification)

        userRepository = UserRepository(context = applicationContext)

        btnBack = findViewById(R.id.btnBackGamification)
        layoutOfflineBanner = findViewById(R.id.layoutOfflineBannerGamification)
        tvUserLevelTitle = findViewById(R.id.tvUserLevelTitle)
        tvUserPoints = findViewById(R.id.tvUserPoints)
        tvLevelProgressText = findViewById(R.id.tvLevelProgressText)
        pbLevelProgress = findViewById(R.id.pbLevelProgress)
        rvBadges = findViewById(R.id.rvBadges)

        btnBack.setOnClickListener { onBackPressed() }

        rvBadges.layoutManager = GridLayoutManager(this, 2)
        rvBadges.adapter = badgeAdapter

        userRepository.startListening()
        userRepository.user.observe(this) { user ->
            if (user != null) {
                updateUI(user.points, user.level, user.badges)
            } else {
                updateUI(0, 1, emptyList())
            }
        }

        NetworkUtils.NetworkStateLiveData(this).observe(this) { isConnected ->
            layoutOfflineBanner.visibility = if (isConnected) View.GONE else View.VISIBLE
        }
    }

    private fun updateUI(points: Int, level: Int, unlockedBadgeIds: List<String>) {
        val levelName = when (level) {
            1 -> "Level 1 Explorer"
            2 -> "Level 2 Scholar"
            3 -> "Level 3 Achiever"
            4 -> "Level 4 Expert"
            5 -> "Level 5 Master Scholar"
            else -> "Level $level Scholar"
        }
        tvUserLevelTitle.text = levelName
        tvUserPoints.text = "$points Total Points"

        // Next level thresholds: L1: 100, L2: 250, L3: 500, L4: 1000, L5+: Max
        val (currentBase, targetNext) = when (level) {
            1 -> 0 to 100
            2 -> 100 to 250
            3 -> 250 to 500
            4 -> 500 to 1000
            else -> 1000 to 1000
        }

        if (level >= 5) {
            tvLevelProgressText.text = "Max Level Reached!"
            pbLevelProgress.max = 100
            pbLevelProgress.progress = 100
        } else {
            val range = targetNext - currentBase
            val currentProgress = (points - currentBase).coerceIn(0, range)
            val progressPercent = ((currentProgress.toDouble() / range) * 100).toInt()
            pbLevelProgress.max = 100
            pbLevelProgress.progress = progressPercent
            tvLevelProgressText.text = "Progress to Level ${level + 1} ($points / $targetNext Pts)"
        }

        val allBadges = listOf(
            Badge("badge_first_points", "Point Collector", "Earned your first points", "🌟", unlockedBadgeIds.contains("badge_first_points") || points > 0),
            Badge("badge_first_assignment", "Task Master", "Completed an assignment", "✅", unlockedBadgeIds.contains("badge_first_assignment")),
            Badge("badge_ai_study", "AI Scholar", "Used AI Study Assistant", "🤖", unlockedBadgeIds.contains("badge_ai_study")),
            Badge("badge_aps_calculator", "APS Planner", "Saved your APS calculation", "📊", unlockedBadgeIds.contains("badge_aps_calculator")),
            Badge("badge_level2", "Rising Star", "Reached Level 2", "⭐", unlockedBadgeIds.contains("badge_level2") || level >= 2),
            Badge("badge_level5", "Master Scholar", "Reached Level 5", "👑", unlockedBadgeIds.contains("badge_level5") || level >= 5)
        )

        badgeAdapter.updateBadges(allBadges)
    }

    override fun onDestroy() {
        super.onDestroy()
        userRepository.stopListening()
    }
}
