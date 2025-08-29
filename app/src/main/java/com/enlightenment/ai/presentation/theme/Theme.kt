package com.enlightenment.ai.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = SoftRed,
    onPrimaryContainer = Color.White,
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = SkyBlue.copy(alpha = 0.3f),
    onSecondaryContainer = WoodBrown,
    tertiary = GrassGreen,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = WoodBrown,
    surface = Color.White,
    onSurface = WoodBrown,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun AIEnlightenmentTimeTheme(  // 可组合UI组件
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For kids app, we always use light 主题
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}