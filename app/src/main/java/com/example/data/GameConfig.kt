package com.example.data

data class TitleInfo(val title: String, val minLevel: Int, val description: String)
data class AchievementInfo(val id: String, val name: String, val description: String, val xpReward: Int, val iconName: String)

object GameConfig {
    // RPG Leveling Formula
    fun getXpNeededForLevel(level: Int): Int {
        // level 1: 100 XP, level 2: 250 XP, level 3: 450 XP, etc.
        return level * 150 + (level - 1) * 100
    }

    // List of hunter titles
    val titlesList = listOf(
        TitleInfo("E-Rank Hunter", 1, "The lowest tier. Survival is your main quest."),
        TitleInfo("D-Rank Hunter", 3, "Getting stronger. Dungeons are becoming easier."),
        TitleInfo("C-Rank Hunter", 5, "A respected fighter. You lead party raids."),
        TitleInfo("B-Rank Hunter", 8, "A formidable powerhouse. Guilds are noticing you."),
        TitleInfo("A-Rank Hunter", 12, "Elite level. A threat to high-level dungeon bosses."),
        TitleInfo("S-Rank Hunter", 18, "Nation-level power. Your presence shifts world politics."),
        TitleInfo("Necromancer", 25, "One who commands the dead. Shadows await your call."),
        TitleInfo("Shadow Monarch", 35, "The ultimate ruler. 'ARISE!'")
    )

    // Pre-configured achievements
    val achievementsList = listOf(
        AchievementInfo("FIRST_QUEST", "First Blood", "Complete your first task or habit", 100, "FitnessCenter"),
        AchievementInfo("LEVEL_5", "Limit Breaker", "Reach level 5", 250, "TrendingUp"),
        AchievementInfo("STREAK_3", "Unyielding Will", "Maintain a 3-day completion streak", 150, "Fire"),
        AchievementInfo("STREAK_7", "Shadow Commander", "Maintain a 7-day completion streak", 500, "Shield"),
        AchievementInfo("HYDRATION", "Elixir of Life", "Track 2000ml of water in a single day", 100, "LocalDrink"),
        AchievementInfo("JOURNAL", "Chronicler", "Write your first journal entry", 100, "Book"),
        AchievementInfo("SLEEP_GOOD", "Rejuvenated", "Track 8+ hours of sleep for 3 days", 200, "Bed"),
        AchievementInfo("MAX_STATS", "Arise!", "Allocate 20 total skill points", 300, "Bolt")
    )
}
