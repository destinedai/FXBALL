package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FxBounceColorScheme = darkColorScheme(
    primary = CandleBullish,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00381B),
    onPrimaryContainer = Color(0xFF76FFB4),
    secondary = EntryLineColor,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D2866),
    onSecondaryContainer = Color(0xFFB0D0FF),
    tertiary = TakeProfitColor,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4A3B00),
    onTertiaryContainer = Color(0xFFFFEA75),
    error = CandleBearish,
    onError = Color.White,
    background = ChartBackground,
    onBackground = TextPrimary,
    surface = ChartSurface,
    onSurface = TextPrimary,
    surfaceVariant = ChartSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ChartGrid,
    outlineVariant = ChartGridSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always preserve the signature TradingView Dark look
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FxBounceColorScheme,
        typography = Typography,
        content = content
    )
}
