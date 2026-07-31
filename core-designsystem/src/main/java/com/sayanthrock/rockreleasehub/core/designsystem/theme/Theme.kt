package com.sayanthrock.rockreleasehub.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sayanthrock.rockreleasehub.core.model.AppTheme
import com.sayanthrock.rockreleasehub.core.model.ThemeMode

private val RockObsidianDarkColorScheme = darkColorScheme(
    primary = RockObsidianDarkPrimary,
    secondary = RockObsidianDarkSecondary,
    background = RockObsidianDarkBackground,
    surface = RockObsidianDarkSurface,
    onBackground = RockObsidianDarkOnBackground,
    onSurface = RockObsidianDarkOnBackground
)

private val RockObsidianLightColorScheme = lightColorScheme(
    primary = RockObsidianLightPrimary,
    secondary = RockObsidianLightSecondary,
    background = RockObsidianLightBackground,
    surface = RockObsidianLightSurface,
    onBackground = RockObsidianLightOnBackground,
    onSurface = RockObsidianLightOnBackground
)

private val AmoledBlackDarkColorScheme = darkColorScheme(
    primary = AmoledBlackDarkPrimary,
    secondary = AmoledBlackDarkSecondary,
    background = AmoledBlackDarkBackground,
    surface = AmoledBlackDarkSurface,
    onBackground = AmoledBlackDarkOnBackground,
    onSurface = AmoledBlackDarkOnBackground
)

private val AmoledBlackLightColorScheme = lightColorScheme(
    primary = AmoledBlackLightPrimary,
    secondary = AmoledBlackLightSecondary,
    background = AmoledBlackLightBackground,
    surface = AmoledBlackLightSurface,
    onBackground = AmoledBlackLightOnBackground,
    onSurface = AmoledBlackLightOnBackground
)

private val ArcticLightDarkColorScheme = darkColorScheme(
    primary = ArcticLightDarkPrimary,
    secondary = ArcticLightDarkSecondary,
    background = ArcticLightDarkBackground,
    surface = ArcticLightDarkSurface,
    onBackground = ArcticLightDarkOnBackground,
    onSurface = ArcticLightDarkOnBackground
)

private val ArcticLightLightColorScheme = lightColorScheme(
    primary = ArcticLightLightPrimary,
    secondary = ArcticLightLightSecondary,
    background = ArcticLightLightBackground,
    surface = ArcticLightLightSurface,
    onBackground = ArcticLightLightOnBackground,
    onSurface = ArcticLightLightOnBackground
)

private val LiquidGraphiteDarkColorScheme = darkColorScheme(
    primary = LiquidGraphiteDarkPrimary,
    secondary = LiquidGraphiteDarkSecondary,
    background = LiquidGraphiteDarkBackground,
    surface = LiquidGraphiteDarkSurface,
    onBackground = LiquidGraphiteDarkOnBackground,
    onSurface = LiquidGraphiteDarkOnBackground
)

private val LiquidGraphiteLightColorScheme = lightColorScheme(
    primary = LiquidGraphiteLightPrimary,
    secondary = LiquidGraphiteLightSecondary,
    background = LiquidGraphiteLightBackground,
    surface = LiquidGraphiteLightSurface,
    onBackground = LiquidGraphiteLightOnBackground,
    onSurface = LiquidGraphiteLightOnBackground
)

private val TerminalGreenDarkColorScheme = darkColorScheme(
    primary = TerminalGreenDarkPrimary,
    secondary = TerminalGreenDarkSecondary,
    background = TerminalGreenDarkBackground,
    surface = TerminalGreenDarkSurface,
    onBackground = TerminalGreenDarkOnBackground,
    onSurface = TerminalGreenDarkOnBackground
)

private val TerminalGreenLightColorScheme = lightColorScheme(
    primary = TerminalGreenLightPrimary,
    secondary = TerminalGreenLightSecondary,
    background = TerminalGreenLightBackground,
    surface = TerminalGreenLightSurface,
    onBackground = TerminalGreenLightOnBackground,
    onSurface = TerminalGreenLightOnBackground
)

private val SunsetAlloyDarkColorScheme = darkColorScheme(
    primary = SunsetAlloyDarkPrimary,
    secondary = SunsetAlloyDarkSecondary,
    background = SunsetAlloyDarkBackground,
    surface = SunsetAlloyDarkSurface,
    onBackground = SunsetAlloyDarkOnBackground,
    onSurface = SunsetAlloyDarkOnBackground
)

private val SunsetAlloyLightColorScheme = lightColorScheme(
    primary = SunsetAlloyLightPrimary,
    secondary = SunsetAlloyLightSecondary,
    background = SunsetAlloyLightBackground,
    surface = SunsetAlloyLightSurface,
    onBackground = SunsetAlloyLightOnBackground,
    onSurface = SunsetAlloyLightOnBackground
)

private fun getAppColorScheme(appTheme: AppTheme, isDark: Boolean): ColorScheme {
    return when (appTheme) {
        AppTheme.ROCK_OBSIDIAN -> if (isDark) RockObsidianDarkColorScheme else RockObsidianLightColorScheme
        AppTheme.AMOLED_BLACK -> if (isDark) AmoledBlackDarkColorScheme else AmoledBlackLightColorScheme
        AppTheme.ARCTIC_LIGHT -> if (isDark) ArcticLightDarkColorScheme else ArcticLightLightColorScheme
        AppTheme.LIQUID_GRAPHITE -> if (isDark) LiquidGraphiteDarkColorScheme else LiquidGraphiteLightColorScheme
        AppTheme.TERMINAL_GREEN -> if (isDark) TerminalGreenDarkColorScheme else TerminalGreenLightColorScheme
        AppTheme.SUNSET_ALLOY -> if (isDark) SunsetAlloyDarkColorScheme else SunsetAlloyLightColorScheme
    }
}

@Composable
fun RockReleaseHubTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    appTheme: AppTheme = AppTheme.ROCK_OBSIDIAN,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getAppColorScheme(appTheme, darkTheme)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
