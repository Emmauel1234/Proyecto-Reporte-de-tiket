package com.example.apptiket.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Paleta de la app: azul marino + azul de acento. */
object TiketColors {
    val Navy = Color(0xFF0F172A)
    val NavyLight = Color(0xFF1E3A8A)
    val Blue = Color(0xFF2563EB)
    val BlueSoft = Color(0xFFEFF6FF)
    val BlueLight = Color(0xFF93C5FD)
    val Background = Color(0xFFF8FAFC)
    val Border = Color(0xFFE2E8F0)
    val TextMain = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val Slate = Color(0xFF64748B)
    val SlateSoft = Color(0xFFF1F5F9)
    val Red = Color(0xFFE11D48)
    val RedSoft = Color(0xFFFFF1F2)
    val Amber = Color(0xFFD97706)
    val AmberSoft = Color(0xFFFFFBEB)
    val Green = Color(0xFF059669)
    val GreenSoft = Color(0xFFECFDF5)
}

/**
 * Tema propio con colores fijos (sin colores dinámicos del sistema, que en Android 12+
 * cambiarían la paleta según el fondo de pantalla).
 */
@Composable
fun TiketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = TiketColors.Blue,
            onPrimary = Color.White,
            background = TiketColors.Background,
            onBackground = TiketColors.TextMain,
            surface = Color.White,
            onSurface = TiketColors.TextMain,
            onSurfaceVariant = TiketColors.TextMuted,
            outline = TiketColors.Border,
            secondaryContainer = TiketColors.BlueSoft,
            onSecondaryContainer = TiketColors.Blue
        ),
        typography = Typography,
        content = content
    )
}