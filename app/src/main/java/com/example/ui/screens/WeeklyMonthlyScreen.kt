package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryRecord
import com.example.ui.theme.LocalSoloThemeColors
import com.example.viewmodel.SoloLevelingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeeklyMonthlyScreen(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val records by viewModel.allHistoryRecords.collectAsState()

    val totalXpEarned = records.sumOf { it.xpGained }
    val totalQuestsCleared = records.sumOf { it.completedCount }
    val averageSleep = if (records.isNotEmpty()) records.map { it.sleepHours }.average() else 8.0
    val maxStreak = records.size // calculated dynamically from history length

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CHRONICLE LEDGER STATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Historical Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Summary metric numbers row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total XP Card
                SoloGlassCard(modifier = Modifier.weight(1f)) {
                    Text("TOTAL EARNED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$totalXpEarned XP", fontSize = 16.sp, color = themeColors.primaryAccent, fontWeight = FontWeight.Black)
                }
                // Cleared Quests
                SoloGlassCard(modifier = Modifier.weight(1f)) {
                    Text("TRIALS CLEARED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$totalQuestsCleared", fontSize = 16.sp, color = themeColors.secondaryAccent, fontWeight = FontWeight.Black)
                }
                // Sleep Avg
                SoloGlassCard(modifier = Modifier.weight(1f)) {
                    Text("SLEEP CYCLE AVG", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${String.format("%.1f", averageSleep)}h", fontSize = 16.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Black)
                }
            }
        }

        // GitHub style Heatmap Grid Section
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DISCIPLINE HEAT GRID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Glow indicates completed daily trials over the past 15 weeks.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                HeatmapGrid(records = records, primaryColor = themeColors.primaryAccent)
            }
        }

        // Glowing Bar Charts - Daily XP Trend
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DAILY EXPERIENCE GAIN HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val last7Days = records.takeLast(7)
                if (last7Days.isEmpty()) {
                    Text("Pristine trail. Complete some tasks to populate charts.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    AnalyticsBarChart(
                        data = last7Days.map { it.xpGained.toFloat() },
                        labels = last7Days.map { it.date.split("-").last() },
                        primaryColor = themeColors.primaryAccent,
                        secondaryColor = themeColors.secondaryAccent
                    )
                }
            }
        }

        // Glowing Bar Charts - Sleep Quality Trend
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SLEEP CHAMBER SLEEP QUALITY TRACKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val last7Days = records.takeLast(7)
                if (last7Days.isEmpty()) {
                    Text("Pristine sleep biometric ledger.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    AnalyticsBarChart(
                        data = last7Days.map { it.sleepHours },
                        labels = last7Days.map { it.date.split("-").last() },
                        primaryColor = Color(0xFF38BDF8),
                        secondaryColor = Color(0xFF0284C7)
                    )
                }
            }
        }
    }
}

@Composable
fun HeatmapGrid(records: List<HistoryRecord>, primaryColor: Color) {
    // We render a grid of 15 columns (weeks) by 7 rows (days)
    val totalCells = 15 * 7
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()

    // Generate list of date strings going back from today (totalCells items)
    val dates = remember {
        val list = mutableListOf<String>()
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.DAY_OF_YEAR, -totalCells + 1)
        for (i in 0 until totalCells) {
            list.add(sdf.format(tempCal.time))
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Since we want columns as weeks (15 weeks), we chunk dates by 7 items!
        val weeks = dates.chunked(7)
        items(weeks) { weekDates ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                weekDates.forEach { dateStr ->
                    val rec = records.firstOrNull { it.date == dateStr }
                    val completed = rec?.completedCount ?: 0

                    val cellColor = when {
                        completed == 0 -> Color(0xFF131024)
                        completed <= 2 -> primaryColor.copy(alpha = 0.3f)
                        completed <= 4 -> primaryColor.copy(alpha = 0.65f)
                        else -> primaryColor // 5+ completed items glows brightly!
                    }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(cellColor)
                            .border(
                                width = 0.5.dp,
                                color = if (completed > 0) primaryColor.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsBarChart(
    data: List<Float>,
    labels: List<String>,
    primaryColor: Color,
    secondaryColor: Color
) {
    val maxVal = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalBars = data.size
                val barSpacing = 16.dp.toPx()
                val availableWidth = size.width - (barSpacing * (totalBars - 1))
                val barWidth = availableWidth / totalBars

                for (i in 0 until totalBars) {
                    val ratio = data[i] / maxVal
                    val barHeight = size.height * ratio
                    val left = i * (barWidth + barSpacing)
                    val top = size.height - barHeight

                    // Draw vertical gradient bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw ambient neon light reflections under the peaks
                    if (data[i] > 0) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.25f),
                            radius = (barWidth / 1.5f),
                            center = Offset(left + (barWidth / 2), top)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
