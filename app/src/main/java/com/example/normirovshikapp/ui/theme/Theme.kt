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
    primary = GazBlueDeep,
    onPrimary = GazWhite,
    secondary = GazBluePrimary,
    onSecondary = GazWhite,
    background = GazGrayBg,
    onBackground = Color(0xFF0F172A),
    surface = GazCardSurface,
    onSurface = GazBlueDeep,
    outline = GazSilver,

    secondaryContainer = GazBluePrimary,
    onSecondaryContainer = GazWhite,

    tertiary = GazBlueLight,
    onTertiary = GazWhite,
    tertiaryContainer = GazGrayBg,
    onTertiaryContainer = GazBlueDeep
)

private val DarkColorScheme = darkColorScheme(
    primary = GazBlueLight,
    onPrimary = GazWhite,
    secondary = GazSilver,
    onSecondary = GazBlueDeep,
    background = Color(0xFF0B0F19),
    onBackground = GazWhite,
    surface = Color(0xFF151D30),
    onSurface = GazWhite,
    outline = Color(0xFF1E293B),

    secondaryContainer = GazBlueLight,
    onSecondaryContainer = GazWhite,

    tertiary = GazBluePrimary,
    onTertiary = GazWhite,
    tertiaryContainer = Color(0xFF1E293B),
    onTertiaryContainer = GazWhite
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
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
