package com.countdhikr.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

// Global CompositionLocal driving custom sub-screens with robust architectural sync
val LocalThemeIsDark = compositionLocalOf { false }

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Emerald500,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,

    secondary = Gold500,
    onSecondary = Color.White,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold900,

    tertiary = Emerald700,
    onTertiary = Color.White,
    tertiaryContainer = Emerald100,
    onTertiaryContainer = Emerald900,

    background = SurfaceLight,
    onBackground = Neutral900,

    surface = SurfaceLight,
    onSurface = Neutral900,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Neutral600,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F9F6),
    surfaceContainer = CardSurfaceLight,
    surfaceContainerHigh = Color(0xFFEFF3EF),
    surfaceContainerHighest = Color(0xFFE7EBE7),

    outline = Neutral300,
    outlineVariant = Neutral200,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color(0xFF7F1D1D),

    inverseSurface = Neutral800,
    inverseOnSurface = Neutral100,
    inversePrimary = Emerald300,

    scrim = Color.Black,
    surfaceTint = Color.Transparent,
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Emerald900,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,

    secondary = Gold400,
    onSecondary = Gold900,
    secondaryContainer = Gold800,
    onSecondaryContainer = Gold100,

    tertiary = Emerald300,
    onTertiary = Emerald900,
    tertiaryContainer = Emerald800,
    onTertiaryContainer = Emerald100,

    background = SurfaceDark,
    onBackground = Neutral100,

    surface = SurfaceDark,
    onSurface = Neutral100,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Neutral400,

    surfaceContainerLowest = Color(0xFF0A0D0B),
    surfaceContainerLow = Color(0xFF121715),
    surfaceContainer = CardSurfaceDark,
    surfaceContainerHigh = Color(0xFF252B28),
    surfaceContainerHighest = Color(0xFF2F3633),

    outline = Neutral600,
    outlineVariant = Neutral700,

    error = ErrorRedDark,
    onError = Color(0xFF450A0A),
    errorContainer = ErrorRedContainerDark,
    onErrorContainer = Color(0xFFFECACA),

    inverseSurface = Neutral200,
    inverseOnSurface = Neutral800,
    inversePrimary = Emerald600,

    scrim = Color.Black,
    surfaceTint = Color.Transparent,
)

@Composable
fun CountDhikrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    animateBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CountDhikrTypography,
        ) {
            AuroraBackground(
                darkTheme = darkTheme,
                animateBackground = animateBackground
            ) {
                content()
            }
        }
    }
}
