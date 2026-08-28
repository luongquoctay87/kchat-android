package com.kchat.core.navigation.settings

import com.kchat.core.design.FontScale
import com.kchat.core.design.KChatAppearance
import com.kchat.core.design.ThemeMode
import com.kchat.core.model.UserSettings
import com.kchat.core.ui.screens.settings.DmPrivacyPolicy
import com.kchat.core.ui.screens.settings.MessageStorageSettings
import com.kchat.core.ui.screens.settings.QuietHours
import com.kchat.data.repository.UserSettingsPatch
import com.kchat.data.repository.UserSettingsPatch.Companion.CLEAR_DEFAULT_DISAPPEARING

fun UserSettings.toAppearance(): KChatAppearance = KChatAppearance(
    themeMode = when (theme) {
        "light" -> ThemeMode.Light
        "dark" -> ThemeMode.Dark
        else -> ThemeMode.System
    },
    fontScale = when (fontSize) {
        "small" -> FontScale.Small
        "large" -> FontScale.Large
        else -> FontScale.Medium
    },
    enterToSend = enterToSend,
)

fun KChatAppearance.toSettingsPatch(): UserSettingsPatch = UserSettingsPatch(
    theme = when (themeMode) {
        ThemeMode.Light -> "light"
        ThemeMode.Dark -> "dark"
        ThemeMode.System -> "system"
    },
    fontSize = when (fontScale) {
        FontScale.Small -> "small"
        FontScale.Large -> "large"
        FontScale.Medium -> "medium"
    },
    enterToSend = enterToSend,
)

fun UserSettings.toQuietHours(): QuietHours = QuietHours(
    enabled = quietHoursEnabled,
    startHour = (quietHoursStartHour ?: 22).coerceIn(0, 23),
    endHour = (quietHoursEndHour ?: 7).coerceIn(0, 23),
)

fun QuietHours.toSettingsPatch(): UserSettingsPatch = if (enabled) {
    UserSettingsPatch(
        quietHoursEnabled = true,
        quietHoursStartHour = startHour,
        quietHoursEndHour = endHour,
    )
} else {
    UserSettingsPatch(quietHoursEnabled = false)
}

fun UserSettings.toDmPrivacyPolicy(): DmPrivacyPolicy = when (privacyDm) {
    "none" -> DmPrivacyPolicy.Nobody
    "contacts" -> DmPrivacyPolicy.Contacts
    else -> DmPrivacyPolicy.Everyone
}

fun DmPrivacyPolicy.toPrivacyDmValue(): String = when (this) {
    DmPrivacyPolicy.Nobody -> "none"
    DmPrivacyPolicy.Contacts -> "contacts"
    DmPrivacyPolicy.Everyone -> "everyone"
}

fun UserSettings.toMessageStorage(): MessageStorageSettings = MessageStorageSettings(
    cacheRetentionDays = localCacheRetentionDays ?: 30,
    defaultDisappearingSeconds = defaultDisappearingSeconds,
)

fun MessageStorageSettings.toSettingsPatch(): UserSettingsPatch = UserSettingsPatch(
    localCacheRetentionDays = cacheRetentionDays,
    defaultDisappearingSeconds = defaultDisappearingSeconds ?: CLEAR_DEFAULT_DISAPPEARING,
)
