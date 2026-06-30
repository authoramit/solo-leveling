package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single profile row
    val level: Int = 1,
    val xp: Int = 0,
    val activeTitle: String = "E-Rank Hunter",
    val skillPoints: Int = 5,
    // Attributes
    val strength: Int = 10,
    val intelligence: Int = 10,
    val agility: Int = 10,
    val sense: Int = 10,
    val vitality: Int = 10,
    // Streaks
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val perfectDaysCount: Int = 0,
    val lastActiveDate: String? = null
)
