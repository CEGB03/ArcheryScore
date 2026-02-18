package com.cegb03.archeryscore.ui.theme

import android.app.Activity
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
    primary = ArcheryGreenLight,
    onPrimary = Color(0xFF003300),
    primaryContainer = ArcheryGreenDark,
    onPrimaryContainer = Color(0xFFE8F5E9),
    
    secondary = ArcheryBlueLight,
    onSecondary = Color(0xFF001A33),
    secondaryContainer = ArcheryBlueDark,
    onSecondaryContainer = Color(0xFFE3F2FD),
    
    tertiary = ArcheryOrangeLight,
    onTertiary = Color(0xFF331A00),
    tertiaryContainer = ArcheryOrangeDark,
    onTertiaryContainer = Color(0xFFFFE0B2),
    
    background = DarkBackground,
    onBackground = Color(0xFFE3F2FD),
    surface = DarkSurface,
    onSurface = Color(0xFFE3F2FD),
    surfaceVariant = Color(0xFF3949AB),
    onSurfaceVariant = Color(0xFFE8EAF6)
)

private val LightColorScheme = lightColorScheme(
    primary = ArcheryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    
    secondary = ArcheryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF0D47A1),
    
    tertiary = ArcheryOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFE65100),
    
    background = LightBackground,
    onBackground = Color(0xFF263238),  // Gris azulado más suave
    surface = LightSurface,
    onSurface = Color(0xFF263238),     // Gris azulado más suave
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF455A64),
    
    outline = Color(0xFF90A4AE),
    outlineVariant = Color(0xFFCFD8DC)
)

@Composable
fun ArcheryScoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,  // Desactivado para usar nuestros colores personalizados
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