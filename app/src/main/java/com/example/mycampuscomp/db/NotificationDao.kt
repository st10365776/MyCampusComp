package com.example.mycampuscomp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mycampuscomp.model.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getForUser(userId: String): Flow<List<NotificationModel>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND `read` = 0")
    fun countUnread(userId: String): Flow<Int>

    // IGNORE makes deterministic ids (e.g. daily reminders) show only once; returns -1 on duplicates.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationModel): Long

    @Query("UPDATE notifications SET `read` = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET `read` = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
