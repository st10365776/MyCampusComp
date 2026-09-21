package com.example.mycampuscomp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mycampuscomp.model.TimetableClass
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_classes WHERE day = :day AND userId = :userId ORDER BY time ASC")
    fun getClassesForDay(day: String, userId: String): Flow<List<TimetableClass>>

    // Whole week (Mon-Fri), ordered day-by-day (not alphabetically - "Mon"
    // needs to sort before "Tue" etc.) and then by time within each day.
    @Query(
        """
        SELECT * FROM timetable_classes WHERE userId = :userId
        ORDER BY CASE day
            WHEN 'Mon' THEN 1
            WHEN 'Tue' THEN 2
            WHEN 'Wed' THEN 3
            WHEN 'Thu' THEN 4
            WHEN 'Fri' THEN 5
            WHEN 'Sat' THEN 6
            WHEN 'Sun' THEN 7
            ELSE 8
        END, time ASC
        """
    )
    fun getAllClassesForUser(userId: String): Flow<List<TimetableClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<TimetableClass>)

    // Scoped to a single user so signing in as someone else on the same
    // device doesn't wipe another account's cached classes.
    @Query("DELETE FROM timetable_classes WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)
}