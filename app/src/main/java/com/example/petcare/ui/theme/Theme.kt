package com.example.petcare.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightGreenIcon,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E3B2D),
    onPrimaryContainer = Color(0xFFBBEAD2),
    secondary = GreenLight,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = Color(0xFF161817),
    surfaceContainerLow = SurfaceDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF282D2A),
    error = RedError,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenPrimary,
    secondary = GreenLight,
    onSecondary = TextPrimary,
    secondaryContainer = GreenLight,
    onSecondaryContainer = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F4EE),
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFDF9),
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = RedError,
    onError = Color.White,
)

@Composable
fun PetCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
