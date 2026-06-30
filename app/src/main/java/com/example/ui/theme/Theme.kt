package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal to easily read our custom theme colors anywhere in the app
val LocalSoloThemeColors = staticCompositionLocalOf {
    SoloThemeColors.getThemeColors("Solo Leveling")
}

@Composable
fun SoloLevelingTheme(
    themeName: String = "Solo Leveling",
    content: @Composable () -> Unit
) {
    val soloColors = SoloThemeColors.getThemeColors(themeName)

    // Bridge with standard MaterialTheme color schemes
    val m3ColorScheme = darkColorScheme(
        primary = soloColors.primaryAccent,
        secondary = soloColors.secondaryAccent,
        background = soloColors.bgStart,
        surface = soloColors.cardBg,
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )

    CompositionLocalProvider(
        LocalSoloThemeColors provides soloColors
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
