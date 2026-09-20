package com.example.mycampuscomp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mycampuscomp.model.Assignment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE userId = :userId ORDER BY dueDate ASC")
    fun getAssignmentsForUser(userId: String): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<Assignment>)

    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    suspend fun deleteAssignment(assignmentId: String)

    // Scoped to a single user so signing in as someone else on the same
    // device doesn't wipe another account's cached assignments.
    @Query("DELETE FROM assignments WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)
}