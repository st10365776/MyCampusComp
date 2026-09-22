package com.example.mycampuscomp.model

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general",
    val timestamp: Long = 0L,
    val read: Boolean = false,
    val userId: String = ""
)