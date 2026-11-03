package com.example.mycampuscomp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mycampuscomp.model.TimetableClass
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_classes WHERE day = :day AND userId = :userId AND syncState != 'PENDING_DELETE' ORDER BY time ASC")
    fun getClassesForDay(day: String, userId: String): Flow<List<TimetableClass>>

    @Query("SELECT * FROM timetable_classes WHERE userId = :userId AND syncState != 'PENDING_DELETE' ORDER BY CASE WHEN day = 'Mon' THEN 1 WHEN day = 'Tue' THEN 2 WHEN day = 'Wed' THEN 3 WHEN day = 'Thu' THEN 4 WHEN day = 'Fri' THEN 5 ELSE 6 END, time ASC")
    fun getAllClasses(userId: String): Flow<List<TimetableClass>>

    @Query("SELECT * FROM timetable_classes WHERE userId = :userId AND syncState != 'SYNCED'")
    suspend fun getPending(userId: String): List<TimetableClass>

    @Query("SELECT * FROM timetable_classes WHERE id = :id")
    suspend fun getById(id: String): TimetableClass?

    @Query("SELECT COUNT(*) FROM timetable_classes WHERE userId = :userId AND syncState != 'SYNCED'")
    fun countPending(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<TimetableClass>)

    @Query("UPDATE timetable_classes SET syncState = 'PENDING_DELETE' WHERE id = :classId")
    suspend fun markDeleted(classId: String)

    @Query("DELETE FROM timetable_classes WHERE id = :classId")
    suspend fun deleteClass(classId: String)

    // Only rows already in sync are replaced by a server pull; unsynced local changes are kept.
    @Query("DELETE FROM timetable_classes WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun deleteSyncedForUser(userId: String)
}