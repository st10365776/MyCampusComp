package com.example.mycampuscomp.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val studyStreak: Int = 0,
    val apsScore: Int = 0,
    val profileImageUrl: String = ""
)