package com.example.mycampuscomp.model

import androidx.annotation.DrawableRes

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val isUnlocked: Boolean
)
