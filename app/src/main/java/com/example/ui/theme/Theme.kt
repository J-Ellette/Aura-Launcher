package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraBlue,
    onPrimary = Color.White,
    secondary = AuraTeal,
    onSecondary = Color.Black,
    tertiary = AuraIndigo,
    background = MidnightBackdrop,
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF141A26),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E2636),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0x3DFFFFFF)
)

private val AuraLightColorScheme = lightColorScheme(
    primary = AuraBlueLight,
    onPrimary = Color.White,
    secondary = AuraTeal,
    onSecondary = Color.White,
    tertiary = AuraIndigo,
    background = Color(0xFFF2F5FA),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6ECF5),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0x2E0F172A)
)

val AuraShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun AuraLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AuraDarkColorScheme
        else -> AuraLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
