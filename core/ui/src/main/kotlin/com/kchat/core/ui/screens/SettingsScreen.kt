package com.kchat.core.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.ui.SettingsRow
import com.kchat.core.ui.components.KChatSettingsCard
import com.kchat.core.ui.components.UserAvatar

@Composable
fun SettingsScreen(
    username: String,
    displayName: String,
    avatarUrl: String? = null,
    modifier: Modifier = Modifier,
    onProfile: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onAppearance: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onStorage: () -> Unit = {},
    onDevices: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(KChatDimens.screenPadding),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            SettingsRow(
                label = when {
                    displayName.isNotBlank() -> displayName
                    username.isNotBlank() -> "@$username"
                    else -> "Hồ sơ"
                },
                subtitle = when {
                    displayName.isNotBlank() && username.isNotBlank() -> "@$username"
                    displayName.isBlank() && username.isBlank() -> "Đang tải…"
                    else -> null
                },
                onClick = onProfile,
                leading = {
                    UserAvatar(
                        name = displayName.ifBlank { username }.ifBlank { "?" },
                        size = KChatDimens.avatarSmall,
                        imageUrl = avatarUrl,
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        KChatSettingsCard {
            SettingsRow(label = "Thông báo", onClick = onNotifications)
            SettingsRow(label = "Giao diện", onClick = onAppearance)
            SettingsRow(label = "Quyền riêng tư", onClick = onPrivacy)
            SettingsRow(label = "Lưu trữ tin nhắn", onClick = onStorage)
            SettingsRow(label = "Thiết bị đăng nhập", onClick = onDevices, showDivider = false)
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Đăng xuất",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
