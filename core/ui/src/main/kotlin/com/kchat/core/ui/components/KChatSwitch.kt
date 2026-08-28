package com.kchat.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * App switch with readable off-state contrast.
 * Default M3 uses outline thumb on surfaceVariant track — nearly invisible in our light palette.
 */
@Composable
fun KChatSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = scheme.onPrimary,
            checkedTrackColor = scheme.primary,
            checkedBorderColor = scheme.primary,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = scheme.onSurface.copy(alpha = 0.28f),
            uncheckedBorderColor = Color.Transparent,
            uncheckedIconColor = scheme.onSurfaceVariant,
            disabledCheckedThumbColor = scheme.onPrimary.copy(alpha = 0.6f),
            disabledCheckedTrackColor = scheme.primary.copy(alpha = 0.38f),
            disabledUncheckedThumbColor = Color.White.copy(alpha = 0.7f),
            disabledUncheckedTrackColor = scheme.onSurface.copy(alpha = 0.12f),
        ),
    )
}
