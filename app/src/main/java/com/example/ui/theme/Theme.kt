package com.example.ui.theme

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

private val PinterestColorScheme = lightColorScheme(
    primary = PinterestRed,
    onPrimary = PinterestWhite,
    primaryContainer = PinterestLightGrey,
    onPrimaryContainer = PinterestCharcoal,
    secondary = PinterestCharcoal,
    onSecondary = PinterestWhite,
    secondaryContainer = PinterestGrayBg,
    onSecondaryContainer = PinterestSubtitles,
    background = PinterestWhite,
    onBackground = PinterestCharcoal,
    surface = PinterestWhite,
    onSurface = PinterestCharcoal,
    surfaceVariant = PinterestGrayBg,
    onSurfaceVariant = PinterestSubtitles,
    outline = PinterestOutline,
    error = PinterestRed,
    onError = PinterestWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = PinterestColorScheme,
    typography = Typography,
    content = content
  )
}
