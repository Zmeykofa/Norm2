package com.example.normirovshikapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColorScheme = lightColorScheme(
    primary = GazBlue,
    onPrimary = GazWhite,
    secondary = GazBlueLight,
    onSecondary = GazWhite,
    background = GazWhite,
    onBackground = Color(0xFF1C1C1C),
    surface = GazGrayLight,
    onSurface = GazBlue,
    outline = GazSilver,

    secondaryContainer = GazBlue,
    onSecondaryContainer = GazWhite,

    // ⚡ Добавляем, чтобы убрать остатки фиолетового
    tertiary = GazBlueLight,
    onTertiary = GazWhite,
    tertiaryContainer = GazGrayLight,
    onTertiaryContainer = GazBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = GazBlueLight,
    onPrimary = GazWhite,
    secondary = GazSilver,
    onSecondary = GazBlue,
    background = Color(0xFF121212),
    onBackground = GazWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = GazWhite,
    outline = GazGrayLight,

    secondaryContainer = GazBlueLight,
    onSecondaryContainer = GazWhite,

    tertiary = GazBlue,
    onTertiary = GazWhite,
    tertiaryContainer = Color(0xFF2A2A2A),
    onTertiaryContainer = GazWhite
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun NormirovshikAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // важно: отключаем системные палитры
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
