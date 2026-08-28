package com.kchat.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal fun kChatTypography(fontScale: FontScale): Typography {
    val scale = when (fontScale) {
        FontScale.Small -> 0.9f
        FontScale.Medium -> 1f
        FontScale.Large -> 1.15f
    }

    fun scaled(size: Float) = (size * scale).sp

    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = scaled(57f),
            lineHeight = scaled(64f),
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaled(28f),
            lineHeight = scaled(36f),
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaled(24f),
            lineHeight = scaled(32f),
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = scaled(22f),
            lineHeight = scaled(28f),
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = scaled(16f),
            lineHeight = scaled(24f),
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = scaled(16f),
            lineHeight = scaled(24f),
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = scaled(14f),
            lineHeight = scaled(20f),
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = scaled(12f),
            lineHeight = scaled(16f),
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = scaled(14f),
            lineHeight = scaled(20f),
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = scaled(12f),
            lineHeight = scaled(16f),
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = scaled(11f),
            lineHeight = scaled(16f),
        ),
    )
}
