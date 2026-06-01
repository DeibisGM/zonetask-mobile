package com.app.zonetask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = AppPrimary,
    background       = AppBackground,
    surface          = AppSurface,
    error            = AppError,
    onPrimary        = AppOnPrimary,
    onBackground     = AppOnSurface,
    onSurface        = AppOnSurface,
    onSurfaceVariant = AppSecondaryText,
    outline          = AppBorder
)

@Composable
fun ZoneTaskTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
