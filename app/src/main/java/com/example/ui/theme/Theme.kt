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

private val DarkColorScheme =
  darkColorScheme(
    primary = MineGreenPrimary,
    onPrimary = MineDarkBackground,
    primaryContainer = MineAccentDark,
    onPrimaryContainer = MineWhite,
    secondary = MineGreenAccent,
    onSecondary = MineDarkBackground,
    background = MineDarkBackground,
    surface = MineCardBackground,
    onBackground = MineWhite,
    onSurface = MineWhite,
    error = MineRed,
    onError = MineWhite
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  // Force custom colors for consistent theme design
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
