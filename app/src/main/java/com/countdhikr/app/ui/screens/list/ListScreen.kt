package com.countdhikr.app.ui.screens.list

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countdhikr.app.data.model.DailyDhikr
import com.countdhikr.app.data.model.DailyDhikrItem
import com.countdhikr.app.data.model.DailyDhikrStatus
import com.countdhikr.app.data.model.Dhikr
import com.countdhikr.app.data.model.Dua
import com.countdhikr.app.ui.screens.home.AppTitlePill
import com.countdhikr.app.ui.theme.LocalThemeIsDark
import com.countdhikr.app.util.DateUtils
import kotlinx.coroutines.launch

// ── Premium Design Tokens ──────────────────────────────────────────────────
private val Emerald = Color(0xFF1AA34A)
private val EmeraldDark = Color(0xFF059669)
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
fun ListScreen(viewModel: ListViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddDhikr by viewModel.showAddDhikrDialog.collectAsState()
    val showAddDua by viewModel.showAddDuaDialog.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val selectedTabIndex = pagerState.currentPage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ROUTINES & BOOK",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // ── Premium Capsule Tab Switcher (Pager-connected) ─────────────
            val isDark = LocalThemeIsDark.current
            val capsuleBg = MaterialTheme.colorScheme.surfaceVariant
            val capsuleBorder = if (isDark) Color.White.copy(alpha = 0.06f) else Emerald.copy(alpha = 0.15f)

            BoxWithConstraints(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(capsuleBg)
                    .border(1.dp, capsuleBorder, RoundedCornerShape(24.dp))
                    .padding(3.dp)
            ) {
                val tabTitles = listOf("DAILY DHIKRS", "MY DUAS")
                val width = maxWidth
                val pillWidth = width / 2
                val targetOffset = if (selectedTabIndex == 0) 0.dp else pillWidth
                
                val slidingOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "tabPillOffset"
                )

                // Sliding gold pill background
                Box(
                    modifier = Modifier
                        .offset(x = slidingOffset)
                        .width(pillWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .background(Gold)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        val tabTextColor by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF1A1A1A) else TextSecondary,
                            animationSpec = tween(250),
                            label = "tabText"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(21.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = tabTextColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tab Content using HorizontalPager (butter smooth, zero lag) ──
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> DhikrListContent(viewModel, uiState)
                    1 -> DuaListContent(viewModel, uiState)
                }
            }
        }

        // Animated rotation for the plus icon button to twist 90 deg when toggling tabs
        val fabRotation by animateFloatAsState(
            targetValue = if (selectedTabIndex == 0) 0f else 90f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "fabRotation"
        )

        // ── Premium Bottom-Right FAB (Flat Brand Green) ──────────────────────
        FloatingActionButton(
            onClick = {
                if (selectedTabIndex == 0) {
                    viewModel.showAddDhikrDialog.value = true
                } else {
                    viewModel.showAddDuaDialog.value = true
                }
            },
            containerColor = Emerald,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Item",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer(rotationZ = fabRotation) // Butter smooth spring twist transition!
            )
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────
    if (showAddDhikr) {
        AddDhikrDialog(
            onDismiss = { viewModel.showAddDhikrDialog.value = false },
            onSave = { title, target, arabic ->
                viewModel.addDhikr(title, target, arabic)
            }
        )
    }

    if (showAddDua) {
        AddDuaDialog(
            onDismiss = { viewModel.showAddDuaDialog.value = false },
            onSave = { arabic, desc ->
                viewModel.addDua(arabic, desc)
            }
        )
    }
}

