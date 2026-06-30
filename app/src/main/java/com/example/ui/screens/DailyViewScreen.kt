package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnifiedItem
import com.example.ui.theme.LocalSoloThemeColors
import com.example.viewmodel.SoloLevelingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyViewScreen(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val focusManager = LocalFocusManager.current

    val activeItems by viewModel.allActiveItems.collectAsState()
    val records by viewModel.allHistoryRecords.collectAsState()
    
    val selectedDate = viewModel.selectedDateStr
    val todayRecord = records.firstOrNull { it.date == selectedDate } ?: com.example.data.HistoryRecord(date = selectedDate)

    // Filter items based on selected date
    val dayItems = activeItems.filter { item ->
        if (item.isHabit) {
            if (item.recurrenceDays == null) true
            else {
                // simple recurrence filter: check if selectedDate matches day abbreviation
                val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val sdfOutput = SimpleDateFormat("EEE", Locale.US)
                try {
                    val dateObj = sdfInput.parse(selectedDate)
                    val dayOfWeek = sdfOutput.format(dateObj!!)
                    item.recurrenceDays.split(",").map { it.trim().lowercase() }.contains(dayOfWeek.lowercase())
                } catch (e: Exception) {
                    true
                }
            }
        } else {
            item.date == selectedDate
        }
    }

    // Form expanded state
    var summonFormExpanded by remember { mutableStateOf(false) }

    // Form inputs state
    var summonTitle by remember { mutableStateOf("") }
    var summonDesc by remember { mutableStateOf("") }
    var summonCategory by remember { mutableStateOf("STRENGTH") }
    var summonIsHabit by remember { mutableStateOf(false) }
    var summonIsTask by remember { mutableStateOf(true) }
    var summonIsCalendarEvent by remember { mutableStateOf(false) }
    var summonDifficulty by remember { mutableStateOf("MEDIUM") }
    var summonPriority by remember { mutableStateOf("MEDIUM") }
    var summonXp by remember { mutableStateOf("100") }
    var summonTime by remember { mutableStateOf("") }
    var summonDuration by remember { mutableStateOf("0") }
    var summonIsNegative by remember { mutableStateOf(false) }
    val selectedRecurrenceDays = remember { mutableStateListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Node
        item {
            DateSelectorRow(viewModel)
        }

        // Active Trial Bulletin Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE BULLETIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primaryAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Daily trial lists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                
                Button(
                    onClick = { summonFormExpanded = !summonFormExpanded },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (summonFormExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (summonFormExpanded) "CLOSE" else "SUMMON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quest Summon Form
        item {
            AnimatedVisibility(
                visible = summonFormExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SUMMON NEW QUEST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.secondaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = summonTitle,
                        onValueChange = { summonTitle = it },
                        label = { Text("Quest Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = summonDesc,
                        onValueChange = { summonDesc = it },
                        label = { Text("Quest Description / Objectives") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Type switches
                    Text("Quest Type", fontSize = 11.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TASK" to true, "HABIT" to false).forEach { (label, isTask) ->
                            val isSelected = if (isTask) summonIsTask else summonIsHabit
                            Button(
                                onClick = {
                                    summonIsTask = isTask
                                    summonIsHabit = !isTask
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) themeColors.primaryAccent else Color(0x11FFFFFF)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Recurrence Selector for habits
                    if (summonIsHabit) {
                        Text("Discipline Cycle (Recurrence)", fontSize = 11.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            daysList.forEach { day ->
                                val active = selectedRecurrenceDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (active) themeColors.primaryAccent else Color(0x1F221E3B))
                                        .border(
                                            width = 1.dp,
                                            color = if (active) themeColors.primaryAccent else Color.Gray.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            if (active) selectedRecurrenceDays.remove(day)
                                            else selectedRecurrenceDays.add(day)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.take(1),
                                        fontSize = 11.sp,
                                        color = if (active) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Negative habit switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Penalty Quest (Negative Habit)", fontSize = 12.sp, color = Color.White)
                            Switch(
                                checked = summonIsNegative,
                                onCheckedChange = { summonIsNegative = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Red,
                                    checkedTrackColor = Color.Red.copy(alpha = 0.5f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Stat category
                    Text("Stat Core Category", fontSize = 11.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf("STRENGTH", "INTELLECT", "AGILITY", "MIND", "LIFE")
                        categories.forEach { cat ->
                            val isSelected = summonCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) themeColors.primaryAccent.copy(alpha = 0.25f) else Color(0x11FFFFFF))
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isSelected) themeColors.primaryAccent else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { summonCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat.take(3), fontSize = 9.sp, color = if (isSelected) Color.White else Color.LightGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Difficulty
                    Text("Trial Difficulty", fontSize = 11.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            val isSelected = summonDifficulty == diff
                            Button(
                                onClick = {
                                    summonDifficulty = diff
                                    summonXp = when(diff) {
                                        "EASY" -> "50"
                                        "HARD" -> "200"
                                        else -> "100" // MEDIUM
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) themeColors.primaryAccent else Color(0x11FFFFFF)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(diff, fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Editable XP Field
                    OutlinedTextField(
                        value = summonXp,
                        onValueChange = { summonXp = it },
                        label = { Text("XP Reward Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (summonTitle.trim().isNotEmpty()) {
                                val item = UnifiedItem(
                                    title = summonTitle.trim(),
                                    description = summonDesc.trim(),
                                    category = summonCategory,
                                    isHabit = summonIsHabit,
                                    isTask = summonIsTask,
                                    isCalendarEvent = summonIsCalendarEvent || summonTime.isNotEmpty(),
                                    date = selectedDate,
                                    time = summonTime.ifEmpty { null },
                                    durationMinutes = summonDuration.toIntOrNull() ?: 0,
                                    difficulty = summonDifficulty,
                                    priority = summonPriority,
                                    xpReward = summonXp.toIntOrNull() ?: 50,
                                    recurrenceDays = if (summonIsHabit) selectedRecurrenceDays.joinToString(",") else null,
                                    isNegative = summonIsNegative,
                                    iconName = when(summonCategory) {
                                        "STRENGTH" -> "FitnessCenter"
                                        "INTELLECT" -> "Code"
                                        "AGILITY" -> "Bolt"
                                        "LIFE" -> "Favorite"
                                        "MIND" -> "Psychology"
                                        else -> "Star"
                                    },
                                    colorHex = when(summonCategory) {
                                        "STRENGTH" -> "#FF8C5A"
                                        "INTELLECT" -> "#A56CFF"
                                        "AGILITY" -> "#38BDF8"
                                        "LIFE" -> "#EF4444"
                                        "MIND" -> "#22C55E"
                                        else -> "#A56CFF"
                                    }
                                )
                                viewModel.createOrUpdateItem(item)
                                // Reset fields
                                summonTitle = ""
                                summonDesc = ""
                                summonFormExpanded = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondaryAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SUMMON QUEST trial", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Daily Quest Items list
        if (dayItems.isEmpty()) {
            item {
                SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Today's slate is pristine, Monarch. No daily trials are scheduled. Trigger SUMMON to conjure a task or habit.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(dayItems) { item ->
                val isCompleted = if (item.isHabit) item.isCompletedOn(selectedDate) else item.isCompleted
                SoloGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.toggleItemCompletion(item) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleItemCompletion(item) }) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = "Complete",
                                    tint = if (isCompleted) themeColors.primaryAccent else Color.LightGray
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) Color.Gray else Color.White
                                )
                                if (item.description.isNotEmpty()) {
                                    Text(
                                        text = item.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryBadge(category = item.category)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${item.xpReward} XP",
                                        fontSize = 10.sp,
                                        color = themeColors.secondaryAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.isNegative) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Red.copy(alpha = 0.15f))
                                                .border(0.5.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("PENALTY", fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DifficultyBadge(difficulty = item.difficulty)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.deleteItem(item) }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = Color.Gray.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Biometrics Logs Header
        item {
            Text(
                text = "BIOMETRIC LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.primaryAccent,
                letterSpacing = 1.5.sp
            )
        }

        // Water container widget
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                WaterWidget(viewModel)
            }
        }

        // Sleep & Mood Slider
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                BiometricsWidget(viewModel)
            }
        }

        // Journal Writer box
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Chronicle Diary Ledger", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = todayRecord.journalEntry,
                    onValueChange = { viewModel.updateJournalEntry(selectedDate, it) },
                    placeholder = { Text("Log your training milestones, battle notes, and general progress thoughts here...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.primaryAccent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )
            }
        }
    }
}

@Composable
fun DateSelectorRow(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val currentSelected = viewModel.selectedDateStr

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()

    // Render a 5-day sliding anchor (yesterday, today, next 3 days)
    val daysList = remember {
        val list = mutableListOf<String>()
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.DAY_OF_YEAR, -2) // Start 2 days ago
        for (i in 0 until 5) {
            list.add(sdf.format(tempCal.time))
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        daysList.forEach { dateStr ->
            val dateObj = sdf.parse(dateStr)!!
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dateObj)
            val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(dateObj)
            val isSelected = dateStr == currentSelected

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) themeColors.primaryAccent else Color(0x11FFFFFF))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) themeColors.secondaryAccent else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.selectDate(dateStr) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayName.uppercase(),
                    fontSize = 9.sp,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dayNum,
                    fontSize = 15.sp,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
