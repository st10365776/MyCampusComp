package com.example.mycampuscomp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "timetable_classes")
data class TimetableClass(
    @PrimaryKey
    val id: String = "",
    val userId: String = "", // uid of the signed-in user this class belongs to
    val subject: String = "",
    val time: String = "",
    val room: String = "",
    val day: String = "", // e.g., "Mon", "Tue", etc.
    val type: String = "Lecture" // e.g., "Lecture", "Lab"
)