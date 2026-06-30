package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameConfig
import com.example.ui.theme.LocalSoloThemeColors
import com.example.viewmodel.SoloLevelingViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatsCharacterScreen(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val profile by viewModel.userProfile.collectAsState()
    val unlocked = viewModel.unlockedAchievements

    var activeTab by remember { mutableStateOf("STATS") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab header: Stats vs Achievements
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131024))
                .padding(4.dp)
        ) {
            listOf("STATS" to "CHARACTER WINDOW", "ACHIEVEMENTS" to "CHRONICLE TROPHIES").forEach { (id, label) ->
                val active = activeTab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) themeColors.primaryAccent else Color.Transparent)
                        .clickable { activeTab = id }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == "STATS") {
            // Stats window scrolling content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Character Info Node
                SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = profile.activeTitle.uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColors.secondaryAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "MONARCH LEVEL ${profile.level}",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Unspent Stat Points: ${profile.skillPoints}",
                                fontSize = 12.sp,
                                color = if (profile.skillPoints > 0) Color.Green else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Level Badge Sphere
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(themeColors.primaryAccent.copy(alpha = 0.15f))
                                .border(2.dp, themeColors.primaryAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // Interactive Pentagon Radar Canvas Chart
                SoloGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text(
                        text = "ATTRIBUTE MATRIX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        StatsRadarChart(
                            strength = profile.strength,
                            intellect = profile.intelligence,
                            agility = profile.agility,
                            sense = profile.sense,
                            vitality = profile.vitality,
                            primaryColor = themeColors.primaryAccent,
                            accentColor = themeColors.secondaryAccent
                        )
                    }
                }

                // Skill distribution list
                SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "UPGRADE CORE ATTRIBUTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val attributesList = listOf(
                        Triple("STRENGTH", profile.strength, "Amplifies daily exercise/strength trial multipliers"),
                        Triple("INTELLIGENCE", profile.intelligence, "Sustains focus capacity, boosts Pomodoro outputs"),
                        Triple("AGILITY", profile.agility, "Improves schedule flexibility & trial clearances"),
                        Triple("SENSE", profile.sense, "Heightens daily biometric tracking XP multipliers"),
                        Triple("VITALITY", profile.vitality, "Shields streak durability from negative trials")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        attributesList.forEach { (name, valNum, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x0EFFFFFF))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$name: $valNum",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                if (profile.skillPoints > 0) {
                                    Button(
                                        onClick = { viewModel.allocateSkillPoint(name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape
                                    ) {
                                        Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // Achievements Window List
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(GameConfig.achievementsList) { ach ->
                    val isUnlocked = unlocked.contains(ach.id)
                    SoloGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isUnlocked) themeColors.secondaryAccent.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isUnlocked) themeColors.secondaryAccent.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.2f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isUnlocked) themeColors.secondaryAccent else Color.Gray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = rememberVectorIcon(ach.iconName),
                                    contentDescription = null,
                                    tint = if (isUnlocked) themeColors.secondaryAccent else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = ach.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color.White else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ach.description,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isUnlocked) "UNLOCKED (+${ach.xpReward} XP)" else "LOCKED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) themeColors.primaryAccent else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsRadarChart(
    strength: Int,
    intellect: Int,
    agility: Int,
    sense: Int,
    vitality: Int,
    primaryColor: Color,
    accentColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2.3f

        val stats = listOf(strength, intellect, sense, vitality, agility)
        val maxStatValue = stats.maxOrNull()?.coerceAtLeast(10)?.toFloat() ?: 10f

        // 1. Draw web pentagon rings (3 tiers)
        for (tier in 1..3) {
            val r = maxRadius * (tier.toFloat() / 3f)
            val path = Path()
            for (i in 0 until 5) {
                val angle = (i * 72f - 90f) * (Math.PI / 180f)
                val x = centerX + r * cos(angle).toFloat()
                val y = centerY + r * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.12f * tier),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw outer boundary and spoke lines
        for (i in 0 until 5) {
            val angle = (i * 72f - 90f) * (Math.PI / 180f)
            val x = centerX + maxRadius * cos(angle).toFloat()
            val y = centerY + maxRadius * sin(angle).toFloat()
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. Plot the user's active attributes polygon
        val polyPath = Path()
        for (i in 0 until 5) {
            val statVal = stats[i].toFloat()
            val r = maxRadius * (statVal / maxStatValue)
            val angle = (i * 72f - 90f) * (Math.PI / 180f)
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()
            if (i == 0) polyPath.moveTo(x, y) else polyPath.lineTo(x, y)
        }
        polyPath.close()

        drawPath(
            path = polyPath,
            color = accentColor.copy(alpha = 0.25f)
        )
        drawPath(
            path = polyPath,
            color = accentColor,
            style = Stroke(width = 2.dp.toPx())
        )

        // 4. Draw node dots
        for (i in 0 until 5) {
            val statVal = stats[i].toFloat()
            val r = maxRadius * (statVal / maxStatValue)
            val angle = (i * 72f - 90f) * (Math.PI / 180f)
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()
            drawCircle(
                color = accentColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

// scroll states natively imported
