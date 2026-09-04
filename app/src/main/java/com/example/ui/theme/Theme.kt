package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGoldPrimary,
    onPrimary = IslamicEmeraldDark,
    primaryContainer = IslamicEmeraldSurfaceVariant,
    onPrimaryContainer = IslamicGoldLight,
    secondary = IslamicMintLight,
    onSecondary = IslamicEmeraldDark,
    secondaryContainer = IslamicEmeraldCard,
    onSecondaryContainer = IslamicMintLight,
    tertiary = IslamicGoldAccent,
    onTertiary = IslamicEmeraldDark,
    background = IslamicEmeraldDark,
    onBackground = IslamicTextPrimary,
    surface = IslamicEmeraldSurface,
    onSurface = IslamicTextPrimary,
    surfaceVariant = IslamicEmeraldSurfaceVariant,
    onSurfaceVariant = IslamicTextSecondary,
    outline = IslamicBorderGold,
    outlineVariant = IslamicBorderEmerald
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicLightPrimary,
    onPrimary = IslamicLightBg,
    primaryContainer = IslamicLightSurfaceVariant,
    onPrimaryContainer = IslamicLightPrimary,
    secondary = IslamicMintDark,
    onSecondary = IslamicLightBg,
    secondaryContainer = IslamicLightSurfaceVariant,
    onSecondaryContainer = IslamicMintDark,
    tertiary = IslamicGoldDark,
    onTertiary = IslamicLightBg,
    background = IslamicLightBg,
    onBackground = IslamicLightText,
    surface = IslamicLightSurface,
    onSurface = IslamicLightText,
    surfaceVariant = IslamicLightSurfaceVariant,
    onSurfaceVariant = IslamicLightTextMuted,
    outline = IslamicBorderGold,
    outlineVariant = IslamicBorderEmerald
)

@Composable
fun DailyWirdTheme(
    darkTheme: Boolean = true, // Default to rich Islamic dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
