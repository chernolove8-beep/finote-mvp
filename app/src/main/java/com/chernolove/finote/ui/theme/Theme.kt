package com.chernolove.finote.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FinoteGold,
    onPrimary = FinoteNavyDark,
    secondary = FinoteGoldSoft,
    onSecondary = FinoteNavyDark,
    tertiary = FinoteNavyLight,
    background = FinoteNavyDark,
    onBackground = FinoteDarkText,
    surface = FinoteDarkSurface,
    onSurface = FinoteDarkText,
    surfaceVariant = FinoteDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB8C7D5),
    primaryContainer = Color(0xFF27435C),
    onPrimaryContainer = FinoteDarkText,
    outline = Color(0xFF55697C)
)

private val LightColorScheme = lightColorScheme(
    primary = FinoteNavy,
    onPrimary = Color.White,
    secondary = FinoteGold,
    onSecondary = FinoteNavyDark,
    tertiary = FinoteSuccess,
    background = FinoteCream,
    onBackground = FinoteInk,
    surface = FinotePaper,
    onSurface = FinoteInk,
    surfaceVariant = FinoteMist,
    onSurfaceVariant = FinoteSlate,
    primaryContainer = FinoteNavyLight,
    onPrimaryContainer = FinoteNavyDark,
    secondaryContainer = FinoteGoldSoft,
    onSecondaryContainer = FinoteNavyDark,
    outline = Color(0xFFD2C7B6)
)

@Composable
fun FinoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Отключаем dynamic color, чтобы на защите приложение выглядело одинаково
    // и сохраняло свой банковский стиль на любом устройстве.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
