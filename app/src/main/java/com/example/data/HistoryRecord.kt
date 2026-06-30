package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryRecord(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val sleepHours: Float = 8.0f,
    val moodRating: Int = 3, // 1 (Worst) to 5 (Best)
    val waterMilliliters: Int = 0,
    val journalEntry: String = "",
    val notes: String = "",
    val xpGained: Int = 0,
    val completedCount: Int = 0
)
