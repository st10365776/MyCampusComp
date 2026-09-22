package com.example.mycampuscomp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mycampuscomp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(
    private val context: Context? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val tag = "UserRepository"

    // Live-updates whenever the signed-in user's profile document changes
    // in Firestore. Null means "no signed-in user" / "not loaded yet".
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private var listener: ListenerRegistration? = null

    // Each signed-in user's profile lives at users/{uid}
    fun startListening() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(tag, "No signed-in user; cannot listen for profile changes")
            _user.value = null
            return
        }

        // Load local cached profile instantly from SharedPreferences so UI displays immediately
        if (context != null) {
            val localUser = updateDailyStudyStreak(context, getLocalUser(context, uid))
            saveUserLocally(context, localUser)
            if (_user.value != localUser) {
                _user.value = localUser
            }
        }

        listener?.remove()
        listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error listening to user profile", error)
                    return@addSnapshotListener
                }
                val remoteUser = snapshot?.toObject(User::class.java)
                val currentLocal = context?.let { getLocalUser(it, uid) } ?: _user.value

                val mergedUser = if (remoteUser != null) {
                    val mergedPoints = maxOf(remoteUser.points, currentLocal?.points ?: 0)
                    val mergedLevel = maxOf(remoteUser.level, currentLocal?.level ?: 1)
                    val mergedStreak = maxOf(remoteUser.studyStreak, currentLocal?.studyStreak ?: 0)
                    val mergedAps = maxOf(remoteUser.apsScore, currentLocal?.apsScore ?: 0)
                    val mergedBadges = (remoteUser.badges + (currentLocal?.badges ?: emptyList())).distinct()
                    val mergedName = remoteUser.name.ifEmpty { currentLocal?.name ?: "" }
                    val mergedEmail = remoteUser.email.ifEmpty { currentLocal?.email ?: "" }
                    val mergedImage = remoteUser.profileImageUrl.ifEmpty { currentLocal?.profileImageUrl ?: "" }

                    User(
                        uid = uid,
                        name = mergedName,
                        email = mergedEmail,
                        studyStreak = mergedStreak,
                        apsScore = mergedAps,
                        profileImageUrl = mergedImage,
                        points = mergedPoints,
                        level = mergedLevel,
                        badges = mergedBadges
                    )
                } else {
                    currentLocal ?: User(uid = uid)
                }

                if (_user.value != mergedUser) {
                    _user.value = mergedUser
                }
                if (context != null) {
                    saveUserLocally(context, mergedUser)
                }

                if (remoteUser != null && (mergedUser.points > remoteUser.points || mergedUser.badges.size > remoteUser.badges.size || mergedUser.studyStreak > remoteUser.studyStreak || mergedUser.apsScore > remoteUser.apsScore)) {
                    syncUserToFirestore(mergedUser)
                }
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    suspend fun updateUserProfile(name: String, profileImageUrl: String, targetContext: Context? = null) {
        val uid = auth.currentUser?.uid ?: return
        val ctx = targetContext ?: context

        val current = ctx?.let { getLocalUser(it, uid) } ?: _user.value ?: User(uid = uid)
        val updatedUser = current.copy(name = name, profileImageUrl = profileImageUrl)

        if (_user.value != updatedUser) {
            _user.postValue(updatedUser)
        }
        ctx?.let { saveUserLocally(it, updatedUser) }

        try {
            val updates = mapOf(
                "name" to name,
                "profileImageUrl" to profileImageUrl
            )
            firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(tag, "Error updating user profile in database", e)
        }
    }

    suspend fun addGamificationPoints(pointsToAdd: Int, badgeToUnlock: String? = null, targetContext: Context? = null) {
        val uid = auth.currentUser?.uid ?: return
        val ctx = targetContext ?: context

        val current = ctx?.let { getLocalUser(it, uid) } ?: _user.value ?: User(uid = uid)

        val currentPoints = current.points
        val newPoints = currentPoints + pointsToAdd

        val newLevel = when {
            newPoints >= 1000 -> 5
            newPoints >= 500 -> 4
            newPoints >= 250 -> 3
            newPoints >= 100 -> 2
            else -> 1
        }

        val currentBadges = current.badges.toMutableList()
        if (badgeToUnlock != null && !currentBadges.contains(badgeToUnlock)) {
            currentBadges.add(badgeToUnlock)
        }
        if (newPoints >= 10 && !currentBadges.contains("badge_first_points")) {
            currentBadges.add("badge_first_points")
        }
        if (newLevel >= 2 && !currentBadges.contains("badge_level2")) {
            currentBadges.add("badge_level2")
        }
        if (newLevel >= 5 && !currentBadges.contains("badge_level5")) {
            currentBadges.add("badge_level5")
        }

        val updatedUser = current.copy(
            points = newPoints,
            level = newLevel,
            badges = currentBadges
        )

        if (_user.value != updatedUser) {
            _user.postValue(updatedUser)
        }
        ctx?.let { saveUserLocally(it, updatedUser) }

        try {
            val userDocRef = firestore.collection("users").document(uid)
            val updates = mapOf(
                "points" to newPoints,
                "level" to newLevel,
                "badges" to currentBadges
            )
            userDocRef.set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(tag, "Error syncing gamification points to database", e)
        }
    }

    suspend fun updateApsScore(newApsScore: Int, targetContext: Context? = null) {
        val uid = auth.currentUser?.uid ?: return
        val ctx = targetContext ?: context

        val current = ctx?.let { getLocalUser(it, uid) } ?: _user.value ?: User(uid = uid)

        val currentBadges = current.badges.toMutableList()
        if (!currentBadges.contains("badge_aps_calculator")) {
            currentBadges.add("badge_aps_calculator")
        }
        val currentPoints = current.points
        val newPoints = currentPoints + 30
        val newLevel = when {
            newPoints >= 1000 -> 5
            newPoints >= 500 -> 4
            newPoints >= 250 -> 3
            newPoints >= 100 -> 2
            else -> 1
        }

        val updatedUser = current.copy(
            apsScore = newApsScore,
            points = newPoints,
            level = newLevel,
            badges = currentBadges
        )

        if (_user.value != updatedUser) {
            _user.postValue(updatedUser)
        }
        ctx?.let { saveUserLocally(it, updatedUser) }

        try {
            val userDocRef = firestore.collection("users").document(uid)
            val updates = mapOf(
                "apsScore" to newApsScore,
                "points" to newPoints,
                "level" to newLevel,
                "badges" to currentBadges
            )
            userDocRef.set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(tag, "Error syncing APS score to database", e)
        }
    }

    private fun syncUserToFirestore(user: User) {
        if (user.uid.isEmpty()) return
        val updates = mapOf(
            "name" to user.name,
            "email" to user.email,
            "studyStreak" to user.studyStreak,
            "apsScore" to user.apsScore,
            "profileImageUrl" to user.profileImageUrl,
            "points" to user.points,
            "level" to user.level,
            "badges" to user.badges
        )
        firestore.collection("users").document(user.uid).set(updates, SetOptions.merge())
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_UID = "uid"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_STUDY_STREAK = "study_streak"
        private const val KEY_APS_SCORE = "aps_score"
        private const val KEY_PROFILE_IMAGE_URL = "profile_image_url"
        private const val KEY_POINTS = "points"
        private const val KEY_LEVEL = "level"
        private const val KEY_BADGES = "badges"
        private const val KEY_LAST_LOGIN_DATE = "last_login_date"

        fun saveUserLocally(context: Context, user: User) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = getLocalUser(context, user.uid)

            val finalUid = user.uid.ifEmpty { existing.uid }
            val finalName = user.name.ifEmpty { existing.name }
            val finalEmail = user.email.ifEmpty { existing.email }
            val finalStreak = maxOf(user.studyStreak, existing.studyStreak)
            val finalAps = maxOf(user.apsScore, existing.apsScore)
            val finalImage = user.profileImageUrl.ifEmpty { existing.profileImageUrl }
            val finalPoints = maxOf(user.points, existing.points)
            val finalLevel = maxOf(user.level, existing.level)
            val finalBadges = (user.badges + existing.badges).distinct()

            val badgesString = finalBadges.joinToString(",")
            prefs.edit()
                .putString(KEY_UID, finalUid)
                .putString(KEY_NAME, finalName)
                .putString(KEY_EMAIL, finalEmail)
                .putInt(KEY_STUDY_STREAK, finalStreak)
                .putInt(KEY_APS_SCORE, finalAps)
                .putString(KEY_PROFILE_IMAGE_URL, finalImage)
                .putInt(KEY_POINTS, finalPoints)
                .putInt(KEY_LEVEL, finalLevel)
                .putString(KEY_BADGES, badgesString)
                .apply()
        }

        fun getLocalUser(context: Context, fallbackUid: String = ""): User {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val uid = prefs.getString(KEY_UID, fallbackUid) ?: fallbackUid
            val name = prefs.getString(KEY_NAME, "") ?: ""
            val email = prefs.getString(KEY_EMAIL, "") ?: ""
            val studyStreak = prefs.getInt(KEY_STUDY_STREAK, 0)
            val apsScore = prefs.getInt(KEY_APS_SCORE, 0)
            val profileImageUrl = prefs.getString(KEY_PROFILE_IMAGE_URL, "") ?: ""
            val points = prefs.getInt(KEY_POINTS, 0)
            val level = prefs.getInt(KEY_LEVEL, 1)
            val badgesString = prefs.getString(KEY_BADGES, "") ?: ""
            val badges = if (badgesString.isBlank()) emptyList() else badgesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            return User(
                uid = uid,
                name = name,
                email = email,
                studyStreak = studyStreak,
                apsScore = apsScore,
                profileImageUrl = profileImageUrl,
                points = points,
                level = level,
                badges = badges
            )
        }

        fun updateDailyStudyStreak(context: Context, currentUser: User): User {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val lastDateStr = prefs.getString(KEY_LAST_LOGIN_DATE, "") ?: ""

            var streak = currentUser.studyStreak
            if (lastDateStr.isEmpty()) {
                streak = maxOf(streak, 1)
            } else if (lastDateStr != todayStr) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                try {
                    val lastDate = sdf.parse(lastDateStr)
                    val today = sdf.parse(todayStr)
                    if (lastDate != null && today != null) {
                        val diffDays = ((today.time - lastDate.time) / (1000 * 60 * 60 * 24)).toInt()
                        streak = when (diffDays) {
                            1 -> streak + 1
                            0 -> streak
                            else -> 1
                        }
                    }
                } catch (e: Exception) {
                    streak = maxOf(streak, 1)
                }
            }
            prefs.edit().putString(KEY_LAST_LOGIN_DATE, todayStr).apply()
            return currentUser.copy(studyStreak = streak)
        }

        fun saveProfileImageUrlLocally(context: Context, url: String) {
            val user = getLocalUser(context)
            saveUserLocally(context, user.copy(profileImageUrl = url))
        }

        fun getLocalProfileImageUrl(context: Context): String {
            return getLocalUser(context).profileImageUrl
        }
    }
}
