package com.example.mycampuscomp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey
    val id: String = "",
    val userId: String = "", // uid of the signed-in user this assignment belongs to
    val title: String = "",
    val dueDate: String = "", // e.g. "Oct 12, 2026"
    val status: String = "Pending", // "Pending", "Urgent", or "Completed"
    // Local-only sync flag, never written to Firestore.
    @get:Exclude
    @ColumnInfo(defaultValue = "SYNCED")
    val syncState: String = SyncState.SYNCED
)