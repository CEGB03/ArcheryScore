package com.cegb03.archeryscore.ui.theme

import androidx.compose.ui.graphics.Color

// Colores principales - Paleta más vibrante inspirada en el tiro con arco
val ArcheryGreen = Color(0xFF4CAF50)        // Verde vibrante (target)
val ArcheryGreenLight = Color(0xFF81C784)   // Verde claro
val ArcheryGreenDark = Color(0xFF388E3C)    // Verde oscuro

val ArcheryBlue = Color(0xFF2196F3)         // Azul cielo
val ArcheryBlueLight = Color(0xFF64B5F6)    // Azul claro
val ArcheryBlueDark = Color(0xFF1976D2)     // Azul oscuro

val ArcheryOrange = Color(0xFFFF9800)       // Naranja (energía)
val ArcheryOrangeLight = Color(0xFFFFB74D)  // Naranja claro
val ArcheryOrangeDark = Color(0xFFF57C00)   // Naranja oscuro

val ArcheryYellow = Color(0xFFFFEB3B)       // Amarillo (centro del target)
val ArcheryRed = Color(0xFFF44336)          // Rojo (anillo del target)

// Colores de fondo y superficie
val LightBackground = Color(0xFFF5F9FF)     // Fondo azul muy claro
val LightSurface = Color(0xFFFFFFFF)        // Blanco puro
val LightSurfaceVariant = Color(0xFFE3F2FD) // Azul muy suave

// Para modo oscuro
val DarkBackground = Color(0xFF1A237E)      // Azul oscuro
val DarkSurface = Color(0xFF283593)         // Azul medio oscuro

// Colores legacy (mantener para compatibilidad)
val Purple80 = ArcheryGreenLight
val PurpleGrey80 = ArcheryBlueLight
val Pink80 = ArcheryOrangeLight

val Purple40 = ArcheryGreen
val PurpleGrey40 = ArcheryBlue
val Pink40 = ArcheryOrange

val RosaClaro = Color(0xFFFFC0CB)
val RosaClaroTransparente = RosaClaro.copy(alpha = 0.4f)
val RosaClaroSemi = RosaClaro.copy(alpha = 0.8f)
val RosaClaroSemi2 = RosaClaro.copy(alpha = 0.5f)

val Fondo = LightBackground
val TextoPrincipal = Color(0xFF263238)  // Gris azulado más suave
val RosaOscuro = ArcheryRed

// Colores para los puntajes de tiro con arco según el target
val ScoreYellow = Color(0xFFFFEB3B)  // X, 11, 10, 9
val ScoreRed = Color(0xFFF44336)     // 8, 7
val ScoreBlue = Color(0xFF2196F3)    // 6, 5
val ScoreBlack = Color(0xFF000000)   // 4, 3, M
val ScoreWhite = Color(0xFFFFFFFF)   // 2, 1

// Función auxiliar para obtener el color del puntaje según el target de tiro con arco
fun getScoreColor(score: String): Color {
    return when (score) {
        "X", "11", "10", "9" -> ScoreYellow
        "8", "7" -> ScoreRed
        "6", "5" -> ScoreBlue
        "4", "3" -> ScoreBlack
        "2", "1" -> ScoreWhite
        "M" -> ScoreBlack
        else -> Color(0xFF9E9E9E) // Gris por defecto
    }
}

// Función auxiliar para obtener el color del texto según el fondo
fun getScoreTextColor(score: String): Color {
    return when (score) {
        "2", "1" -> Color.Black // Texto negro para fondo blanco
        else -> Color.White // Texto blanco para el resto
    }
}