package com.countdhikr.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object List : Screen("list", "Routine", Icons.AutoMirrored.Filled.List)
    data object Prayer : Screen("prayer", "Prayer", Icons.Filled.AccessTime)
    data object Qibla : Screen("qibla", "Qibla", Icons.Filled.Explore)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val allScreens: kotlin.collections.List<Screen> = listOf(Home, List, Prayer, Qibla, Settings)
    }
}
