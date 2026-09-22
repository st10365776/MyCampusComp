package com.example.mycampuscomp.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val studyStreak: Int = 0,
    val apsScore: Int = 0,
    val profileImageUrl: String = "",
    val points: Int = 0,
    val level: Int = 1,
    val badges: List<String> = emptyList()
)