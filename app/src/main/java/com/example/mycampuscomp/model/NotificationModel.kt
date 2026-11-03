package com.example.mycampuscomp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationModel(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general",
    val timestamp: Long = 0L,
    val read: Boolean = false,
    val userId: String = ""
)