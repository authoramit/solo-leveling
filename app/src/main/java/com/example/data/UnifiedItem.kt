package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unified_items")
data class UnifiedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "STRENGTH", // STRENGTH, INTELLECT, AGILITY, VITALITY, MIND, LIFE
    val isHabit: Boolean = false,
    val isTask: Boolean = false,
    val isCalendarEvent: Boolean = false,
    val isQuest: Boolean = false,
    val date: String = "", // "yyyy-MM-dd"
    val time: String? = null, // "HH:mm"
    val durationMinutes: Int = 0, // For time-blocking
    val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val xpReward: Int = 50,
    val isCompleted: Boolean = false,
    val completionHistory: String = "", // Comma-separated list of "yyyy-MM-dd" for habits
    val recurrenceDays: String? = null, // Comma-separated: "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
    val isArchived: Boolean = false,
    val isNegative: Boolean = false, // If true, completing it subtracts XP (e.g., negative habits)
    val iconName: String = "Star",
    val colorHex: String = "#A56CFF",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Check if habit is completed on a specific date
    fun isCompletedOn(dateStr: String): Boolean {
        if (!isHabit) return isCompleted
        return completionHistory.split(",").contains(dateStr)
    }
}
