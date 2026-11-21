package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = LeafSecondary,
    onSecondary = LeafOnSecondary,
    secondaryContainer = LeafSecondaryContainer,
    onSecondaryContainer = LeafOnSecondaryContainer,
    tertiary = SkyTertiary,
    onTertiary = SkyOnTertiary,
    tertiaryContainer = SkyTertiaryContainer,
    onTertiaryContainer = SkyOnTertiaryContainer,
    background = CleanBackground,
    onBackground = DeepSlateInk,
    surface = CleanSurface,
    onSurface = DeepSlateInk,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = MutedSlateInk,
    outline = SubtleOutline,
    error = StatusUrgentFg,
    errorContainer = StatusUrgentBg,
    onError = TealOnPrimary,
    onErrorContainer = StatusUrgentFg
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = TealOnPrimaryContainer,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = TealPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = LeafOnSecondaryContainer,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = LeafSecondaryContainer,
    tertiary = SkyTertiaryContainer,
    onTertiary = SkyOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SubtleOutline
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TertemizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
