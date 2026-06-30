package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Theme Color Palettes
object SoloThemePalettes {
    // 1. Solo Leveling (Default)
    val SoloLevelingBgStart = Color(0xFF0E0A1A)
    val SoloLevelingBgEnd = Color(0xFF15102A)
    val SoloLevelingPrimary = Color(0xFFA56CFF)
    val SoloLevelingSecondary = Color(0xFFFF8C5A)
    val SoloLevelingSurface = Color(0x1F1F1735)
    val SoloLevelingBorder = Color(0x2F7F50FF)

    // 2. Crimson Monarch
    val CrimsonBgStart = Color(0xFF0B0303)
    val CrimsonBgEnd = Color(0xFF180606)
    val CrimsonPrimary = Color(0xFFFF3333)
    val CrimsonSecondary = Color(0xFFFFCC00)
    val CrimsonSurface = Color(0x1FFF0D0D)
    val CrimsonBorder = Color(0x3FFF2F2F)

    // 3. Forest
    val ForestBgStart = Color(0xFF040A06)
    val ForestBgEnd = Color(0xFF0C1D13)
    val ForestPrimary = Color(0xFF22C55E)
    val ForestSecondary = Color(0xFFEAB308)
    val ForestSurface = Color(0x1F0F3E22)
    val ForestBorder = Color(0x2F22C55E)

    // 4. Cyberpunk
    val CyberBgStart = Color(0xFF05050A)
    val CyberBgEnd = Color(0xFF0D0214)
    val CyberPrimary = Color(0xFF00F0FF)
    val CyberSecondary = Color(0xFFFF007F)
    val CyberSurface = Color(0x1F140F35)
    val CyberBorder = Color(0x2F00F0FF)

    // 5. Midnight
    val MidnightBgStart = Color(0xFF020813)
    val MidnightBgEnd = Color(0xFF08142C)
    val MidnightPrimary = Color(0xFF3B82F6)
    val MidnightSecondary = Color(0xFF10B981)
    val MidnightSurface = Color(0x1F1E3A8A)
    val MidnightBorder = Color(0x2F3B82F6)

    // 6. Pure Dark
    val PureDarkBgStart = Color(0xFF000000)
    val PureDarkBgEnd = Color(0xFF080808)
    val PureDarkPrimary = Color(0xFF94A3B8)
    val PureDarkSecondary = Color(0xFF8B5CF6)
    val PureDarkSurface = Color(0x1F1E293B)
    val PureDarkBorder = Color(0x2F475569)

    // 7. Frost
    val FrostBgStart = Color(0xFF08131A)
    val FrostBgEnd = Color(0xFF112530)
    val FrostPrimary = Color(0xFF38BDF8)
    val FrostSecondary = Color(0xFFFFFFFF)
    val FrostSurface = Color(0x1F0C4A6E)
    val FrostBorder = Color(0x2F38BDF8)
}

data class SoloThemeColors(
    val bgStart: Color,
    val bgEnd: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val surface: Color,
    val border: Color,
    val cardBg: Color = Color(0x2A15102A),
    val glowStrength: Float = 1.0f
) {
    companion object {
        fun getThemeColors(themeName: String): SoloThemeColors {
            return when (themeName) {
                "Crimson Monarch" -> SoloThemeColors(
                    bgStart = SoloThemePalettes.CrimsonBgStart,
                    bgEnd = SoloThemePalettes.CrimsonBgEnd,
                    primaryAccent = SoloThemePalettes.CrimsonPrimary,
                    secondaryAccent = SoloThemePalettes.CrimsonSecondary,
                    surface = SoloThemePalettes.CrimsonSurface,
                    border = SoloThemePalettes.CrimsonBorder,
                    cardBg = Color(0x2A180606)
                )
                "Forest" -> SoloThemeColors(
                    bgStart = SoloThemePalettes.ForestBgStart,
                    bgEnd = SoloThemePalettes.ForestBgEnd,
                    primaryAccent = SoloThemePalettes.ForestPrimary,
                    secondaryAccent = SoloThemePalettes.ForestSecondary,
                    surface = SoloThemePalettes.ForestSurface,
                    border = SoloThemePalettes.ForestBorder,
                    cardBg = Color(0x2A0C1D13)
                )
                "Cyberpunk" -> SoloThemeColors(
                    bgStart = SoloThemePalettes.CyberBgStart,
                    bgEnd = SoloThemePalettes.CyberBgEnd,
                    primaryAccent = SoloThemePalettes.CyberPrimary,
                    secondaryAccent = SoloThemePalettes.CyberSecondary,
                    surface = SoloThemePalettes.CyberSurface,
                    border = SoloThemePalettes.CyberBorder,
                    cardBg = Color(0x2A0D0214)
                )
                "Midnight" -> SoloThemeColors(
                    bgStart = SoloThemePalettes.MidnightBgStart,
                    bgEnd = SoloThemePalettes.MidnightBgEnd,
                    primaryAccent = SoloThemePalettes.MidnightPrimary,
                    secondaryAccent = SoloThemePalettes.MidnightSecondary,
                    surface = SoloThemePalettes.MidnightSurface,
                    border = SoloThemePalettes.MidnightBorder,
                    cardBg = Color(0x2A08142C)
                )
                "Pure Dark" -> SoloThemeColors(
                    bgStart = SoloThemePalettes.PureDarkBgStart,
                    bgEnd = SoloThemePalettes.PureDarkBgEnd,
                    primaryAccent = SoloThemePalettes.PureDarkPrimary,
                    secondaryAccent = SoloThemePalettes.PureDarkSecondary,
                    surface = SoloThemePalettes.PureDarkSurface,
                    border = SoloThemePalettes.PureDarkBorder,
                    cardBg = Color(0x2A080808)
                )
                "Frost" -> SoloThemeColors(
                    bgStart = SoloThemePalettes.FrostBgStart,
                    bgEnd = SoloThemePalettes.FrostBgEnd,
                    primaryAccent = SoloThemePalettes.FrostPrimary,
                    secondaryAccent = SoloThemePalettes.FrostSecondary,
                    surface = SoloThemePalettes.FrostSurface,
                    border = SoloThemePalettes.FrostBorder,
                    cardBg = Color(0x2A112530)
                )
                else -> SoloThemeColors( // "Solo Leveling"
                    bgStart = SoloThemePalettes.SoloLevelingBgStart,
                    bgEnd = SoloThemePalettes.SoloLevelingBgEnd,
                    primaryAccent = SoloThemePalettes.SoloLevelingPrimary,
                    secondaryAccent = SoloThemePalettes.SoloLevelingSecondary,
                    surface = SoloThemePalettes.SoloLevelingSurface,
                    border = SoloThemePalettes.SoloLevelingBorder,
                    cardBg = Color(0x2A15102A)
                )
            }
        }
    }
}
