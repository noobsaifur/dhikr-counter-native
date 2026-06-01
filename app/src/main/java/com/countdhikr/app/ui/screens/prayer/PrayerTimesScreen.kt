package com.countdhikr.app.ui.screens.prayer

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countdhikr.app.data.model.SelectedCountry
import com.countdhikr.app.ui.screens.home.AppTitlePill
import com.countdhikr.app.ui.screens.list.GlassCard
import com.countdhikr.app.ui.theme.LocalThemeIsDark
import com.countdhikr.app.util.Constants
import com.countdhikr.app.util.DateUtils

// ── Premium Design Tokens ──────────────────────────────────────────────────
private val Emerald = Color(0xFF1AA34A)
private val EmeraldDark = Color(0xFF15803D)
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

private val CardBg: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0x990A121D) else Color(0xF5FFFFFF)

private val BorderColor: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0x1AFFFFFF) else Color(0x1210B981)

// Restored large list of countries for premium feel
val COUNTRIES = listOf(
    SelectedCountry("Saudi Arabia", "SA", 24.7136, 46.6753, 4),
    SelectedCountry("UAE", "AE", 25.2048, 55.2708, 8),
    SelectedCountry("Pakistan", "PK", 30.3753, 69.3451, 1),
    SelectedCountry("India", "IN", 20.5937, 78.9629, 1),
    SelectedCountry("Turkey", "TR", 38.9637, 35.2433, 13),
    SelectedCountry("Egypt", "EG", 26.8206, 30.8025, 5),
    SelectedCountry("Indonesia", "ID", -0.7893, 113.9213, 11),
    SelectedCountry("Malaysia", "MY", 4.2105, 101.9758, 3),
    SelectedCountry("Bangladesh", "BD", 23.685, 90.3563, 1),
    SelectedCountry("United Kingdom", "GB", 55.3781, -3.436, 2),
    SelectedCountry("United States", "US", 37.0902, -95.7129, 2),
    SelectedCountry("Canada", "CA", 56.1304, -106.3468, 2),
    SelectedCountry("Australia", "AU", -25.2744, 133.7751, 3)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(viewModel: PrayerTimesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.refreshPrayerTimes()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.useAutoLocation && uiState.times == null) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRAYER TIMES",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x1F10B981), CircleShape)
                    .clip(CircleShape)
                    .clickable { viewModel.refreshPrayerTimes() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Emerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Emerald, strokeWidth = 3.dp)
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️", fontSize = 36.sp)
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { viewModel.refreshPrayerTimes() },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // ── Header Card (Next Prayer Countdown & Today info) ────────
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        isGold = true,
                        isToday = true
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1F10B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = Emerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = uiState.locationName ?: "Mecca, SA",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (uiState.useAutoLocation) "Auto-detect GPS location" else "Manual country preset",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            ThinDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = remember { DateUtils.formatDisplayDate(DateUtils.getTodayDateString()) },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Gold.copy(alpha = 0.1f))
                                            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.NightsStay,
                                                contentDescription = "Hijri Calendar",
                                                tint = Gold,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = uiState.hijriDate ?: "Loading...",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Gold
                                            )
                                        }
                                    }
                                }

                                if (uiState.nextPrayer != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Emerald)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "${uiState.nextPrayer} in ${uiState.timeToNext}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Prayer Times Grid / Rows ────────────────────────────────
                if (uiState.times != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrayerTimeRow("Fajr", DateUtils.formatSystemTime(context, uiState.times!!.fajr), "🌅", uiState.nextPrayer == "Fajr")
                            PrayerTimeRow("Sunrise", DateUtils.formatSystemTime(context, uiState.times!!.sunrise), "☀️", uiState.nextPrayer == "Sunrise")
                            PrayerTimeRow("Dhuhr", DateUtils.formatSystemTime(context, uiState.times!!.dhuhr), "☀️", uiState.nextPrayer == "Dhuhr")
                            PrayerTimeRow("Asr", DateUtils.formatSystemTime(context, uiState.times!!.asr), "🌤️", uiState.nextPrayer == "Asr")
                            PrayerTimeRow("Maghrib", DateUtils.formatSystemTime(context, uiState.times!!.maghrib), "🌇", uiState.nextPrayer == "Maghrib")
                            PrayerTimeRow("Isha", DateUtils.formatSystemTime(context, uiState.times!!.isha), "🌙", uiState.nextPrayer == "Isha")
                        }
                    }
                }

                // ── Settings Title ──────────────────────────────────────────
                item {
                    Text(
                        text = "PRAYER CONFIGURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SectionHeader,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                    )
                }

                // ── Settings Container Card ─────────────────────────────────
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            // Auto Location Toggle
                            SettingToggleRow(
                                title = "Auto Detect Location",
                                subtitle = "Sync times with device GPS coordinates",
                                checked = uiState.useAutoLocation,
                                onCheckedChange = {
                                    viewModel.toggleAutoLocation()
                                    if (it) {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                }
                            )

                            // Country Selector Dropdown (visible if auto-location is off)
                            AnimatedVisibility(visible = !uiState.useAutoLocation) {
                                Column {
                                    ThinDivider()
                                    var countryExpanded by remember { mutableStateOf(false) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .clickable { countryExpanded = !countryExpanded }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Selected Country", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            Text(uiState.selectedCountry?.name ?: "Tap to choose country", fontSize = 11.sp, color = TextSecondary)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = uiState.selectedCountry?.name ?: "Select",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = countryExpanded,
                                        onDismissRequest = { countryExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        COUNTRIES.forEach { country ->
                                            DropdownMenuItem(
                                                text = { Text(country.name, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    viewModel.selectCountry(country)
                                                    countryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            ThinDivider()

                            // Calculation Method Dropdown
                            var methodExpanded by remember { mutableStateOf(false) }
                            val currentMethodName = remember(uiState.prayerMethod) {
                                Constants.PRAYER_METHODS.find { it.id == uiState.prayerMethod }?.name ?: "Standard Method"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { methodExpanded = !methodExpanded }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Calculation Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(currentMethodName, fontSize = 11.sp, color = TextSecondary)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = currentMethodName.take(18) + if (currentMethodName.length > 18) "..." else "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                }
                            }

                            DropdownMenu(
                                expanded = methodExpanded,
                                onDismissRequest = { methodExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Constants.PRAYER_METHODS.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            viewModel.setPrayerMethod(method.id)
                                            methodExpanded = false
                                        }
                                    )
                                }
                            }

                            ThinDivider()

                            // Azan Notifications
                            SettingToggleRow(
                                title = "Azan Notifications",
                                subtitle = "Alert at prayer time intervals",
                                checked = uiState.azanEnabled,
                                onCheckedChange = { viewModel.toggleAzan() }
                            )

                            // Azan detailed settings (visible if notifications enabled)
                            AnimatedVisibility(visible = uiState.azanEnabled) {
                                Column {
                                    SettingToggleRow(
                                        title = "Play Azan Sound",
                                        subtitle = "Audio recitation on alert trigger",
                                        checked = uiState.azanSound,
                                        onCheckedChange = { viewModel.toggleAzanSound() }
                                    )
                                    
                                    AnimatedVisibility(visible = uiState.azanSound) {
                                        var azanExpanded by remember { mutableStateOf(false) }
                                        val activeAzanUrl = uiState.customAzanUrl ?: Constants.AZAN_SOUNDS.first().url
                                        val activeAzanName = remember(activeAzanUrl) {
                                            Constants.AZAN_SOUNDS.find { it.url == activeAzanUrl }?.name ?: "Makkah"
                                        }
                                        
                                        Column {
                                            ThinDivider()
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(24.dp))
                                                    .clickable { azanExpanded = !azanExpanded }
                                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Select Azan Voice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                                    Text("Choose standard or specialized Azan sound", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = activeAzanName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Emerald
                                                    )
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = azanExpanded,
                                                onDismissRequest = { azanExpanded = false },
                                                modifier = Modifier.fillMaxWidth(0.8f)
                                            ) {
                                                Constants.AZAN_SOUNDS.forEach { sound ->
                                                    DropdownMenuItem(
                                                        text = { Text(sound.name + " Azan", fontWeight = FontWeight.Bold) },
                                                        onClick = {
                                                            viewModel.setAzanSoundUrl(sound.url)
                                                            azanExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    ThinDivider()
                                    SettingToggleRow(
                                        title = "Vibrate on Azan",
                                        subtitle = "Haptic feedback pattern during Azan",
                                        checked = uiState.azanVibrate,
                                        onCheckedChange = { viewModel.toggleAzanVibrate() }
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

import com.countdhikr.app.ui.theme.CardSurfaceDark

@Composable
fun PrayerTimeRow(name: String, time: String, icon: String, isNext: Boolean) {
    val isDark = LocalThemeIsDark.current
    val borderStroke = when {
        isNext -> BorderStroke(1.5.dp, Gold)
        isDark -> BorderStroke(1.dp, Color(0xFF222235))
        else -> BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f))
    }
    val bg = if (isDark) CardSurfaceDark else Color(0xFFFFFFFF)
    val contentColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF171717)
    val timeColor = if (isNext) Gold else (if (isDark) Color(0xFFF3F4F6) else Color(0xFF171717))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = bg,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isNext) Emerald else (if (isDark) Color(0x0DFFFFFF) else Color(0x0D000000)))
                        .border(1.dp, if (isNext) Gold else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 16.sp)
                }

                Column {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = if (isNext) FontWeight.Black else FontWeight.Bold,
                        color = contentColor
                    )
                    if (isNext) {
                        Text(
                            text = "ACTIVE PRAYER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Gold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Text(
                text = time,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = timeColor
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = LocalThemeIsDark.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
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
}

@Composable
private fun ThinDivider() {
    val isDark = LocalThemeIsDark.current
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dividerColor)
    )
}
