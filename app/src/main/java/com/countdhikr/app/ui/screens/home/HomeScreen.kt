package com.countdhikr.app.ui.screens.home

import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countdhikr.app.ui.screens.list.GlassCard
import com.countdhikr.app.util.DateUtils

import com.countdhikr.app.ui.theme.LocalThemeIsDark

// ── Premium Colors ─────────────────────────────────────────────────────────────
private val Emerald = Color(0xFF1AA34A)
private val EmeraldDark = Color(0xFF15803D)
private val Gold = Color(0xFFD4AF37)

private val TextPrimary: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFFF3F4F6) else Color(0xFF171717)

private val TextSecondary: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0xFF9CA3AF) else Color(0xFF525252)

private val IconCircleBg: Color
    @Composable
    get() = if (LocalThemeIsDark.current) Color(0x2610B981) else Color(0x1F10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    // Play click sound on increments
    LaunchedEffect(uiState.count) {
        if (uiState.soundEnabled && uiState.count > 0) {
            com.countdhikr.app.util.SoundPlayer.playClick(context)
        }
    }

    // ── Target-reached ripple ──────────────────────────────────────────────
    var showRipple by remember { mutableStateOf(false) }
    val rippleScale = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(1f) }

    LaunchedEffect(uiState.count) {
        val count = uiState.count
        val target = uiState.target
        if (count > 0 && target > 0 && count % target == 0) {
            showRipple = true
            rippleScale.snapTo(0f)
            rippleAlpha.snapTo(1f)
            launch { rippleScale.animateTo(2.5f, tween(800, easing = FastOutSlowInEasing)) }
            launch { rippleAlpha.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
            delay(800)
            showRipple = false
        }
    }

    // ── Pulse dot for daily mode ───────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    // ── Counter bounce (GPU-composited via graphicsLayer) ──────────────────
    val counterScale by animateFloatAsState(
        targetValue = if (showRipple) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "counterScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                if (showRipple) {
                    drawCircle(
                        color = Emerald.copy(alpha = 0.14f * rippleAlpha.value),
                        radius = size.maxDimension * rippleScale.value,
                        center = center
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP PILL BADGE ──────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            AppTitlePill()
            Spacer(modifier = Modifier.height(16.dp))

            // ── DAY INFO CARD ───────────────────────────────────────────────
            if (uiState.todayDhikr != null) {
                val today = uiState.todayDhikr!!
                val totalCount = today.dhikrs.sumOf { it.count }
                val totalTarget = today.dhikrs.sumOf { it.target }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGold = false,
                    isToday = false
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Gold)
                                    .border(1.dp, Gold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("DAY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), lineHeight = 10.sp)
                                    Text(today.dayNumber.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1A1A), lineHeight = 20.sp)
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Today's", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("Dhikr", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    // Mode badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (uiState.isInDailyMode) Color(0xFF162820) else Color(0x1A808080))
                                            .border(
                                                0.5.dp,
                                                if (uiState.isInDailyMode) Color(0xFF1AA34A) else Color.Transparent,
                                                RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (uiState.isInDailyMode) "DAILY MODE" else "GENERAL",
                                            color = if (uiState.isInDailyMode) Color(0xFF1AA34A) else TextSecondary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 3.dp)) {
                                    Text("📅", fontSize = 10.sp)
                                    Text(DateUtils.formatDisplayDate(today.date), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x1AD4AF37))
                                        .border(1.dp, Gold, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.NightsStay,
                                            contentDescription = "Hijri",
                                            tint = Gold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = today.hijriDate,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Gold
                                        )
                                    }
                                }
                            }
                        }

                        if (today.dhikrs.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(totalCount.toString(), fontWeight = FontWeight.Black, fontSize = 22.sp, color = TextPrimary)
                                Text("/$totalTarget", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text("TOTAL", fontSize = 7.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }

            // ── COUNTER CARD (Weighted to scale vertically + Tappable to increment)
            val interactionSource = remember { MutableInteractionSource() }
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null, // Sleek, no distracting default ripple
                        onClick = viewModel::onIncrement
                    ),
                isGold = false
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Big Counter — GPU-composited scale
                    Text(
                        text = uiState.count.toString(),
                        fontSize = 110.sp,
                        fontWeight = FontWeight.Black,
                        color = if (showRipple) Emerald else TextPrimary,
                        modifier = Modifier.graphicsLayer { scaleX = counterScale; scaleY = counterScale },
                        letterSpacing = (-2).sp
                    )

                    // Reset / Save Contrast Row (Translucent Grey Capsule Pills)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        // Reset button: Outlined warning red
                        Box(
                            modifier = Modifier
                                .background(Color.Transparent, RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.onReset() }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RESET", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444), letterSpacing = 1.sp)
                        }

                        // Save button: Filled primary green
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1AA34A), RoundedCornerShape(12.dp))
                                .border(0.dp, Color.Transparent, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    Toast.makeText(context, "Progress Autosaved!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SAVE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dhikr name + dropdown
                    DhikrSelector(
                        name = uiState.name,
                        todayDhikr = uiState.todayDhikr,
                        activeDailyDhikrItem = uiState.activeDailyDhikrItem,
                        onSelectDailyDhikr = viewModel::onSelectDailyDhikr,
                        onSelectGeneral = viewModel::setGeneralMode
                    )

                    // Target (Hidden in General Counter mode if target is 0)
                    if (uiState.target > 0) {
                        Text(
                            "TARGET: ${uiState.target}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    // Daily mode indicator
                    if (uiState.isInDailyMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .graphicsLayer { alpha = pulseAlpha }
                                    .background(Emerald)
                            )
                            Text("COUNTING DAILY ROUTINE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Emerald, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── BOTTOM ACTIONS ROW (Circular buttons pushed slightly higher for consistency)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vibrate Toggle Card (Circle shape)
                ActionCircle(
                    active = uiState.vibrateEnabled,
                    onClick = viewModel::onToggleVibrate
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibrate Toggle",
                        tint = if (uiState.vibrateEnabled) Emerald else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Main circular Plus button
                CountButton(onClick = viewModel::onIncrement)

                // Sound Toggle Card (Circle shape)
                ActionCircle(
                    active = uiState.soundEnabled,
                    onClick = viewModel::onToggleSound
                ) {
                    Icon(
                        imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = if (uiState.soundEnabled) Emerald else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


// ── Reusable Components ────────────────────────────────────────────────────────

@Composable
fun AppTitlePill() {
    Surface(
        color = Gold.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("✨", fontSize = 12.sp)
            Text(
                text = "DHIKR COUNTER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Gold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun GlassPill(text: String, onClick: () -> Unit) {
    val isDark = LocalThemeIsDark.current
    val pillBg = if (isDark) Color(0x15FFFFFF) else Color(0x0D000000)
    val pillBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    Box(
        modifier = Modifier
            .background(pillBg, RoundedCornerShape(12.dp))
            .border(1.dp, pillBorder, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DhikrSelector(
    name: String,
    todayDhikr: com.countdhikr.app.data.model.DailyDhikr?,
    activeDailyDhikrItem: com.countdhikr.app.data.model.DailyDhikrItem?,
    onSelectDailyDhikr: (String) -> Unit,
    onSelectGeneral: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasDailyDhikrs = todayDhikr != null && todayDhikr.dhikrs.isNotEmpty()

    Box(contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = hasDailyDhikrs) { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
            if (hasDailyDhikrs) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = Emerald, modifier = Modifier.size(20.dp))
            }
        }

        if (hasDailyDhikrs) {
            val isDark = LocalThemeIsDark.current
            val menuBg = if (isDark) Color(0xFF1A1F2E) else Color(0xFFFFFFFF)
            val dividerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(menuBg)
            ) {
                DropdownMenuItem(
                    text = { Text("General Counter", fontWeight = FontWeight.Bold, color = TextPrimary) },
                    onClick = { onSelectGeneral(); expanded = false }
                )
                HorizontalDivider(color = dividerColor)
                todayDhikr!!.dhikrs.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("${item.count} / ${item.target}", fontSize = 11.sp, color = TextSecondary)
                                }
                                if (activeDailyDhikrItem?.id == item.id) {
                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = Emerald, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        onClick = { onSelectDailyDhikr(item.id); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCircle(active: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val isDark = LocalThemeIsDark.current
    val inactiveBg = if (isDark) Color(0x0DFFFFFF) else Color(0x0D000000)
    val inactiveBorder = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(if (active) IconCircleBg else inactiveBg, CircleShape)
            .border(1.dp, if (active) Emerald.copy(alpha = 0.25f) else inactiveBorder, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun CountButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "countScale"
    )
    val isDark = LocalThemeIsDark.current
    val buttonBg = if (isDark) Color(0xFF162820) else Color(0xFF1AA34A)
    val buttonBorder = if (isDark) Color(0xFF2A3F35) else Color.Transparent

    Box(
        modifier = Modifier
            .size(88.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(buttonBg, CircleShape)
            .border(2.dp, buttonBorder, CircleShape)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increment Count",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp)
        )
    }
}
