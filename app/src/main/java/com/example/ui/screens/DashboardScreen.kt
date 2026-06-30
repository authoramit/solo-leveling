package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.BorderStroke
import android.graphics.Color as AndroidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnifiedItem
import com.example.ui.theme.LocalSoloThemeColors
import com.example.viewmodel.SoloLevelingViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(viewModel: SoloLevelingViewModel) {
    val widgets = viewModel.dashboardWidgets
    val profile by viewModel.userProfile.collectAsState()
    val activeItems by viewModel.allActiveItems.collectAsState()
    val todayDate = viewModel.getTodayDateString()

    // Filter items due for selected date to show in Today's Quests
    val todayItems = activeItems.filter { item ->
        if (item.isHabit) {
            if (item.recurrenceDays == null) true
            else {
                // simple recurrence filter: check if today matches day abbreviation
                val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
                val dayOfWeek = sdf.format(java.util.Date())
                item.recurrenceDays.split(",").map { it.trim().lowercase() }.contains(dayOfWeek.lowercase())
            }
        } else {
            item.date == todayDate
        }
    }

    var showLayoutCustomizer by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar block with custom gradient border
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(LocalSoloThemeColors.current.primaryAccent, LocalSoloThemeColors.current.bgEnd)
                                    )
                                )
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(LocalSoloThemeColors.current.bgStart),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = if (profile.activeTitle.length >= 2) {
                                    profile.activeTitle.substring(0, 2).uppercase()
                                } else {
                                    "SJ"
                                }
                                Text(
                                    text = initials,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalSoloThemeColors.current.primaryAccent
                                )
                            }
                        }

                        Column {
                            Text(
                                text = profile.activeTitle.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalSoloThemeColors.current.primaryAccent.copy(alpha = 0.7f),
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "SUNG JIN-WOO",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status/Rank Badge
                        val rankLabel = if (profile.activeTitle.contains("S-Rank", ignoreCase = true)) "S-Rank" else if (profile.activeTitle.contains("E-Rank", ignoreCase = true)) "E-Rank" else "Monarch"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LocalSoloThemeColors.current.secondaryAccent)
                                )
                                Text(
                                    text = rankLabel.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showLayoutCustomizer = !showLayoutCustomizer },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LocalSoloThemeColors.current.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = "Customize Layout",
                                tint = LocalSoloThemeColors.current.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Dynamically render widgets based on visibility and ordered position
            val visibleWidgets = widgets.filter { it.visible }.sortedBy { it.order }
            items(visibleWidgets, key = { it.id }) { widget ->
                Box(modifier = Modifier.animateItem()) {
                    WidgetWrapper(
                        widgetId = widget.id,
                        name = widget.name,
                        onMoveUp = { viewModel.moveWidgetOrder(widget.id, true) },
                        onMoveDown = { viewModel.moveWidgetOrder(widget.id, false) },
                        onHide = { viewModel.toggleWidgetVisibility(widget.id) }
                    ) {
                        when (widget.id) {
                            "STAT_WHEEL" -> StatWheelWidget(profile.level, profile.xp, todayItems)
                            "XP_CARD" -> XpCardWidget(profile.level, profile.xp, profile.activeTitle, profile.currentStreak)
                            "DAILY_QUESTS" -> DailyQuestsWidget(todayItems, viewModel)
                            "COMPANION_BOT" -> FieldBotWidget(viewModel.botMessage)
                            "POMODORO" -> PomodoroWidget(viewModel)
                            "HABIT_TRACK" -> HabitsWidget(todayItems, viewModel)
                            "SLEEP_MOOD" -> BiometricsWidget(viewModel)
                            "WATER" -> WaterWidget(viewModel)
                            "JOURNAL" -> JournalPreviewWidget(viewModel)
                        }
                    }
                }
            }
        }

        // Layout Customizer Drawer Overlay
        if (showLayoutCustomizer) {
            AlertDialog(
                onDismissRequest = { showLayoutCustomizer = false },
                title = { Text("Customize OS Widgets", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(widgets) { w ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E2C))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(w.name, color = Color.White, fontSize = 14.sp)
                                Switch(
                                    checked = w.visible,
                                    onCheckedChange = { viewModel.toggleWidgetVisibility(w.id) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LocalSoloThemeColors.current.primaryAccent,
                                        checkedTrackColor = LocalSoloThemeColors.current.primaryAccent.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLayoutCustomizer = false }) {
                        Text("Apply", color = LocalSoloThemeColors.current.primaryAccent)
                    }
                },
                containerColor = Color(0xFF131024),
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

@Composable
fun WidgetWrapper(
    widgetId: String,
    name: String,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onHide: () -> Unit,
    content: @Composable () -> Unit
) {
    var expandedControls by remember { mutableStateOf(false) }

    SoloGlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "widget_$widgetId"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LocalSoloThemeColors.current.primaryAccent,
                letterSpacing = 1.5.sp
            )
            IconButton(
                onClick = { expandedControls = !expandedControls },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Widget options",
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = expandedControls,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ArrowUpward, "Move Up", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ArrowDownward, "Move Down", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onHide, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.VisibilityOff, "Hide", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
fun StatWheelWidget(level: Int, xp: Int, todayItems: List<UnifiedItem>) {
    val themeColors = LocalSoloThemeColors.current

    // Rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "stat_wheel")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val completedCount = todayItems.count { if (it.isHabit) it.isCompletedOn(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())) else it.isCompleted }
    val totalCount = todayItems.size.coerceAtLeast(1)
    val percentage = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val xpRequired = com.example.data.GameConfig.getXpNeededForLevel(level)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radial Canvas Ring
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(angle)
            ) {
                val strokeWidth = 8.dp.toPx()
                
                // Ring 1: Outermost thin solid border
                drawCircle(
                    color = themeColors.primaryAccent.copy(alpha = 0.15f),
                    radius = size.minDimension / 2 - 2.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
                
                // Ring 2: Middle thin dashed white border (corresponds to border-dashed border-white/5)
                val dashEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.minDimension / 2 - 10.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashEffect)
                )
                
                // Ring 3: Innermost thin solid border
                drawCircle(
                    color = themeColors.primaryAccent.copy(alpha = 0.35f),
                    radius = size.minDimension / 2 - 20.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Background track ring for the main progress
                drawCircle(
                    color = Color(0xFF151221).copy(alpha = 0.6f),
                    radius = size.minDimension / 2 - 20.dp.toPx(),
                    style = Stroke(width = strokeWidth)
                )

                // Actual Progress Arc on Ring 3 radius
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(themeColors.primaryAccent, themeColors.secondaryAccent, themeColors.primaryAccent)
                    ),
                    startAngle = -90f,
                    sweepAngle = percentage * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Draw orbiting crystal nodes (completed quests)
                val activeCount = todayItems.size
                if (activeCount > 0) {
                    val radius = size.minDimension / 2 - 20.dp.toPx()
                    for (i in 0 until activeCount) {
                        val theta = (i * (360f / activeCount) - 90f) * (Math.PI / 180f)
                        val isItemCompleted = if (todayItems[i].isHabit) todayItems[i].isCompletedOn(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())) else todayItems[i].isCompleted
                        val x = (size.width / 2) + radius * cos(theta).toFloat()
                        val y = (size.height / 2) + radius * sin(theta).toFloat()
                        drawCircle(
                            color = if (isItemCompleted) Color(AndroidColor.parseColor(todayItems[i].colorHex)) else Color.Gray.copy(alpha = 0.6f),
                            radius = if (isItemCompleted) 6.dp.toPx() else 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // Level indicator in the center
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LEVEL",
                    fontSize = 10.sp,
                    color = themeColors.primaryAccent.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "$level",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(themeColors.primaryAccent.copy(alpha = 0.20f))
                        .border(0.5.dp, themeColors.primaryAccent.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$xp / $xpRequired XP",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primaryAccent
                    )
                }
            }
        }

        // Stats summary column
        Column(
            modifier = Modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "TRIALS STATUS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                letterSpacing = 1.sp
            )
            Text(
                text = "Grid completion is at ${(percentage * 100).toInt()}% today.",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Surrounding nodes reflect active quests and your daily discipline loop.",
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 14.sp,
                modifier = Modifier.width(150.dp)
            )
        }
    }
}

