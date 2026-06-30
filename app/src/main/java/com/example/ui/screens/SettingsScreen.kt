package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSoloThemeColors
import com.example.viewmodel.SoloLevelingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SoloLevelingViewModel) {
    val themeColors = LocalSoloThemeColors.current
    val context = LocalContext.current

    val themesList = listOf(
        "Solo Leveling" to "Electric Violet & Warm Coral",
        "Crimson Monarch" to "Blood Crimson & Imperial Gold",
        "Forest" to "Emerald Green & Solar Amber",
        "Cyberpunk" to "Neon Cyan & Hot Magenta",
        "Midnight" to "Deep Ocean Blue & Mint Teal",
        "Pure Dark" to "Monochrome Slate & Orchid Violet",
        "Frost" to "Glacial Ice Blue & Crystal White"
    )

    var backupText by remember { mutableStateOf("") }
    var importText by remember { mutableStateOf("") }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Header
        item {
            Column {
                Text(
                    text = "SYSTEM ENGINE OVERRIDES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "System Core Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Theme Switcher Section
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CALIBRATE THEME INTERFACE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themesList.forEach { (name, desc) ->
                        val isSelected = viewModel.currentTheme == name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) themeColors.primaryAccent.copy(alpha = 0.2f) else Color(0x0EFFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) themeColors.primaryAccent else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.updateTheme(name) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(desc, color = Color.Gray, fontSize = 10.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = themeColors.secondaryAccent)
                            }
                        }
                    }
                }
            }
        }

        // Data Backup / Restore section
        item {
            SoloGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CHRONICLE BACKUP VAULT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primaryAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Export and secure your hunter profiles, progression level histories, and active trials database locally.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val data = viewModel.exportBackupString()
                            backupText = data
                            // Copy to system clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Solo Leveling OS Backup", data)
                            clipboard.setPrimaryClip(clip)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export & Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val success = viewModel.importBackupString(importText)
                            if (success) {
                                showImportSuccessDialog = true
                                importText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.FileDownload, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import / Seal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (backupText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131024))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Backup string copied to clipboard! Keep it in a safe place.",
                            fontSize = 11.sp,
                            color = Color.Green,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Text field to paste import
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Paste Backup String here to Restore", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.primaryAccent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                    )
                )
            }
        }

        // Wipe / Reset section
        item {
            SoloGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "DANGER TERMINAL PROCESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Wipe and erase all local progress profiles, level histories, active habits, and tasks databases completely.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showResetConfirmationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DeleteForever, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Purge Operating System", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }

    // Success Dialogue
    if (showImportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showImportSuccessDialog = false },
            title = { Text("System Synchronization", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Vault Backup imported successfully! All records, character profiles, and active quests are aligned.", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { showImportSuccessDialog = false }) {
                    Text("Decline Gate", color = themeColors.primaryAccent)
                }
            },
            containerColor = Color(0xFF131024),
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Reset Dialogue
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmationDialog = false },
            title = { Text("Confirm Total System Purge?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to completely erase your RPG progress and reset your leveling operating system? This process is irreversible.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetConfirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("PROCEED PURGE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmationDialog = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF131024),
            shape = RoundedCornerShape(18.dp)
        )
    }
}
