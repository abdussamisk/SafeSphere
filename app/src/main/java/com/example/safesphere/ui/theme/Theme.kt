package com.example.safesphere.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Colors
// ---------------------------------------------------------------------------

// Base surfaces
val Cream = Color(0xFFFAF8F6)          // app background
val Surface = Color(0xFFFFFFFF)        // cards
val SurfaceMuted = Color(0xFFF0EBE5)   // muted chips / avatar bg
val Border = Color(0xFFE8E2DD)         // card / input borders

// Text
val TextPrimary = Color(0xFF2A2725)
val TextSecondary = Color(0xFF5C5653)
val TextMuted = Color(0xFF968F8C)

// Brand green (used for "active" / success states across the main app)
val SageActive = Color(0xFF6D8A74)
val SageText = Color(0xFF425C48)
val SageChipBg = Color(0xFFE3ECE1)

// Premium auth accent (deeper, richer version of the brand green)
val AccentDeep = Color(0xFF2D4A3A)
val AccentSoft = Color(0xFFD8E3D3)

// Danger / emergency (Live Sharing screen)
val DangerRed = Color(0xFFD9694A)
val DangerBg = Color(0xFFFBE0D8)
val DangerText = Color(0xFFC1503A)

// Dark CTA used across the calm main-app surfaces
val InkButton = Color(0xFF2A2725)

// ---------------------------------------------------------------------------
// Typography
// ---------------------------------------------------------------------------

/**
 * The design uses Inter throughout (Regular / Medium / Semi Bold / Bold / Extra Bold).
 * Drop the Inter .ttf files into res/font/ (inter_regular.ttf, inter_medium.ttf,
 * inter_semibold.ttf, inter_bold.ttf, inter_extrabold.ttf) and uncomment the Font() entries
 * below. Until then this falls back to the platform default sans-serif so the code compiles
 * out of the box.
 */
val InterFontFamily = FontFamily.Default
// Example once font files are added:
// val InterFontFamily = FontFamily(
//     Font(R.font.inter_regular, FontWeight.Normal),
//     Font(R.font.inter_medium, FontWeight.Medium),
//     Font(R.font.inter_semibold, FontWeight.SemiBold),
//     Font(R.font.inter_bold, FontWeight.Bold),
//     Font(R.font.inter_extrabold, FontWeight.ExtraBold),
// )

val SafeSphereTypography = Typography(
    // Screen titles (e.g. "Contacts", "Settings", 28px Extra Bold)
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
    ),
    // Auth hero titles ("Welcome back", "Create your account", 30px Extra Bold)
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
    ),
    // Uppercase section eyebrows ("QUICK ACTIONS", "PRIVACY & SHARING")
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
    ),
)

// ---------------------------------------------------------------------------
// Shapes
// ---------------------------------------------------------------------------

val SafeSphereShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Extra one-off radii used by pill buttons / cards / inputs across the app
val PillShape = RoundedCornerShape(50)
val CardShape = RoundedCornerShape(16.dp)
val CardShapeLarge = RoundedCornerShape(20.dp)
val InputShape = RoundedCornerShape(16.dp)

// ---------------------------------------------------------------------------
// Theme
// ---------------------------------------------------------------------------

private val SafeSphereColorScheme = lightColorScheme(
    primary = InkButton,
    onPrimary = Surface,
    secondary = SageActive,
    onSecondary = Surface,
    tertiary = AccentDeep,
    background = Cream,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    error = DangerRed,
    onError = Surface,
    errorContainer = DangerBg,
    onErrorContainer = DangerText,
)

@Composable
fun SafeSphereTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SafeSphereColorScheme,
        typography = SafeSphereTypography,
        shapes = SafeSphereShapes,
        content = content,
    )
}
