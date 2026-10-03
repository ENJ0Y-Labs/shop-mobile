package com.enjoy.shopmobile.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val ShopColors = darkColorScheme(
    primary = Color(0xFFFF7A00), onPrimary = Color(0xFF111111),
    primaryContainer = Color(0xFF5C2D00), onPrimaryContainer = Color(0xFFFFDBBF),
    background = Color(0xFF090909), onBackground = Color(0xFFF7F7F5),
    surface = Color(0xFF111111), onSurface = Color(0xFFF7F7F5),
    surfaceVariant = Color(0xFF181818), onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF292929), error = Color(0xFFFF7474),
    errorContainer = Color(0xFF3A1717), onErrorContainer = Color(0xFFFFDAD6)
)
private val ShopTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}
@Composable fun ShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ShopColors, typography = ShopTypography, content = content)
}