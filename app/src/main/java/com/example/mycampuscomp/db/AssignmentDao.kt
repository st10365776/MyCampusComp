package com.example.mycampuscomp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mycampuscomp.model.Assignment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE userId = :userId AND syncState != 'PENDING_DELETE' ORDER BY dueDate ASC")
    fun getAssignmentsForUser(userId: String): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE userId = :userId AND syncState != 'SYNCED'")
    suspend fun getPending(userId: String): List<Assignment>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getById(id: String): Assignment?

    @Query("SELECT COUNT(*) FROM assignments WHERE userId = :userId AND syncState != 'SYNCED'")
    fun countPending(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<Assignment>)

    @Query("UPDATE assignments SET syncState = 'PENDING_DELETE' WHERE id = :assignmentId")
    suspend fun markDeleted(assignmentId: String)

    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    suspend fun deleteAssignment(assignmentId: String)

    // Only rows already in sync are replaced by a server pull; unsynced local changes are kept.
    @Query("DELETE FROM assignments WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun deleteSyncedForUser(userId: String)
}