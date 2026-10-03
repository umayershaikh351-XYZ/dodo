package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CipherLockColorScheme = darkColorScheme(
    primary = MatrixGreen,
    onPrimary = BackgroundPrimary,
    primaryContainer = BackgroundSurface,
    onPrimaryContainer = MatrixGreen,
    secondary = MatrixGreenDim,
    onSecondary = TextPrimary,
    secondaryContainer = BackgroundElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = StatusInfo,
    onTertiary = BackgroundPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = GridLine,
    error = StatusFail,
    onError = BackgroundPrimary
)

@Composable
fun CipherLockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CipherLockColorScheme,
        typography = CipherLockTypography,
        content = content
    )
}

// Keep alias for compatibility with template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CipherLockTheme(content = content)
}
