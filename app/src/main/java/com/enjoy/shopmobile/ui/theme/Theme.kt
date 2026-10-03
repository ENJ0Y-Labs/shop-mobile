package com.enjoy.shopmobile.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ShopColors = darkColorScheme(
    primary = Color(0xFFFF7A00),
    onPrimary = Color(0xFF111111),
    primaryContainer = Color(0xFF5C2D00),
    onPrimaryContainer = Color(0xFFFFDBBF),
    secondary = Color(0xFFFF9A3D),
    onSecondary = Color(0xFF111111),
    background = Color(0xFF090909),
    onBackground = Color(0xFFF7F7F5),
    surface = Color(0xFF111111),
    onSurface = Color(0xFFF7F7F5),
    surfaceVariant = Color(0xFF181818),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF292929),
    outlineVariant = Color(0xFF202020),
    error = Color(0xFFFF7474),
    errorContainer = Color(0xFF3A1717),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val ShopTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold)
    )
}

private val ShopShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@Composable
fun ShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShopColors,
        typography = ShopTypography,
        shapes = ShopShapes
    ) {
        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = ShopColors.background,
            contentColor = ShopColors.onBackground,
            content = content
        )
    }
}

object ShopThemeDefaults {
    @Composable
    fun buttonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

    @Composable
    fun cardColors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    @Composable
    fun textFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        cursorColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface
    )
}
