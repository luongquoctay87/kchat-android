package com.kchat.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class FontScale {
    Small,
    Medium,
    Large,
}

data class KChatAppearance(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontScale: FontScale = FontScale.Medium,
    val enterToSend: Boolean = true,
)

val KChatAppearanceSaver = listSaver(
    save = { appearance ->
        listOf(appearance.themeMode.ordinal, appearance.fontScale.ordinal, appearance.enterToSend)
    },
    restore = { saved ->
        KChatAppearance(
            themeMode = ThemeMode.entries[saved[0] as Int],
            fontScale = FontScale.entries[saved[1] as Int],
            enterToSend = saved[2] as Boolean,
        )
    },
)

private val LocalFontScale = staticCompositionLocalOf { FontScale.Medium }

private val KChatBlue = Color(0xFF1A73E8)
private val KChatBlueDark = Color(0xFF8AB4F8)

private val LightColors = lightColorScheme(
    primary = KChatBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF5F6368),
    onSecondary = Color.White,
    background = Color(0xFFF0F4F8),
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFF9AA0A6),
    outlineVariant = Color(0xFFE8EAED),
)

private val DarkColors = darkColorScheme(
    primary = KChatBlueDark,
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFF9AA0A6),
    background = Color(0xFF0E1114),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF1A1D21),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF2D3135),
    onSurfaceVariant = Color(0xFF9AA0A6),
    outline = Color(0xFF3C4043),
    outlineVariant = Color(0xFF2D3135),
)

object KChatColors {
    val chatBackground @Composable get() = MaterialTheme.colorScheme.background
    val bubbleSent @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val bubbleReceived @Composable get() = MaterialTheme.colorScheme.surface
    val online @Composable get() = Color(0xFF34A853)
    val unreadBadge @Composable get() = MaterialTheme.colorScheme.primary
    val callBackground @Composable get() = Color(0xFF1A1D21)
    val botAlert @Composable get() = MaterialTheme.colorScheme.errorContainer
}

@Composable
fun KChatTheme(
    appearance: KChatAppearance = KChatAppearance(),
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appearance.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val typography = kChatTypography(appearance.fontScale)

    CompositionLocalProvider(LocalFontScale provides appearance.fontScale) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = typography,
            shapes = KChatShapes,
            content = content,
        )
    }
}

val LocalKChatFontScale: FontScale
    @Composable get() = LocalFontScale.current
