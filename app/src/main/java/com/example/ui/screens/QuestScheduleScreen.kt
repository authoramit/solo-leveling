package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnifiedItem
import com.example.ui.theme.LocalSoloThemeColors
import com.example.viewmodel.SoloLevelingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScheduleScreen(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val activeItems by viewModel.allActiveItems.collectAsState()
    val selectedDate = viewModel.selectedDateStr

    var showTimeBlockDialog by remember { mutableStateOf(false) }
    var blockTitle by remember { mutableStateOf("") }
    var blockTime by remember { mutableStateOf("09:00") }
    var blockDuration by remember { mutableStateOf("60") }
    var blockCategory by remember { mutableStateOf("INTELLECT") }

    val scheduledItems = activeItems.filter { !it.isArchived && (it.isCalendarEvent || it.time != null) && it.date == selectedDate }
        .sortedBy { it.time ?: "23:59" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date anchor bar
        item {
            DateSelectorRow(viewModel)
        }

        // Time blocking header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DIMENSIONAL CHRONOLOGY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primaryAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Time Block Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = { showTimeBlockDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BLOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Timeline visualization of scheduled items
        if (scheduledItems.isEmpty()) {
            item {
                SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No focus blocks or scheduled trials for today. Press BLOCK to allocate high-focus deep work slots.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(scheduledItems) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Time block indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(60.dp).padding(top = 4.dp)
                    ) {
                        Text(
                            text = item.time ?: "ALL DAY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primaryAccent
                        )
                        if (item.durationMinutes > 0) {
                            Text(
                                text = "${item.durationMinutes} Min",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Timeline node line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(80.dp)
                            .background(themeColors.border.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(themeColors.secondaryAccent)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Glass schedule card representation
                    SoloGlassCard(
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.toggleItemCompletion(item) }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isCompleted) Color.Gray else Color.White
                                )
                                if (item.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = themeColors.primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (item.description.isNotEmpty()) {
                                Text(
                                    text = item.description,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryBadge(category = item.category)
                                Text(
                                    text = "+${item.xpReward} XP",
                                    fontSize = 10.sp,
                                    color = themeColors.secondaryAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Calendar Grid Header
        item {
            Text(
                text = "MONTHLY GATEWAY AGENDA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.primaryAccent,
                letterSpacing = 1.5.sp
            )
        }

        // Calendar Grid Representation (Drawn dynamically!)
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val sdfMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                
                Text(
                    text = sdfMonthName.format(calendar.time).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Days of week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    days.forEach { d ->
                        Text(
                            text = d,
                            fontSize = 11.sp,
                            color = themeColors.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Days calculation (renders exactly 35 grid items for safety compilation)
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Align Monday start
                
                val todayCalendar = Calendar.getInstance()
                val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Backtrack to fill start days
                calendar.add(Calendar.DAY_OF_MONTH, -startDayOfWeek)

                for (week in 0 until 5) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (day in 0 until 7) {
                            val formattedDate = sdfDate.format(calendar.time)
                            val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
                            val isCurrentMonth = calendar.get(Calendar.MONTH) == currentMonth
                            val isSelected = formattedDate == selectedDate
                            val hasTasks = activeItems.any { it.date == formattedDate && !it.isArchived }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) themeColors.primaryAccent
                                        else if (hasTasks) themeColors.primaryAccent.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) themeColors.secondaryAccent else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectDate(formattedDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else if (isCurrentMonth) Color.White.copy(alpha = 0.8f) else Color.Gray
                                    )
                                    if (hasTasks && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(themeColors.secondaryAccent)
                                        )
                                    }
                                }
                            }
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                }
            }
        }
    }

    // Time block builder dialog
    if (showTimeBlockDialog) {
        AlertDialog(
            onDismissRequest = { showTimeBlockDialog = false },
            title = { Text("Summon Time Block Node", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = blockTitle,
                        onValueChange = { blockTitle = it },
                        label = { Text("Session Name (e.g., Deep Study)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.primaryAccent
                        )
                    )
                    OutlinedTextField(
                        value = blockTime,
                        onValueChange = { blockTime = it },
                        label = { Text("Start Time (HH:mm)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.primaryAccent
                        )
                    )
                    OutlinedTextField(
                        value = blockDuration,
                        onValueChange = { blockDuration = it },
                        label = { Text("Duration (Minutes)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.primaryAccent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (blockTitle.trim().isNotEmpty()) {
                            val item = UnifiedItem(
                                title = blockTitle.trim(),
                                category = blockCategory,
                                isCalendarEvent = true,
                                isTask = true,
                                date = selectedDate,
                                time = blockTime,
                                durationMinutes = blockDuration.toIntOrNull() ?: 60,
                                xpReward = 75,
                                iconName = "Bolt"
                            )
                            viewModel.createOrUpdateItem(item)
                            showTimeBlockDialog = false
                            blockTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondaryAccent)
                ) {
                    Text("Seal Block", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeBlockDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF131024),
            shape = RoundedCornerShape(18.dp)
        )
    }
}
