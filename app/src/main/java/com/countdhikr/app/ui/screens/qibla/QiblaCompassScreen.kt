package com.countdhikr.app.ui.screens.qibla

import android.Manifest
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countdhikr.app.ui.screens.home.AppTitlePill
import com.countdhikr.app.ui.screens.list.GlassCard
import com.countdhikr.app.ui.theme.LocalThemeIsDark
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(isPageActive: Boolean, viewModel: QiblaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSensorDetails by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            if (isPageActive) {
                viewModel.startSensors()
            }
        }
    }

    DisposableEffect(lifecycleOwner, isPageActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isPageActive) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopSensors()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        if (isPageActive && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.startSensors()
        } else {
            viewModel.stopSensors()
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopSensors()
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
                text = "QIBLA COMPASS",
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
                    .clickable { viewModel.startSensors() },
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

        if (!uiState.isGyroscopeAvailable) {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚠️", fontSize = 36.sp)
                        Text(
                            text = uiState.error ?: "Compass sensors are unavailable on this device.",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            return
        }

        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Emerald, strokeWidth = 3.dp)
            }
        } else {
            // ── Main Content Scroll Column ─────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // ── Compact Status Row (Tapping opens Modern Dialog) ───────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showSensorDetails = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Emerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Compass Status: Active",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "View Telemetry",
                                fontSize = 11.sp,
                                color = Gold,
                                fontWeight = FontWeight.Black
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Details",
                                tint = Gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // ── Modern Telemetry Glass Dialog ─────────────────────────────────────
                if (showSensorDetails) {
                    val bearing = uiState.qiblaDirection ?: 0f
                    val distance = uiState.distanceToKaaba ?: 0.0
                    val latStr = if (uiState.latitude != null) String.format(Locale.US, "%.4f", uiState.latitude) else "---"
                    val lngStr = if (uiState.longitude != null) String.format(Locale.US, "%.4f", uiState.longitude) else "---"
                    
                    AlertDialog(
                        onDismissRequest = { showSensorDetails = false },
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x1F10B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = Emerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    "SENSOR TELEMETRY",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                // Accuracy Status Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Accuracy Level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Box(modifier = Modifier.size(width = 3.dp, height = 6.dp).background(Emerald, RoundedCornerShape(1.dp)))
                                            Box(modifier = Modifier.size(width = 3.dp, height = 9.dp).background(Emerald, RoundedCornerShape(1.dp)))
                                            Box(modifier = Modifier.size(width = 3.dp, height = 12.dp).background(Emerald, RoundedCornerShape(1.dp)))
                                        }
                                        Text(
                                            text = "High Accuracy",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Emerald
                                        )
                                    }
                                }
                                
                                ThinDivider(modifier = Modifier.padding(vertical = 4.dp))
                                
                                // Detail Rows
                                DetailRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "User Coordinates",
                                    value = "$latStr° N, $lngStr° E"
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.Explore,
                                    label = "Qibla Direction",
                                    value = String.format(Locale.US, "%.1f° from North", bearing)
                                )
                                
                                DetailRow(
                                    icon = Icons.Default.Adjust,
                                    label = "Distance to Mecca",
                                    value = String.format(Locale.US, "%,.0f km", distance)
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Calibration hint inside dialog
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x0DFFFFFF))
                                        .border(0.5.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("💡", fontSize = 12.sp)
                                    Text(
                                        text = "Rotate phone in a figure-8 motion if sensor drift occurs.",
                                        fontSize = 10.sp,
                                        color = Gold,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { showSensorDetails = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Close", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        },
                        containerColor = Color(0xFF0F1420),
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                    )
                }

                // ── Instructions Hint ──────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                ) {
                    val instructionText = if (uiState.isAlignedWithQibla) "Aligned with Qibla Kaaba!" else "Turn to align compass arrow"
                    val instructionColor = if (uiState.isAlignedWithQibla) Emerald else TextPrimary
                    
                    Text(
                        text = instructionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = instructionColor,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Rotate phone until the green arrow points to the gold Kaaba target.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    )
                }

                // ── High-Fidelity Compass Visualizer ───────────────────────
                val heading = uiState.compassHeading ?: 0f
                val qibla = uiState.qiblaDirection ?: 0f
                
                val compassRotation by animateFloatAsState(
                    targetValue = -heading,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "compassRotation"
                )
                
                val needleRotation by animateFloatAsState(
                    targetValue = qibla - heading,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "needleRotation"
                )

                val density = LocalDensity.current
                val isAligned = uiState.isAlignedWithQibla

                val activeGlowAlpha by rememberInfiniteTransition("glow").animateFloat(
                    initialValue = 0.05f, targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
                    label = "glowAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(290.dp)
                        .background(Color(0x3D0A121D), CircleShape)
                        .border(1.dp, if (isAligned) Emerald.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Dial Background & Border
                    Canvas(
                        modifier = Modifier
                            .size(270.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                if (isAligned) Emerald.copy(alpha = 0.6f) else BorderColor,
                                CircleShape
                            )
                            .drawBehind {
                                if (isAligned) {
                                    drawCircle(
                                        color = Emerald.copy(alpha = activeGlowAlpha),
                                        radius = size.width / 2
                                    )
                                }
                            }
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2
                        
                        // Draw dial face
                        drawCircle(
                            color = Color(0xFF070B13),
                            radius = radius,
                            center = center
                        )

                        // Draw ticks
                        rotate(degrees = compassRotation) {
                            for (i in 0 until 360 step 5) {
                                val isMajor = i % 30 == 0
                                val isCardinal = i % 90 == 0
                                val tickLength = when {
                                    isCardinal -> 14.dp.toPx()
                                    isMajor -> 9.dp.toPx()
                                    else -> 5.dp.toPx()
                                }
                                val tickColor = when {
                                    isCardinal -> Gold.copy(alpha = 0.7f)
                                    isMajor -> Color.White.copy(alpha = 0.35f)
                                    else -> Color.White.copy(alpha = 0.12f)
                                }
                                val tickWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                                
                                rotate(degrees = i.toFloat()) {
                                    drawLine(
                                        color = tickColor,
                                        start = Offset(center.x, 0f),
                                        end = Offset(center.x, tickLength),
                                        strokeWidth = tickWidth
                                    )
                                }
                            }

                            // Render Cardinal Direction Labels (N, S, E, W)
                            val goldTextPaint = Paint().apply {
                                color = android.graphics.Color.parseColor("#D4AF37")
                                textSize = 17.sp.toPx()
                                typeface = Typeface.create("serif", Typeface.BOLD)
                                textAlign = Paint.Align.CENTER
                            }
                            
                            val silverTextPaint = Paint().apply {
                                color = android.graphics.Color.parseColor("#9CA3AF")
                                textSize = 14.sp.toPx()
                                typeface = Typeface.create("serif", Typeface.BOLD)
                                textAlign = Paint.Align.CENTER
                            }

                            drawContext.canvas.nativeCanvas.drawText(
                                "N", center.x, 26.dp.toPx(), goldTextPaint
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "S", center.x, size.height - 14.dp.toPx(), silverTextPaint
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "E", size.width - 18.dp.toPx(), center.y + 5.dp.toPx(), silverTextPaint
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "W", 18.dp.toPx(), center.y + 5.dp.toPx(), silverTextPaint
                            )
                        }

                        // Draw Emerald needle
                        rotate(degrees = needleRotation) {
                            drawLine(
                                color = Emerald,
                                start = center,
                                end = Offset(center.x, 34.dp.toPx()),
                                strokeWidth = 3.dp.toPx()
                            )

                            // Arrowhead path
                            val arrowHead = Path().apply {
                                moveTo(center.x, 16.dp.toPx())
                                lineTo(center.x - 8.dp.toPx(), 34.dp.toPx())
                                lineTo(center.x + 8.dp.toPx(), 34.dp.toPx())
                                close()
                            }
                            drawPath(path = arrowHead, color = Emerald)
                        }

                        // Central core ring
                        drawCircle(color = Emerald, radius = 7.dp.toPx(), center = center)
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = center)
                    }

                    // ── Kaaba target block (At top center of the compass) ──────
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-4).dp)
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1420))
                            .border(1.5.dp, Gold, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Beautiful Kaaba Cube representation
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF161922)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .padding(top = 1.dp)
                                        .background(Gold)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 4.dp, height = 7.dp)
                                        .background(Gold)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x0DFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
    }
}

@Composable
private fun ThinDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.05f))
    )
}
