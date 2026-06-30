package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSoloThemeColors

@Composable
fun SoloGlassCard(
    modifier: Modifier = Modifier,
    testTag: String = "glass_card",
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val themeColors = LocalSoloThemeColors.current

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        themeColors.cardBg,
                        themeColors.cardBg.copy(alpha = 0.65f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        themeColors.border.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .then(clickableModifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun SoloXpBar(
    currentXp: Int,
    requiredXp: Int,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalSoloThemeColors.current
    val progress = (currentXp.toFloat() / requiredXp.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "xp_bar"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXP: $currentXp / $requiredXp",
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.primaryAccent,
                fontSize = 11.sp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.primaryAccent,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E2C))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                themeColors.primaryAccent,
                                themeColors.secondaryAccent
                            )
                        )
                    )
                    .drawBehind {
                        // Drawing a subtle neon light sweep glow
                        drawRoundRect(
                            color = themeColors.primaryAccent.copy(alpha = 0.3f),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
            )
        }
    }
}

@Composable
fun CategoryBadge(category: String, modifier: Modifier = Modifier) {
    val themeColors = LocalSoloThemeColors.current
    val (color, icon) = when (category.uppercase()) {
        "STRENGTH" -> Color(0xFFFF8C5A) to Icons.Filled.FitnessCenter
        "INTELLECT", "KNOWLEDGE" -> Color(0xFFA56CFF) to Icons.Filled.Code
        "AGILITY" -> Color(0xFF38BDF8) to Icons.Filled.Bolt
        "VITALITY", "LIFE" -> Color(0xFFEF4444) to Icons.Filled.Favorite
        "MIND", "SENSE" -> Color(0xFF22C55E) to Icons.Filled.Psychology
        else -> themeColors.primaryAccent to Icons.Filled.Star
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category.uppercase(),
            fontSize = 9.sp,
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun DifficultyBadge(difficulty: String, modifier: Modifier = Modifier) {
    val (color, label) = when (difficulty.uppercase()) {
        "EASY" -> Color(0xFF22C55E) to "E-RANK"
        "HARD" -> Color(0xFFEF4444) to "S-RANK"
        else -> Color(0xFF38BDF8) to "C-RANK" // MEDIUM
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun rememberVectorIcon(iconName: String): ImageVector {
    return when(iconName.lowercase()) {
        "fitnesscenter", "gym", "strength" -> Icons.Filled.FitnessCenter
        "book", "study", "journal", "read" -> Icons.Filled.Book
        "code", "programming", "intellect" -> Icons.Filled.Code
        "psychology", "meditation", "mind" -> Icons.Filled.Psychology
        "localdrink", "water" -> Icons.Filled.LocalDrink
        "bed", "sleep" -> Icons.Filled.Bed
        "fire", "streak" -> Icons.Filled.Whatshot
        "shield", "defense" -> Icons.Filled.Shield
        "bolt", "energy", "agility" -> Icons.Filled.Bolt
        "star" -> Icons.Filled.Star
        "settings" -> Icons.Filled.Settings
        "analytics", "trendingup" -> Icons.Filled.TrendingUp
        "calendar", "schedule" -> Icons.Filled.CalendarMonth
        "add" -> Icons.Filled.Add
        "person", "profile" -> Icons.Filled.Person
        "delete" -> Icons.Filled.Delete
        "check" -> Icons.Filled.CheckCircle
        "arrowup" -> Icons.Filled.ArrowUpward
        "arrowdown" -> Icons.Filled.ArrowDownward
        "lock" -> Icons.Filled.Lock
        "trophy", "achieve" -> Icons.Filled.EmojiEvents
        else -> Icons.Filled.Star
    }
}
