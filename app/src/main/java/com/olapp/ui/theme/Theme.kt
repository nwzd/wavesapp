package com.olapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Brand palette — ocean blue + vibe violet
val Brand       = Color(0xFF0EA5E9)   // Sky blue — main actions
val BrandDeep   = Color(0xFF0284C7)
val BrandSoft   = Color(0xFFE0F2FE)
val Tangerine   = Color(0xFFA855F7)   // Violet — gradient end / vibe accent
val Indigo      = Color(0xFF6366F1)   // Wave-sent indicator
val IndigoSoft  = Color(0xFFEEF2FF)

// Logo gradient
val LogoBlue    = Color(0xFF0EA5E9)
val LogoPurple  = Color(0xFFA855F7)
val LogoGradient = listOf(LogoBlue, LogoPurple)

// Neutrals
val Ink         = Color(0xFF0F172A)
val InkMid      = Color(0xFF475569)
val InkLight    = Color(0xFF94A3B8)
val Surface     = Color(0xFFFFFFFF)
val Background  = Color(0xFFF0F9FF)   // sky-50, barely-there blue
val Card        = Color(0xFFFFFFFF)
val Divider     = Color(0xFFE2E8F0)

// Avatar gradient palettes
val Grad0 = listOf(Brand, Tangerine)
val Grad1 = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
val Grad2 = listOf(Color(0xFFF97316), Color(0xFFEF4444))
val Grad3 = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
val Grad4 = listOf(Color(0xFFF59E0B), Color(0xFF10B981))
val AvatarGradients = listOf(Grad0, Grad1, Grad2, Grad3, Grad4)

fun avatarGradient(name: String): List<Color> =
    AvatarGradients[(name.hashCode() and 0x7fffffff) % AvatarGradients.size]

private val AppColors = lightColorScheme(
    primary              = Brand,
    onPrimary            = Color.White,
    primaryContainer     = BrandSoft,
    onPrimaryContainer   = BrandDeep,
    secondary            = Indigo,
    onSecondary          = Color.White,
    secondaryContainer   = IndigoSoft,
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary             = Tangerine,
    onTertiary           = Color.White,
    background           = Background,
    onBackground         = Ink,
    surface              = Surface,
    onSurface            = Ink,
    surfaceVariant       = Color(0xFFF1F5F9),
    onSurfaceVariant     = InkMid,
    outline              = Divider,
    outlineVariant       = Color(0xFFF1F5F9),
    error                = Color(0xFFDC2626),
    onError              = Color.White,
)

private val AppTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Black,    fontSize = 34.sp, letterSpacing = (-1.0).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 26.sp, letterSpacing = (-0.5).sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 20.sp, letterSpacing = (-0.3).sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = (-0.1).sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 13.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, letterSpacing = 0.2.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 10.sp, letterSpacing = 0.3.sp),
)

@Composable
fun OlaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = AppTypography, content = content)
}
