package com.countdhikr.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.countdhikr.app.ui.screens.home.HomeScreen
import com.countdhikr.app.ui.screens.list.ListScreen
import com.countdhikr.app.ui.screens.prayer.PrayerScreen
import com.countdhikr.app.ui.screens.qibla.QiblaScreen
import com.countdhikr.app.ui.screens.settings.SettingsScreen
import com.countdhikr.app.ui.theme.LocalThemeIsDark
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 5 })

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = if (LocalThemeIsDark.current) Color(0xFF162820) else Color(0xFFFFFFFF),
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.height(72.dp) // Optimal premium navbar height
            ) {
                Screen.allScreens.forEachIndexed { index, screen ->
                    val selected = pagerState.targetPage == index

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1AA34A),
                            selectedTextColor = Color(0xFF1AA34A),
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color(0xFF8FA89E),
                            unselectedTextColor = Color(0xFF8FA89E)
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            userScrollEnabled = true // Hardware-accelerated slidable pages!
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> ListScreen(
                    onNavigateToHome = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                2 -> PrayerScreen()
                3 -> QiblaScreen(isPageActive = pagerState.currentPage == 3)
                4 -> SettingsScreen()
            }
        }
    }
}
