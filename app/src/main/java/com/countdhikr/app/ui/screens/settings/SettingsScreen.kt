package com.countdhikr.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countdhikr.app.data.model.VibrateIntensity
import com.countdhikr.app.ui.screens.home.AppTitlePill
import com.countdhikr.app.ui.screens.list.GlassCard
import com.countdhikr.app.ui.theme.LocalThemeIsDark

// ── Premium Colors ─────────────────────────────────────────────────────────────
private val Emerald = Color(0xFF1AA34A)
private val Gold = Color(0xFFD4AF37)

private val TextPrimary: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFF3F4F6) else Color(0xFF171717)

private val TextSecondary: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF9CA3AF) else Color(0xFF525252)

private val SectionHeader: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF6B7280) else Color(0xFF404040)

private val IconCircleBg: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0x2610B981) else Color(0x1F10B981)

private val CardBg: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0x990A121D) else Color(0xF5FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    var showFactoryResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("SETTINGS", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ═══════════════════ GENERAL ═══════════════════════════════════
            item { SectionLabel("GENERAL") }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Dark Mode
                        SettingRow(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Mode",
                            subtitle = "Adjust screen theme",
                            trailing = {
                                PremiumSwitch(checked = settings.darkMode, onCheckedChange = { viewModel.toggleDarkMode() })
                            }
                        )
                        ThinDivider()
                        // Language
                        SettingRow(
                            icon = Icons.Default.Translate,
                            title = "Language",
                            subtitle = "Preferred app interface language",
                            trailing = {
                                Text("English", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                            }
                        )
                        ThinDivider()
                        // Day Number Settings
                        DayNumberSettingsRow(
                            offset = uiState.dayNumberOffset,
                            onSave = { viewModel.setDayNumberOffset(it) }
                        )
                    }
                }
            }

            // ═══════════════════ SOUNDS & FEEDBACK ═════════════════════════
            item { SectionLabel("SOUNDS & FEEDBACK") }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Haptic Feedback
                        SettingRow(
                            icon = Icons.Default.Vibration,
                            title = "Haptic Feedback",
                            subtitle = "Vibrate device on counts & alarms",
                            trailing = {
                                PremiumSwitch(checked = settings.vibrate, onCheckedChange = { viewModel.toggleVibrate() })
                            }
                        )

                        // Vibration Intensity selector (inside same card)
                        AnimatedVisibility(visible = settings.vibrate) {
                            Column {
                                ThinDivider()
                                Text(
                                    "VIBRATION INTENSITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SectionHeader,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VibrateIntensity.entries.forEach { option ->
                                        val isSelected = settings.vibrateIntensity == option
                                        val isDark = LocalThemeIsDark.current
                                        val intensityBg = if (isSelected) Emerald else (if (isDark) Color(0x15FFFFFF) else Color(0x0D000000))
                                        val intensityBorder = if (isSelected) Emerald else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(intensityBg, RoundedCornerShape(14.dp))
                                                .border(1.dp, intensityBorder, RoundedCornerShape(14.dp))
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { viewModel.setVibrateIntensity(option) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                option.name.lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else TextSecondary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        ThinDivider()
                        // Click Sound
                        SettingRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Click Sound",
                            subtitle = "Play distinct audio click on count increments",
                            trailing = {
                                PremiumSwitch(checked = settings.sound, onCheckedChange = { viewModel.toggleSound() })
                            }
                        )
                    }
                }
            }

            // ═══════════════════ NOTIFICATIONS & ALERTS ════════════════════
            item { SectionLabel("NOTIFICATIONS & ALERTS") }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingRow(
                            icon = Icons.Default.Notifications,
                            title = "Daily Dhikr Reminder",
                            subtitle = "Notify after each salat if daily dhikr not completed",
                            trailing = {
                                PremiumSwitch(checked = settings.dhikrReminderEnabled, onCheckedChange = { viewModel.toggleDhikrReminder() })
                            }
                        )
                        ThinDivider()
                        SettingRow(
                            icon = Icons.Default.AccessAlarm,
                            title = "Salat reminders",
                            subtitle = "Notify 10 min after prayer time (2 min for Maghrib)",
                            trailing = {
                                PremiumSwitch(checked = settings.salatReminderEnabled, onCheckedChange = { viewModel.toggleSalatReminder() })
                            }
                        )
                        ThinDivider()
                        SettingRow(
                            icon = Icons.Default.NotificationsActive,
                            title = "Master Alerts Switch",
                            subtitle = "Enable all background prayer & reminder notifications",
                            trailing = {
                                PremiumSwitch(checked = settings.reminderNotification, onCheckedChange = { viewModel.toggleReminder() })
                            }
                        )
                    }
                }
            }

            // ═══════════════════ ABOUT ═════════════════════════════════════
            item { SectionLabel("ABOUT") }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingRow(
                            icon = Icons.Default.Info,
                            title = "Version",
                            subtitle = "CountDhikr App — Beta Debug 3",
                            trailing = {
                                Text("1.5.0-beta-debug3", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                            }
                        )
                        ThinDivider()
                        SettingRow(
                            icon = Icons.Default.Favorite,
                            title = "Support The Dev",
                            subtitle = "Share your generosity. Keeps the app ad-free",
                            iconTint = Color(0xFFEF4444),
                            trailing = { Text("💳 ☕", fontSize = 18.sp) }
                        )
                    }
                }
            }

            // ═══════════════════ DANGER ZONE ══════════════════════════════
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AEF4444), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x33EF4444), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showFactoryResetDialog = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Reset", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Text("Reset All Data", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }

    // ── Factory Reset Dialog ────────────────────────────────────────────────
    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = { Text("Factory Reset", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset all data? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.factoryReset(); showFactoryResetDialog = false }) {
                    Text("Reset", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) { Text("Cancel") }
            },
            containerColor = Color(0xFF1A1F2E),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Reusable Premium Components
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = SectionHeader,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.05f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = Emerald,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon in circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }

        trailing()
    }
}

@Composable
fun ToggleSettingCard(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (description != null) {
                    Text(description, fontSize = 12.sp, color = TextSecondary)
                }
            }
            PremiumSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun PremiumSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val isDark = LocalThemeIsDark.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Emerald,
            checkedBorderColor = Emerald,
            uncheckedThumbColor = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF),
            uncheckedTrackColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB),
            uncheckedBorderColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
        )
    )
}

@Composable
private fun DayNumberSettingsRow(offset: Int, onSave: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var tempOffset by remember(offset) { mutableStateOf(offset.toString()) }
    val isDark = LocalThemeIsDark.current

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Emerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Day", tint = Emerald, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Day Number Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("Current offset: $offset days", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ThinDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "If you've been counting dhikr before using this app, enter how many days you've already counted.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Starting offset:", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = tempOffset,
                        onValueChange = { tempOffset = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Emerald
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("⚠️ This will recalculate all day numbers", fontSize = 10.sp, color = Gold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isDark) Color(0x15FFFFFF) else Color(0x0D000000), RoundedCornerShape(12.dp))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { tempOffset = offset.toString(); expanded = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Emerald, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSave(tempOffset.toIntOrNull() ?: 0); expanded = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