@Composable
fun XpCardWidget(level: Int, xp: Int, title: String, streak: Int) {
    val themeColors = LocalSoloThemeColors.current
    val xpRequired = com.example.data.GameConfig.getXpNeededForLevel(level)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.secondaryAccent,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Class Level $level",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
            // Streak counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(themeColors.secondaryAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = themeColors.secondaryAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streak DAY STREAK",
                    color = themeColors.secondaryAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SoloXpBar(currentXp = xp, requiredXp = xpRequired)
    }
}

@Composable
fun DailyQuestsWidget(todayItems: List<UnifiedItem>, viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val todayDate = viewModel.getTodayDateString()

    if (todayItems.isEmpty()) {
        Text(
            text = "No quests listed for today, Monarch. Access Quests menu to summon new trials.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            todayItems.take(3).forEach { item ->
                val isCompleted = if (item.isHabit) item.isCompletedOn(todayDate) else item.isCompleted
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCompleted) themeColors.primaryAccent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = if (isCompleted) themeColors.primaryAccent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.toggleItemCompletion(item) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleItemCompletion(item) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "Complete",
                                tint = if (isCompleted) themeColors.primaryAccent else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) Color.Gray else Color.White,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryBadge(category = item.category)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${item.xpReward} XP",
                                    fontSize = 10.sp,
                                    color = themeColors.secondaryAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    DifficultyBadge(difficulty = item.difficulty)
                }
            }
            if (todayItems.size > 3) {
                Text(
                    text = "and ${todayItems.size - 3} more trials awaiting in Daily menu...",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FieldBotWidget(message: String) {
    val themeColors = LocalSoloThemeColors.current

    // Pulsing aura animation for the companion
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing Companion Orb
        Box(
            modifier = Modifier
                .size(54.dp)
                .drawBehind {
                    drawCircle(
                        color = themeColors.primaryAccent.copy(alpha = 0.15f * pulseScale),
                        radius = this.size.minDimension / 1.5f
                    )
                    drawCircle(
                        color = themeColors.primaryAccent.copy(alpha = 0.35f * pulseScale),
                        radius = this.size.minDimension / 2.2f
                    )
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, themeColors.primaryAccent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = null,
                tint = themeColors.secondaryAccent,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Dialogue Glass Bubble
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF131024))
                .border(0.5.dp, themeColors.border.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color.LightGray,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PomodoroWidget(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val min = viewModel.pomodoroSecondsLeft / 60
    val sec = viewModel.pomodoroSecondsLeft % 60
    val timeStr = String.format("%02d:%02d", min, sec)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (viewModel.pomodoroMode == "WORK") "DEEP WORK CHAMBER" else "REST GATEWAY",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (viewModel.pomodoroMode == "WORK") "Grind limits to earn 75 XP." else "Regain energy before next raid.",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.pomodoroIsRunning = !viewModel.pomodoroIsRunning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.pomodoroIsRunning) Color.DarkGray else themeColors.primaryAccent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (viewModel.pomodoroIsRunning) "PAUSE" else "ENGAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = {
                        viewModel.pomodoroIsRunning = false
                        viewModel.pomodoroSecondsLeft = if (viewModel.pomodoroMode == "WORK") 1500 else 300
                    },
                    border = BorderStroke(0.5.dp, Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("RE-SEAL", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Circular Timer View
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            val ratio = (viewModel.pomodoroSecondsLeft.toFloat() / viewModel.pomodoroTotalSeconds.toFloat()).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color(0xFF1E1E2C), style = Stroke(width = 6.dp.toPx()))
                drawArc(
                    color = themeColors.primaryAccent,
                    startAngle = -90f,
                    sweepAngle = ratio * 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = timeStr,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun HabitsWidget(todayItems: List<UnifiedItem>, viewModel: SoloLevelingViewModel) {
    val habits = todayItems.filter { it.isHabit }
    val todayStr = viewModel.getTodayDateString()

    if (habits.isEmpty()) {
        Text(
            text = "No active disciplines (habits) scheduled today.",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            habits.take(3).forEach { habit ->
                val isCompleted = habit.isCompletedOn(todayStr)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x0EFFFFFF))
                        .clickable { viewModel.toggleItemCompletion(habit) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = rememberVectorIcon(habit.iconName),
                            contentDescription = null,
                            tint = Color(AndroidColor.parseColor(habit.colorHex)),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = habit.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) Color.Gray else Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleItemCompletion(habit) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Complete",
                            tint = if (isCompleted) LocalSoloThemeColors.current.primaryAccent else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricsWidget(viewModel: SoloLevelingViewModel) {
    val records by viewModel.allHistoryRecords.collectAsState()
    val todayStr = viewModel.getTodayDateString()
    val record = records.firstOrNull { it.date == todayStr } ?: com.example.data.HistoryRecord(date = todayStr)

    val moodEmojis = listOf("💀", "😴", "😐", "🙂", "⚡")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Sleep Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sleep Chamber", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${String.format("%.1f", record.sleepHours)} Hours", fontSize = 12.sp, color = LocalSoloThemeColors.current.primaryAccent)
            }
            Slider(
                value = record.sleepHours,
                onValueChange = { viewModel.updateSleepHours(todayStr, it) },
                valueRange = 0f..12f,
                steps = 23,
                colors = SliderDefaults.colors(
                    thumbColor = LocalSoloThemeColors.current.primaryAccent,
                    activeTrackColor = LocalSoloThemeColors.current.primaryAccent
                )
            )
        }

        // Mood Row
        Column {
            Text("Aura Calibration (Mood)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..5) {
                    val isSelected = record.moodRating == i
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LocalSoloThemeColors.current.primaryAccent.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) LocalSoloThemeColors.current.primaryAccent else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateMoodRating(todayStr, i) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(moodEmojis[i - 1], fontSize = 18.sp)
                        Text(
                            text = when(i) {
                                1 -> "Exhaust"
                                5 -> "Limitless"
                                else -> "Level $i"
                            },
                            fontSize = 8.sp,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaterWidget(viewModel: SoloLevelingViewModel) {
    val records by viewModel.allHistoryRecords.collectAsState()
    val todayStr = viewModel.getTodayDateString()
    val record = records.firstOrNull { it.date == todayStr } ?: com.example.data.HistoryRecord(date = todayStr)

    val themeColors = LocalSoloThemeColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Vessel Hydration", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${record.waterMilliliters} / 2500 ml Tracked", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.updateWaterWater(record, 250) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("+250ml", fontSize = 10.sp, color = Color.White)
                }
                Button(
                    onClick = { viewModel.updateWaterWater(record, 500) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("+500ml", fontSize = 10.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = { viewModel.updateWaterMilliliters(todayStr, 0) },
                    border = BorderStroke(0.5.dp, Color.Gray),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Empty", fontSize = 10.sp, color = Color.LightGray)
                }
            }
        }

        // Hydration fluid bar
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E2C))
                .border(1.dp, themeColors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val fillHeightRatio = (record.waterMilliliters.toFloat() / 2500f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillHeightRatio)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        )
                    )
            )
        }
    }
}

private fun SoloLevelingViewModel.updateWaterWater(record: com.example.data.HistoryRecord, amount: Int) {
    updateWaterMilliliters(getTodayDateString(), record.waterMilliliters + amount)
}

@Composable
fun JournalPreviewWidget(viewModel: SoloLevelingViewModel) {
    val records by viewModel.allHistoryRecords.collectAsState()
    val todayStr = viewModel.getTodayDateString()
    val record = records.firstOrNull { it.date == todayStr } ?: com.example.data.HistoryRecord(date = todayStr)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Chronicle Entry (Today)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Text(
            text = record.journalEntry.ifEmpty { "Unwritten chronicle segment. Enter Daily View to log todays thoughts..." },
            fontSize = 11.sp,
            color = if (record.journalEntry.isEmpty()) Color.Gray else Color.LightGray,
            lineHeight = 15.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
