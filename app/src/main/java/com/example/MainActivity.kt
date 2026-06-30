package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.LocalSoloThemeColors
import com.example.ui.theme.SoloLevelingTheme
import com.example.viewmodel.SoloLevelingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SoloLevelingViewModel = viewModel()
            
            SoloLevelingTheme(themeName = viewModel.currentTheme) {
                val themeColors = LocalSoloThemeColors.current
                var activeTabIndex by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    // Ambient gradient background matching magical forest midnight aura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(themeColors.bgStart, themeColors.bgEnd)
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        // Ambient Background Fog matching the Immersive UI Design
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Top-left purple aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(themeColors.primaryAccent.copy(alpha = 0.15f), Color.Transparent),
                                    center = Offset(-10.dp.toPx(), -10.dp.toPx()),
                                    radius = 240.dp.toPx()
                                ),
                                center = Offset(-10.dp.toPx(), -10.dp.toPx()),
                                radius = 240.dp.toPx()
                            )
                            // Center indigo aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF312E81).copy(alpha = 0.12f), Color.Transparent),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = 360.dp.toPx()
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = 360.dp.toPx()
                            )
                            // Bottom-right warm secondary aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(themeColors.secondaryAccent.copy(alpha = 0.08f), Color.Transparent),
                                    center = Offset(size.width + 10.dp.toPx(), size.height + 10.dp.toPx()),
                                    radius = 240.dp.toPx()
                                ),
                                center = Offset(size.width + 10.dp.toPx(), size.height + 10.dp.toPx()),
                                radius = 240.dp.toPx()
                            )
                        }

                        // Display active screen
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (activeTabIndex) {
                                0 -> DashboardScreen(viewModel)
                                1 -> DailyViewScreen(viewModel)
                                2 -> QuestScheduleScreen(viewModel)
                                3 -> StatsCharacterScreen(viewModel)
                                4 -> WeeklyMonthlyScreen(viewModel)
                                5 -> SettingsScreen(viewModel)
                            }
                        }

                        // Neon notifications banner overlay at top of screen (Solo Leveling System Toast style!)
                        viewModel.toastMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .align(Alignment.TopCenter),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F0B21).copy(alpha = 0.95f))
                                        .border(
                                            width = 1.dp,
                                            color = themeColors.secondaryAccent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.clearToast() }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = themeColors.secondaryAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = msg,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Mobile Bottom Floating Glass Dock
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .widthIn(max = 500.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(themeColors.cardBg.copy(alpha = 0.90f))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.15f),
                                                themeColors.border.copy(alpha = 0.10f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val navigationItems = listOf(
                                    Icons.Filled.GridView to "Grid",
                                    Icons.Filled.ListAlt to "Daily",
                                    Icons.Filled.CalendarToday to "Schedule",
                                    Icons.Filled.Person to "Status",
                                    Icons.Filled.TrendingUp to "Logs",
                                    Icons.Filled.Settings to "Engine"
                                )

                                navigationItems.forEachIndexed { index, (icon, label) ->
                                    val isSelected = activeTabIndex == index
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .testTag("nav_tab_$index")
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) themeColors.primaryAccent.copy(alpha = 0.20f)
                                                else Color.Transparent
                                            )
                                            .clickable { activeTabIndex = index }
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) themeColors.primaryAccent else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
