package com.example.mycampuscomp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey
    val id: String = "",
    val userId: String = "", // uid of the signed-in user this assignment belongs to
    val title: String = "",
    val dueDate: String = "", // e.g. "Oct 12, 2026"
    val status: String = "Pending" // "Pending", "Urgent", or "Completed"
)