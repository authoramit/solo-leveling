package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SoloLevelingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SoloLevelingRepository

    // Central Flows from Repository
    val allActiveItems: StateFlow<List<UnifiedItem>>
    val allHistoryRecords: StateFlow<List<HistoryRecord>>
    val userProfile: StateFlow<UserProfile>

    // Current Selected Date (defaults to today)
    var selectedDateStr by mutableStateOf(getTodayDateString())
        private set

    // Active Dashboard Theme
    var currentTheme by mutableStateOf("Solo Leveling")
        private set

    // Active Dashboard Layout Configuration (Widget list)
    var dashboardWidgets by mutableStateOf(getDefaultWidgets())
        private set

    // Pomodoro Timer State
    var pomodoroSecondsLeft by mutableStateOf(1500) // 25 min default
    var pomodoroIsRunning by mutableStateOf(false)
    var pomodoroMode by mutableStateOf("WORK") // WORK, BREAK
    var pomodoroTotalSeconds by mutableStateOf(1500)

    // Companion Bot Current Message
    var botMessage by mutableStateOf("Welcome back, Monarch. System is fully operational.")
        private set

    // Current unlocked achievements list
    var unlockedAchievements by mutableStateOf(setOf<String>())
        private set

    // Notification toast message state
    var toastMessage by mutableStateOf<String?>(null)
        private set

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SoloLevelingRepository(database)

        // Bind flows from repo
        allActiveItems = repository.allActiveItems
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allHistoryRecords = repository.allHistoryRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Ensure user profile exists
        userProfile = repository.userProfileFlow
            .map { it ?: UserProfile() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

        // Initial launch routines
        viewModelScope.launch {
            checkAndPrepopulateDatabase()
            delay(1000)
            evaluateGameAchievements()
            evaluateStreaks()
            triggerBotAdvice()
        }

        // Launch Pomodoro ticker
        startPomodoroTicker()
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun selectDate(dateStr: String) {
        selectedDateStr = dateStr
        viewModelScope.launch {
            triggerBotAdvice()
        }
    }

    fun updateTheme(themeName: String) {
        currentTheme = themeName
    }

    // Toggle widgets in layout
    fun toggleWidgetVisibility(widgetId: String) {
        dashboardWidgets = dashboardWidgets.map {
            if (it.id == widgetId) it.copy(visible = !it.visible) else it
        }
    }

    fun moveWidgetOrder(widgetId: String, moveUp: Boolean) {
        val list = dashboardWidgets.toMutableList()
        val index = list.indexOfFirst { it.id == widgetId }
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in 0 until list.size) {
            val temp = list[index]
            list[index] = list[targetIndex]
            list[targetIndex] = temp
            dashboardWidgets = list
        }
    }

    // Unified Item Creation / Edits
    fun createOrUpdateItem(item: UnifiedItem) {
        viewModelScope.launch {
            if (item.id == 0) {
                repository.insertItem(item)
                showToast("System Quest created: ${item.title}")
            } else {
                repository.updateItem(item)
                showToast("System Quest updated: ${item.title}")
            }
            evaluateGameAchievements()
            triggerBotAdvice()
        }
    }

    fun deleteItem(item: UnifiedItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            showToast("System Quest removed: ${item.title}")
            evaluateGameAchievements()
            triggerBotAdvice()
        }
    }

    // Toggle item completion
    fun toggleItemCompletion(item: UnifiedItem) {
        viewModelScope.launch {
            val todayStr = getTodayDateString()
            if (item.isHabit) {
                // Habit Toggle Completion on selectedDateStr
                val dates = item.completionHistory.split(",").filter { it.isNotEmpty() }.toMutableSet()
                val isCompleting = !dates.contains(selectedDateStr)
                
                if (isCompleting) {
                    dates.add(selectedDateStr)
                    // If it is a negative habit, subtract XP
                    if (item.isNegative) {
                        modifyXp(-item.xpReward)
                        showToast("Negative Habit Triggered: -${item.xpReward} XP!")
                    } else {
                        modifyXp(item.xpReward)
                        incrementMetricCompletedCount(selectedDateStr)
                        // STRENGTH, INTELLECT, AGILITY etc increments
                        awardCategoryStatChance(item.category)
                        showToast("Habit Cleared: +${item.xpReward} XP!")
                    }
                } else {
                    dates.remove(selectedDateStr)
                    if (item.isNegative) {
                        modifyXp(item.xpReward) // Reverse
                    } else {
                        modifyXp(-item.xpReward)
                        decrementMetricCompletedCount(selectedDateStr)
                    }
                }
                repository.updateItem(item.copy(completionHistory = dates.joinToString(",")))
            } else {
                // Regular Task / Event completion
                val isCompleting = !item.isCompleted
                if (isCompleting) {
                    modifyXp(item.xpReward)
                    incrementMetricCompletedCount(selectedDateStr)
                    awardCategoryStatChance(item.category)
                    showToast("Quest Complete: +${item.xpReward} XP!")
                } else {
                    modifyXp(-item.xpReward)
                    decrementMetricCompletedCount(selectedDateStr)
                }
                repository.updateItem(item.copy(isCompleted = isCompleting))
            }
            evaluateGameAchievements()
            triggerBotAdvice()
        }
    }

    // Allocate Skill points
    fun allocateSkillPoint(attribute: String) {
        viewModelScope.launch {
            val prof = userProfile.value
            if (prof.skillPoints > 0) {
                val updatedProf = when (attribute.uppercase()) {
                    "STRENGTH" -> prof.copy(strength = prof.strength + 1, skillPoints = prof.skillPoints - 1)
                    "INTELLIGENCE" -> prof.copy(intelligence = prof.intelligence + 1, skillPoints = prof.skillPoints - 1)
                    "AGILITY" -> prof.copy(agility = prof.agility + 1, skillPoints = prof.skillPoints - 1)
                    "SENSE" -> prof.copy(sense = prof.sense + 1, skillPoints = prof.skillPoints - 1)
                    "VITALITY" -> prof.copy(vitality = prof.vitality + 1, skillPoints = prof.skillPoints - 1)
                    else -> prof
                }
                repository.saveUserProfile(updatedProf)
                showToast("Stat Allocated: $attribute Up!")
                evaluateGameAchievements()
            }
        }
    }

    // Daily metric tracking
    fun updateSleepHours(dateStr: String, hours: Float) {
        viewModelScope.launch {
            val rec = repository.getHistoryRecord(dateStr)
            repository.insertOrUpdateHistory(rec.copy(sleepHours = hours))
            evaluateGameAchievements()
        }
    }

    fun updateMoodRating(dateStr: String, rating: Int) {
        viewModelScope.launch {
            val rec = repository.getHistoryRecord(dateStr)
            repository.insertOrUpdateHistory(rec.copy(moodRating = rating))
            evaluateGameAchievements()
        }
    }

    fun updateWaterMilliliters(dateStr: String, ml: Int) {
        viewModelScope.launch {
            val rec = repository.getHistoryRecord(dateStr)
            repository.insertOrUpdateHistory(rec.copy(waterMilliliters = ml))
            evaluateGameAchievements()
            triggerBotAdvice()
        }
    }

    fun updateJournalEntry(dateStr: String, entry: String) {
        viewModelScope.launch {
            val rec = repository.getHistoryRecord(dateStr)
            repository.insertOrUpdateHistory(rec.copy(journalEntry = entry))
            evaluateGameAchievements()
        }
    }

    fun updateDailyNotes(dateStr: String, notes: String) {
        viewModelScope.launch {
            val rec = repository.getHistoryRecord(dateStr)
            repository.insertOrUpdateHistory(rec.copy(notes = notes))
        }
    }

    // Reset whole app data
    fun resetAllData() {
        viewModelScope.launch {
            // Delete all and seed
            val database = AppDatabase.getDatabase(getApplication())
            database.clearAllTables()
            checkAndPrepopulateDatabase()
            showToast("System Operating System Re-Initialized.")
        }
    }

    // JSON export/import data backup offline
    fun exportBackupString(): String {
        // Build a beautiful structured text of all items, user progress, and history records
        // To avoid bringing a heavy JSON parser manually, we format it with a robust and highly readable custom format
        val profile = userProfile.value
        val items = allActiveItems.value
        val history = allHistoryRecords.value

        val sb = StringBuilder()
        sb.append("--- SOLO LEVELING OS BACKUP ---\n")
        sb.append("VERSION: 1\n")
        sb.append("PROFILE: ")
        sb.append("level=${profile.level};xp=${profile.xp};title=${profile.activeTitle};skillPoints=${profile.skillPoints};str=${profile.strength};intel=${profile.intelligence};agi=${profile.agility};sense=${profile.sense};vit=${profile.vitality};streak=${profile.currentStreak};maxStreak=${profile.longestStreak}\n")
        
        sb.append("ITEMS:\n")
        items.forEach {
            sb.append("ITEM: id=${it.id};title=${it.title};desc=${it.description};category=${it.category};habit=${it.isHabit};task=${it.isTask};calendar=${it.isCalendarEvent};quest=${it.isQuest};date=${it.date};time=${it.time ?: ""};duration=${it.durationMinutes};difficulty=${it.difficulty};priority=${it.priority};xp=${it.xpReward};isCompleted=${it.isCompleted};history=${it.completionHistory};recurrence=${it.recurrenceDays ?: ""};neg=${it.isNegative};icon=${it.iconName};color=${it.colorHex};notes=${it.notes}\n")
        }

        sb.append("HISTORY:\n")
        history.forEach {
            sb.append("REC: date=${it.date};sleep=${it.sleepHours};mood=${it.moodRating};water=${it.waterMilliliters};journal=${it.journalEntry.replace("\n", "[BR]")};notes=${it.notes.replace("\n", "[BR]")};xpGained=${it.xpGained};completed=${it.completedCount}\n")
        }

        return sb.toString()
    }

    fun importBackupString(backup: String): Boolean {
        try {
            val lines = backup.lines()
            if (lines.isEmpty() || !lines[0].contains("SOLO LEVELING OS BACKUP")) return false

            viewModelScope.launch {
                val db = AppDatabase.getDatabase(getApplication())
                db.clearAllTables()

                var tempProfile = UserProfile()

                lines.forEach { line ->
                    if (line.startsWith("PROFILE: ")) {
                        val parts = line.removePrefix("PROFILE: ").split(";")
                        val map = parts.associate {
                            val kv = it.split("=")
                            kv[0] to kv.getOrElse(1) { "" }
                        }
                        tempProfile = UserProfile(
                            level = map["level"]?.toIntOrNull() ?: 1,
                            xp = map["xp"]?.toIntOrNull() ?: 0,
                            activeTitle = map["title"] ?: "E-Rank Hunter",
                            skillPoints = map["skillPoints"]?.toIntOrNull() ?: 5,
                            strength = map["str"]?.toIntOrNull() ?: 10,
                            intelligence = map["intel"]?.toIntOrNull() ?: 10,
                            agility = map["agi"]?.toIntOrNull() ?: 10,
                            sense = map["sense"]?.toIntOrNull() ?: 10,
                            vitality = map["vit"]?.toIntOrNull() ?: 10,
                            currentStreak = map["streak"]?.toIntOrNull() ?: 0,
                            longestStreak = map["maxStreak"]?.toIntOrNull() ?: 0
                        )
                        repository.saveUserProfile(tempProfile)
                    } else if (line.startsWith("ITEM: ")) {
                        val parts = line.removePrefix("ITEM: ").split(";")
                        val map = parts.associate {
                            val kv = it.split("=")
                            kv[0] to kv.getOrElse(1) { "" }
                        }
                        val item = UnifiedItem(
                            title = map["title"] ?: "Quest",
                            description = map["desc"] ?: "",
                            category = map["category"] ?: "STRENGTH",
                            isHabit = map["habit"]?.toBoolean() ?: false,
                            isTask = map["task"]?.toBoolean() ?: false,
                            isCalendarEvent = map["calendar"]?.toBoolean() ?: false,
                            isQuest = map["quest"]?.toBoolean() ?: false,
                            date = map["date"] ?: "",
                            time = map["time"]?.ifEmpty { null },
                            durationMinutes = map["duration"]?.toIntOrNull() ?: 0,
                            difficulty = map["difficulty"] ?: "MEDIUM",
                            priority = map["priority"] ?: "MEDIUM",
                            xpReward = map["xp"]?.toIntOrNull() ?: 50,
                            isCompleted = map["isCompleted"]?.toBoolean() ?: false,
                            completionHistory = map["history"] ?: "",
                            recurrenceDays = map["recurrence"]?.ifEmpty { null },
                            isNegative = map["neg"]?.toBoolean() ?: false,
                            iconName = map["icon"] ?: "Star",
                            colorHex = map["color"] ?: "#A56CFF",
                            notes = map["notes"] ?: ""
                        )
                        repository.insertItem(item)
                    } else if (line.startsWith("REC: ")) {
                        val parts = line.removePrefix("REC: ").split(";")
                        val map = parts.associate {
                            val kv = it.split("=")
                            kv[0] to kv.getOrElse(1) { "" }
                        }
                        val rec = HistoryRecord(
                            date = map["date"] ?: "",
                            sleepHours = map["sleep"]?.toFloatOrNull() ?: 8.0f,
                            moodRating = map["mood"]?.toIntOrNull() ?: 3,
                            waterMilliliters = map["water"]?.toIntOrNull() ?: 0,
                            journalEntry = map["journal"]?.replace("[BR]", "\n") ?: "",
                            notes = map["notes"]?.replace("[BR]", "\n") ?: "",
                            xpGained = map["xpGained"]?.toIntOrNull() ?: 0,
                            completedCount = map["completed"]?.toIntOrNull() ?: 0
                        )
                        repository.insertOrUpdateHistory(rec)
                    }
                }
                showToast("Backup Restored Successfully!")
                evaluateGameAchievements()
                triggerBotAdvice()
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    // Internals: Game progression loops
    private suspend fun modifyXp(amount: Int) {
        val prof = repository.getUserProfile()
        var newXp = prof.xp + amount
        var newLevel = prof.level
        var skillPointsGained = 0

        // Handle negative leveling boundary
        if (newXp < 0) {
            if (newLevel > 1) {
                newLevel--
                newXp = GameConfig.getXpNeededForLevel(newLevel) + newXp
            } else {
                newXp = 0
            }
        } else {
            // Level up cycle
            var xpRequired = GameConfig.getXpNeededForLevel(newLevel)
            while (newXp >= xpRequired) {
                newXp -= xpRequired
                newLevel++
                skillPointsGained += 5 // 5 stat points per level!
                xpRequired = GameConfig.getXpNeededForLevel(newLevel)
                
                // Triggers an immediate level-up flash notification
                showToast("LEVEL UP! Reached Level $newLevel! +5 Skill Points.")
            }
        }

        // Auto rank promotion based on level
        val correspondingTitle = GameConfig.titlesList
            .filter { newLevel >= it.minLevel }
            .maxByOrNull { it.minLevel }?.title ?: prof.activeTitle

        repository.saveUserProfile(
            prof.copy(
                level = newLevel,
                xp = newXp,
                skillPoints = prof.skillPoints + skillPointsGained,
                activeTitle = correspondingTitle
            )
        )

        // Track daily earned XP
        val todayStr = getTodayDateString()
        val record = repository.getHistoryRecord(todayStr)
        repository.insertOrUpdateHistory(record.copy(xpGained = record.xpGained + amount))
    }

    private suspend fun incrementMetricCompletedCount(dateStr: String) {
        val record = repository.getHistoryRecord(dateStr)
        repository.insertOrUpdateHistory(record.copy(completedCount = record.completedCount + 1))
    }

    private suspend fun decrementMetricCompletedCount(dateStr: String) {
        val record = repository.getHistoryRecord(dateStr)
        repository.insertOrUpdateHistory(record.copy(completedCount = (record.completedCount - 1).coerceAtLeast(0)))
    }

    private suspend fun awardCategoryStatChance(category: String) {
        // Every completed item has a 10% chance to award +1 in that attribute node directly
        if (Random().nextInt(10) == 0) {
            val prof = repository.getUserProfile()
            val updated = when (category.uppercase()) {
                "STRENGTH" -> prof.copy(strength = prof.strength + 1)
                "INTELLECT", "KNOWLEDGE" -> prof.copy(intelligence = prof.intelligence + 1)
                "AGILITY" -> prof.copy(agility = prof.agility + 1)
                "MIND", "SENSE" -> prof.copy(sense = prof.sense + 1)
                "LIFE", "VITALITY" -> prof.copy(vitality = prof.vitality + 1)
                else -> prof
            }
            repository.saveUserProfile(updated)
            showToast("Stat Node Upgrade: ${category.uppercase()} +1!")
        }
    }

    private fun startPomodoroTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (pomodoroIsRunning) {
                    if (pomodoroSecondsLeft > 0) {
                        pomodoroSecondsLeft--
                    } else {
                        // Timer finished!
                        pomodoroIsRunning = false
                        if (pomodoroMode == "WORK") {
                            modifyXp(75) // Award XP for deep work focus session!
                            showToast("Focus Session Completed! +75 XP.")
                            pomodoroMode = "BREAK"
                            pomodoroSecondsLeft = 300 // 5 min break
                            pomodoroTotalSeconds = 300
                        } else {
                            showToast("Break Ended. Ready to grind again?")
                            pomodoroMode = "WORK"
                            pomodoroSecondsLeft = 1500
                            pomodoroTotalSeconds = 1500
                        }
                    }
                }
            }
        }
    }

    private suspend fun evaluateStreaks() {
        // Calculate daily login streaks based on completion histories or active logs
        val prof = repository.getUserProfile()
        val todayStr = getTodayDateString()
        val records = repository.allHistoryRecords.firstOrNull() ?: emptyList()

        if (records.isNotEmpty()) {
            // Find active streaks by scanning consecutive days with completed tasks/habits
            val datesWithCompletions = records.filter { it.completedCount > 0 }.map { it.date }.toSet()
            
            var streak = 0
            val calendar = Calendar.getInstance()
            
            // Check backwards from today or yesterday
            var checkingDate = getTodayDateString()
            if (!datesWithCompletions.contains(checkingDate)) {
                // If today is empty, check starting from yesterday to keep streak active
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                checkingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            }

            while (datesWithCompletions.contains(checkingDate)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                checkingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            }

            val maxStreak = if (streak > prof.longestStreak) streak else prof.longestStreak
            repository.saveUserProfile(prof.copy(currentStreak = streak, longestStreak = maxStreak, lastActiveDate = todayStr))
        }
    }

    private suspend fun evaluateGameAchievements() {
        val records = repository.allHistoryRecords.firstOrNull() ?: emptyList()
        val items = repository.allActiveItems.firstOrNull() ?: emptyList()
        val profile = repository.getUserProfile()

        val unlocked = mutableSetOf<String>()

        // 1. FIRST_QUEST: complete at least one item (record exists with completed count > 0)
        if (records.any { it.completedCount > 0 }) unlocked.add("FIRST_QUEST")

        // 2. LEVEL_5: Reach level 5
        if (profile.level >= 5) unlocked.add("LEVEL_5")

        // 3. STREAK_3 / STREAK_7
        if (profile.currentStreak >= 3) unlocked.add("STREAK_3")
        if (profile.currentStreak >= 7) unlocked.add("STREAK_7")

        // 4. HYDRATION: track 2000ml of water
        if (records.any { it.waterMilliliters >= 2000 }) unlocked.add("HYDRATION")

        // 5. JOURNAL: write first journal entry
        if (records.any { it.journalEntry.trim().isNotEmpty() }) unlocked.add("JOURNAL")

        // 6. SLEEP_GOOD: track 8+ hours of sleep for 3 separate days
        if (records.filter { it.sleepHours >= 8.0f }.size >= 3) unlocked.add("SLEEP_GOOD")

        // 7. MAX_STATS: allocate 20+ skill points total (sum of strength, intelligence, agility, sense, vitality minus 50 starting points)
        val totalStatsAllocated = (profile.strength + profile.intelligence + profile.agility + profile.sense + profile.vitality) - 50
        if (totalStatsAllocated >= 20) unlocked.add("MAX_STATS")

        // Check for newly unlocked ones and award XP!
        unlocked.forEach { achId ->
            if (!unlockedAchievements.contains(achId)) {
                val ach = GameConfig.achievementsList.firstOrNull { it.id == achId }
                if (ach != null) {
                    modifyXp(ach.xpReward)
                    showToast("ACHIEVEMENT UNLOCKED: ${ach.name}! +${ach.xpReward} XP.")
                }
            }
        }
        unlockedAchievements = unlocked
    }

    private fun triggerBotAdvice() {
        val todayStr = selectedDateStr
        viewModelScope.launch {
            val record = repository.getHistoryRecord(todayStr)
            val profile = userProfile.value
            val activeItems = allActiveItems.value.filter { !it.isArchived }

            val advice = when {
                record.waterMilliliters == 0 -> "Hydration is essential for optimal battle efficiency. Track your water, Monarch."
                record.sleepHours < 6.0f -> "Analyzing biometric data: Dangerously low sleep sleep cycle. Monarch, seek rest to avoid fatigue debuff."
                profile.skillPoints > 0 -> "Alert: Unallocated points detected! Access your status window and upgrade your attributes."
                activeItems.any { !it.isCompleted && it.priority == "HIGH" } -> "Urgent: High priority quests remain unfulfilled on today's bulletin."
                record.completedCount >= 5 -> "Astounding productivity rate today, Monarch! Your growth surpasses the S-Rank ceiling."
                profile.currentStreak >= 3 -> "Streaks empower your core shadow summons. Keep clearing quests to sustain the multiplier."
                else -> "The hunter's path is carved in daily discipline. Rise, and complete your trials."
            }
            botMessage = advice
        }
    }

    private fun showToast(msg: String) {
        toastMessage = msg
        viewModelScope.launch {
            delay(3000)
            if (toastMessage == msg) {
                toastMessage = null
            }
        }
    }

    fun clearToast() {
        toastMessage = null
    }

    // Default prepopulate helper
    private suspend fun checkAndPrepopulateDatabase() {
        val prof = repository.getUserProfile()
        val allItemsList = repository.allItems.firstOrNull() ?: emptyList()
        val todayStr = getTodayDateString()

        if (allItemsList.isEmpty()) {
            // Seed profile
            repository.saveUserProfile(UserProfile(level = 1, xp = 10, currentStreak = 1))

            // Seed items
            repository.insertItem(UnifiedItem(
                title = "Study Programming",
                description = "Deep study session in Kotlin & Compose Architecture",
                category = "INTELLECT",
                isHabit = false,
                isTask = true,
                isCalendarEvent = true,
                date = todayStr,
                time = "10:00",
                durationMinutes = 120,
                difficulty = "HARD",
                priority = "HIGH",
                xpReward = 150,
                iconName = "Code",
                colorHex = "#A56CFF"
            ))

            repository.insertItem(UnifiedItem(
                title = "Iron Temple Grinding",
                description = "Push limits at the gym: Deadlifts and Squats",
                category = "STRENGTH",
                isHabit = true,
                isTask = false,
                isCalendarEvent = false,
                difficulty = "MEDIUM",
                priority = "MEDIUM",
                xpReward = 100,
                recurrenceDays = "Mon,Wed,Fri",
                iconName = "FitnessCenter",
                colorHex = "#FF8C5A"
            ))

            repository.insertItem(UnifiedItem(
                title = "Morning Meditation",
                description = "Deep breath focus, clearing the mind flow",
                category = "MIND",
                isHabit = true,
                isTask = false,
                isCalendarEvent = false,
                difficulty = "EASY",
                priority = "LOW",
                xpReward = 50,
                recurrenceDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                iconName = "Psychology",
                colorHex = "#22C55E"
            ))

            // Seed history
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (i in 5 downTo 1) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val prevDate = format.format(calendar.time)
                repository.insertOrUpdateHistory(
                    HistoryRecord(
                        date = prevDate,
                        sleepHours = (7..9).random().toFloat(),
                        moodRating = (3..5).random(),
                        waterMilliliters = (1500..2500).random(),
                        journalEntry = "Successfully survived another day of high-tier training dungeons.",
                        xpGained = (50..300).random(),
                        completedCount = (2..5).random()
                    )
                )
            }
        }
    }

    data class DashboardWidget(val id: String, val name: String, val visible: Boolean, val order: Int)

    private fun getDefaultWidgets(): List<DashboardWidget> {
        return listOf(
            DashboardWidget("STAT_WHEEL", "Hero Stat Wheel", true, 0),
            DashboardWidget("XP_CARD", "Experience & Rank", true, 1),
            DashboardWidget("DAILY_QUESTS", "Today's Active Quests", true, 2),
            DashboardWidget("COMPANION_BOT", "Shadow Field Bot", true, 3),
            DashboardWidget("POMODORO", "Focus Gate (Pomodoro)", true, 4),
            DashboardWidget("HABIT_TRACK", "Discipline Grid (Habits)", true, 5),
            DashboardWidget("SLEEP_MOOD", "Rest & Spirit (Biometrics)", true, 6),
            DashboardWidget("WATER", "Elixir Vessel (Water)", true, 7),
            DashboardWidget("JOURNAL", "Chronicle Ledger (Journal)", true, 8)
        )
    }
}
