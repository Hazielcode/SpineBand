package com.spineband.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // Colores principales - Navy + Cyan como acentos
    primary = SpineBandCyan,              // Turquesa para botones/acciones
    onPrimary = SpineBandWhite,           // Blanco sobre turquesa
    primaryContainer = SpineBandCyanLight,
    onPrimaryContainer = SpineBandNavy,

    secondary = SpineBandNavy,            // Navy para elementos secundarios
    onSecondary = SpineBandWhite,
    secondaryContainer = SpineBandNavyLight,
    onSecondaryContainer = SpineBandWhite,

    tertiary = SpineBandCyan,
    onTertiary = SpineBandWhite,

    // Estados
    error = SpineBandRed,
    onError = SpineBandWhite,

    // Fondos - MUCHO BLANCO
    background = SpineBandWhite,          // Blanco puro
    onBackground = SpineBandBlack,        // Negro suave para textos

    surface = SpineBandWhite,             // Blanco puro para cards
    onSurface = SpineBandBlack,
    surfaceVariant = SpineBandOffWhite,   // Blanco suave para variantes
    onSurfaceVariant = SpineBandDarkGray,

    // Bordes y divisores
    outline = SpineBandGray,
    outlineVariant = SpineBandLightGray,
)

private val DarkColorScheme = darkColorScheme(
    primary = SpineBandCyan,
    onPrimary = SpineBandNavy,
    primaryContainer = SpineBandNavy,
    onPrimaryContainer = SpineBandCyan,

    secondary = SpineBandNavy,
    onSecondary = SpineBandCyan,
    secondaryContainer = SpineBandNavyLight,
    onSecondaryContainer = SpineBandCyan,

    tertiary = SpineBandCyanLight,
    onTertiary = SpineBandNavy,

    error = SpineBandRed,
    onError = SpineBandWhite,

    background = Color(0xFF121212),
    onBackground = SpineBandWhite,

    surface = Color(0xFF1E1E1E),
    onSurface = SpineBandWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = SpineBandLightGray,

    outline = Color(0xFF3C3C3C),
    outlineVariant = Color(0xFF2C2C2C),
)

@Composable
fun SpineBandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar BLANCA en modo claro (moderno)
            window.statusBarColor = if (darkTheme) {
                colorScheme.primary.toArgb()
            } else {
                SpineBandWhite.toArgb()
            }
            // Iconos oscuros en modo claro (contraste con blanco)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}