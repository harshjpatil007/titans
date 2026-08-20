package com.example.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GeoGreenPrimary,
    onPrimary = GeoSurfaceLight,
    primaryContainer = GeoGreenContainer,
    onPrimaryContainer = GeoGreenDark,
    secondary = GeoRedCritical,
    onSecondary = GeoSurfaceLight,
    secondaryContainer = GeoRedContainer,
    onSecondaryContainer = GeoRedText,
    tertiary = GeoDarkCard,
    onTertiary = GeoDarkText,
    background = GeoBackground,
    onBackground = GeoTextPrimary,
    surface = GeoSurfaceLight,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoSurfaceMuted,
    onSurfaceVariant = GeoTextSecondary,
    outline = GeoBorder,
    outlineVariant = GeoBorder,
    error = GeoRedCritical,
    onError = GeoSurfaceLight
)

val GeometricShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun RakshAITheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var ctx = view.context
            while (ctx is ContextWrapper) {
                if (ctx is Activity) {
                    val window = ctx.window
                    window.statusBarColor = GeoBackground.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                    break
                }
                ctx = ctx.baseContext
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = GeometricShapes,
        content = content
    )
}