@Composable
fun DhikrListContent(viewModel: ListViewModel, uiState: ListUiState) {
    val todayStr = remember { DateUtils.getTodayDateString() }
    
    val sortedDailyDhikrs = remember(uiState.dailyDhikrs) {
        uiState.dailyDhikrs.sortedByDescending { it.date }
    }

    val todayCard = remember(sortedDailyDhikrs, todayStr) {
        sortedDailyDhikrs.find { it.date == todayStr }
    }
    val historyCards = remember(sortedDailyDhikrs, todayStr) {
        sortedDailyDhikrs.filter { it.date != todayStr }
    }
    
    var showAllHistory by remember { mutableStateOf(false) }
    val visibleHistory = remember(showAllHistory, historyCards) {
        if (showAllHistory) historyCards else historyCards.take(3)
    }

    var isDailyDhikrExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Dhikr Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isDailyDhikrExpanded = !isDailyDhikrExpanded }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1A10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            "Daily Routines",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            "Track and backfill daily routines",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val daysSize = uiState.dailyDhikrs.size
                    Text(
                        text = if (daysSize == 1) "1 Day Tracked" else "$daysSize Days Tracked",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Icon(
                        imageVector = if (isDailyDhikrExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isDailyDhikrExpanded) {
            // Today's Card
            item {
                if (todayCard != null) {
                    DailyDhikrCard(
                        dailyDhikr = todayCard,
                        isToday = true,
                        activeDailyDhikrId = uiState.activeDailyDhikrId,
                        soundEnabled = uiState.soundEnabled,
                        onAddDhikr = { title, target, arabic ->
                            viewModel.addDhikrToDay(todayStr, title, target, arabic)
                        },
                        onDeleteDhikr = { itemId ->
                            viewModel.deleteDhikrFromDay(todayStr, itemId)
                        },
                        onIncrementDhikr = { itemId ->
                            viewModel.incrementDayDhikr(todayStr, itemId)
                        },
                        onResetDhikr = { itemId ->
                            viewModel.resetDayDhikr(todayStr, itemId)
                        },
                        onSelectDhikr = { itemId ->
                            viewModel.selectDailyDhikr(itemId)
                        }
                    )
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Emerald, strokeWidth = 3.dp)
                        }
                    }
                }
            }

            // History Section
            if (historyCards.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "History Logs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = SectionHeader,
                            letterSpacing = 0.5.sp
                        )
                        if (historyCards.size > 3) {
                            Text(
                                text = if (showAllHistory) "Show Less" else "View All (${historyCards.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Gold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showAllHistory = !showAllHistory }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(visibleHistory, key = { it.id }) { historyCard ->
                    DailyDhikrCard(
                        dailyDhikr = historyCard,
                        isToday = false,
                        activeDailyDhikrId = null,
                        soundEnabled = false,
                        onAddDhikr = { _, _, _ -> },
                        onDeleteDhikr = {},
                        onIncrementDhikr = {},
                        onResetDhikr = {},
                        onSelectDhikr = {}
                    )
                }
            }
        }

        // General Dhikrs Section
        item {
            Text(
                "My Saved Dhikrs",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = SectionHeader,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
        }

        // General Counter Option
        item {
            val isActive = uiState.activeDhikrId == null && uiState.activeDailyDhikrId == null
            val onClick = remember { { viewModel.selectDhikr("") } }
            GeneralDhikrCard(
                title = "General Counter",
                arabic = "∞",
                count = 0,
                target = 100,
                isActive = isActive,
                isGeneral = true,
                onClick = onClick,
                onDelete = {}
            )
        }

        if (uiState.dhikrs.isNotEmpty()) {
            items(uiState.dhikrs, key = { it.id }) { dhikr ->
                val isActive = uiState.activeDhikrId == dhikr.id && uiState.activeDailyDhikrId == null
                val onClick = remember(dhikr.id) { { viewModel.selectDhikr(dhikr.id) } }
                val onDelete = remember(dhikr.id) { { viewModel.deleteDhikr(dhikr.id) } }
                GeneralDhikrCard(
                    title = dhikr.title,
                    arabic = dhikr.arabic,
                    count = dhikr.count,
                    target = dhikr.target,
                    isActive = isActive,
                    isGeneral = false,
                    onClick = onClick,
                    onDelete = onDelete
                )
            }
        }

        // Add custom Dhikr card directly under the saved list
        item {
            AddCustomItemCard(
                title = "Add Your Dhikr",
                subtitle = "Create custom dhikr counter",
                onClick = { viewModel.showAddDhikrDialog.value = true }
            )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun DuaListContent(viewModel: ListViewModel, uiState: ListUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Must Recite Duas",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = SectionHeader,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        if (uiState.duas.isNotEmpty()) {
            items(uiState.duas, key = { it.id }) { dua ->
                val onDelete = remember(dua.id) { { viewModel.deleteDua(dua.id) } }
                DuaCard(
                    arabic = dua.arabic,
                    desc = dua.desc,
                    onDelete = onDelete
                )
            }
        }

        // Add custom Dua card directly under the saved list
        item {
            AddCustomItemCard(
                title = "Add Your Dua",
                subtitle = "Save favorite supplications",
                onClick = { viewModel.showAddDuaDialog.value = true }
            )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun DailyDhikrCard(
    dailyDhikr: DailyDhikr,
    isToday: Boolean,
    activeDailyDhikrId: String?,
    soundEnabled: Boolean,
    onAddDhikr: (String, Int, String?) -> Unit,
    onDeleteDhikr: (String) -> Unit,
    onIncrementDhikr: (String) -> Unit,
    onResetDhikr: (String) -> Unit,
    onSelectDhikr: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(isToday) }
    var showAddForm by remember { mutableStateOf(false) }
    val isDark = LocalThemeIsDark.current

    val totalCount = remember(dailyDhikr.dhikrs) { dailyDhikr.dhikrs.sumOf { it.count } }
    val totalTarget = remember(dailyDhikr.dhikrs) { dailyDhikr.dhikrs.sumOf { it.target } }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isGold = isToday, // Restore gold border to match Screenshot 2 perfectly!
        isToday = isToday
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Day Circle Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) Gold else (if (isDark) Color(0xFF2A3F35) else Color(0xFFEFF3EF))
                            )
                            .border(1.dp, if (isToday) Gold else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "DAY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) Color(0xFF1A1A1A) else TextSecondary,
                                lineHeight = 10.sp
                            )
                            Text(
                                dailyDhikr.dayNumber.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isToday) Color(0xFF1A1A1A) else TextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Info Area
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isToday) "Today" else DateUtils.formatDisplayDate(dailyDhikr.date),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (isToday) Gold else TextPrimary
                            )
                            StatusBadge(status = dailyDhikr.status, isToday = isToday)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text("🌙", fontSize = 10.sp)
                            Text(
                                dailyDhikr.hijriDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (dailyDhikr.dhikrs.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                totalCount.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                "/$totalTarget",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded List Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    ThinDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    if (dailyDhikr.dhikrs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "No dhikrs added yet. Start your daily routine!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            if (isToday && !showAddForm) {
                                OutlinedButton(
                                    onClick = { showAddForm = true },
                                    border = BorderStroke(1.dp, Emerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Dhikr to Today", color = Emerald, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            dailyDhikr.dhikrs.forEach { dhikr ->
                                val isActive = activeDailyDhikrId == dhikr.id

                                // Crucial performance fix: remember the lambda callbacks so they are stable!
                                val onIncrement = remember(dhikr.id) { { onIncrementDhikr(dhikr.id) } }
                                val onReset = remember(dhikr.id) { { onResetDhikr(dhikr.id) } }
                                val onSelect = remember(dhikr.id) { { onSelectDhikr(dhikr.id) } }
                                val onDelete = remember(dhikr.id) { { onDeleteDhikr(dhikr.id) } }

                                DailyDhikrRow(
                                    title = dhikr.title,
                                    arabic = dhikr.arabic,
                                    count = dhikr.count,
                                    target = dhikr.target,
                                    isActive = isActive,
                                    isToday = isToday,
                                    soundEnabled = soundEnabled,
                                    onIncrement = onIncrement,
                                    onReset = onReset,
                                    onSelect = onSelect,
                                    onDelete = onDelete
                                )
                            }
                        }
                    }

                    // Add form inline inside today's card
                    if (isToday) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (showAddForm) {
                            var nameInput by remember { mutableStateOf("") }
                            var arabicInput by remember { mutableStateOf("") }
                            var targetInput by remember { mutableStateOf(33) }
                            var showDropdown by remember { mutableStateOf(false) }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (LocalThemeIsDark.current) Color(0x0DFFFFFF) else Color(0x0D000000))
                                    .border(1.dp, if (LocalThemeIsDark.current) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    placeholder = { Text("Dhikr name (e.g. SubhanAllah)", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Emerald,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedContainerColor = Color(0x1A000000),
                                        unfocusedContainerColor = Color(0x0F000000)
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = arabicInput,
                                    onValueChange = { arabicInput = it },
                                    placeholder = { Text("Arabic text (optional)", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Emerald,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedContainerColor = Color(0x1A000000),
                                        unfocusedContainerColor = Color(0x0F000000)
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Target Count:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .background(if (LocalThemeIsDark.current) Color(0x15FFFFFF) else Color(0x0D000000), RoundedCornerShape(8.dp))
                                                .border(1.dp, if (LocalThemeIsDark.current) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showDropdown = true }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(targetInput.toString(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showDropdown,
                                            onDismissRequest = { showDropdown = false }
                                        ) {
                                            listOf(33, 99, 100, 500, 1000).forEach { amount ->
                                                DropdownMenuItem(
                                                    text = { Text(amount.toString(), fontWeight = FontWeight.Bold) },
                                                    onClick = {
                                                        targetInput = amount
                                                        showDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showAddForm = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = TextSecondary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            if (nameInput.isNotBlank()) {
                                                onAddDhikr(
                                                    nameInput.trim(),
                                                    targetInput,
                                                    arabicInput.trim().takeIf { it.isNotBlank() }
                                                )
                                                nameInput = ""
                                                arabicInput = ""
                                                showAddForm = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Emerald,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Add", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0B10B981), RoundedCornerShape(16.dp))
                                    .border(
                                        BorderStroke(1.dp, Emerald.copy(alpha = 0.2f)),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showAddForm = true }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Emerald, modifier = Modifier.size(16.dp))
                                    Text("Add Dhikr to Today", color = Emerald, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyDhikrRow(
    title: String,
    arabic: String?,
    count: Int,
    target: Int,
    isActive: Boolean,
    isToday: Boolean,
    soundEnabled: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val completed = count >= target
    val view = androidx.compose.ui.platform.LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isActive) Color(0x2B10B981) else Color(0x0DFFFFFF),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isActive) Emerald.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isToday) {
                if (soundEnabled) {
                    com.countdhikr.app.util.SoundPlayer.playClick(context)
                }
                onIncrement()
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                if (!arabic.isNullOrEmpty()) {
                    Text(
                        arabic,
                        fontWeight = FontWeight.Black,
                        color = Emerald,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp
                    )
                }
                if (completed) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Emerald,
                        modifier = Modifier.size(15.dp)
                    )
                }
                if (isActive && isToday) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Emerald)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "COUNTING",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Progress Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                val progress = if (target > 0) count.toFloat() / target else 0f
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (completed) Emerald else Gold,
                    trackColor = if (LocalThemeIsDark.current) Color(0x1AFFFFFF) else Color(0x1F000000)
                )
                Text(
                    "$count/$target",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.widthIn(min = 40.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        // Routine controls if Today
        if (isToday) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                // Play select for Counter
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(if (isActive) Emerald else Color(0x1F10B981), CircleShape)
                        .clip(CircleShape)
                        .clickable { onSelect() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Select to count",
                        tint = if (isActive) Color.White else Emerald,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Reset Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(if (LocalThemeIsDark.current) Color(0x0DFFFFFF) else Color(0x0D000000), CircleShape)
                        .clip(CircleShape)
                        .clickable { onReset() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0x1AEF4444), CircleShape)
                        .clip(CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: DailyDhikrStatus, isToday: Boolean) {
    if (isToday) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x201AA34A))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "Active",
                color = Color(0xFF1AA34A),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
        return
    }

    when (status) {
        DailyDhikrStatus.COMPLETED -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x201AA34A))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "✓ Done",
                    color = Color(0xFF1AA34A),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        DailyDhikrStatus.PARTIAL -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x20E8A020))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE8A020),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        "Partial",
                        color = Color(0xFFE8A020),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        DailyDhikrStatus.MISSED -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x1FEF4444))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "✕ Missed",
                    color = Color(0xFFEF4444),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun GeneralDhikrCard(
    title: String,
    arabic: String?,
    count: Int,
    target: Int,
    isActive: Boolean,
    isGeneral: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val activeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .graphicsLayer {
                shadowElevation = if (isActive) 8f else 0f
            }
            .drawBehind {
                if (isActive) {
                    drawRoundRect(
                        color = Gold.copy(alpha = activeGlowAlpha),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )
                }
            },
        isGold = isActive,
        isToday = isActive
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
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circle Count Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Emerald else (if (LocalThemeIsDark.current) Color(0x0DFFFFFF) else Color(0x0D000000)))
                        .border(1.dp, if (isActive) Gold.copy(alpha = 0.5f) else (if (LocalThemeIsDark.current) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isGeneral) "∞" else count.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isActive) Color.White else TextPrimary
                    )
                }

                // Title Area
                Column {
                    Text(
                        title,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    if (!arabic.isNullOrEmpty() && !isGeneral) {
                        Text(
                            arabic,
                            fontWeight = FontWeight.Black,
                            color = Emerald,
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        "Target: ${if (isGeneral) "∞" else target}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x26D4AF37))
                            .border(0.5.dp, Gold.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            color = Gold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (!isGeneral) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x1AEF4444), CircleShape)
                            .clip(CircleShape)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DuaCard(
    arabic: String,
    desc: String,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (LocalThemeIsDark.current) Color(0x0DFFFFFF) else Color(0x0D000000))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "DUA RECORD",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0x1AEF4444), CircleShape)
                        .clip(CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                arabic,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Right,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                color = Emerald,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 32.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                desc,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

// ── Dialogs ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDhikrDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("33") }
    var arabic by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(CardBg)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp)),
        confirmButton = {
            Button(
                onClick = {
                    val targetInt = target.toIntOrNull() ?: 33
                    if (title.isNotBlank()) {
                        onSave(title.trim(), targetInt, arabic.trim().takeIf { it.isNotBlank() })
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                "Add Custom Dhikr",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Dhikr Name (e.g. Al-Hamdulillah)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Emerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = Emerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arabic,
                    onValueChange = { arabic = it },
                    label = { Text("Arabic Text (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Emerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = Emerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Emerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = Emerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDuaDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var arabic by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(CardBg)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp)),
        confirmButton = {
            Button(
                onClick = {
                    if (arabic.isNotBlank() && desc.isNotBlank()) {
                        onSave(arabic.trim(), desc.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                "Add Custom Dua",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = arabic,
                    onValueChange = { arabic = it },
                    label = { Text("Arabic Text") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Emerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = Emerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Translation / Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Emerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = Emerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// ── Thin Custom Divider ───────────────────────────────────────────────────
@Composable
fun ThinDivider(modifier: Modifier = Modifier) {
    val isDark = LocalThemeIsDark.current
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dividerColor)
    )
}

// ── Glassmorphism Custom Card ──────────────────────────────────────────────
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isGold: Boolean = false,
    isToday: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalThemeIsDark.current
    val bg = if (isDark) {
        Color(0xFF162820)
    } else {
        Color(0xFFFFFFFF)
    }
    
    val border = when {
        isGold || isToday -> BorderStroke(1.5.dp, Gold)
        isDark -> BorderStroke(1.dp, Color(0xFF222235))
        else -> BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f))
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = bg,
            contentColor = TextPrimary
        ),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

// ── Custom "Add Your Dhikr / Dua" Card matching Mockup ──────────────────────
@Composable
fun AddCustomItemCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = LocalThemeIsDark.current
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Gold circular plus icon button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x26F59E0B) else Color(0xFFFEF3C7))
                    .border(1.dp, Gold.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Gold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
