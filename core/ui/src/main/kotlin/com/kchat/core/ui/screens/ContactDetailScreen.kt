package com.kchat.core.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.ContactSummary
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.SettingsRow
import com.kchat.core.ui.components.KChatPrimaryButton
import com.kchat.core.ui.components.KChatSettingsCard
import com.kchat.core.ui.components.UserAvatar

@Composable
fun ContactDetailScreen(
    contact: ContactSummary,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var confirmRemove by remember { mutableStateOf(false) }
    val statusText = contactPresenceLabel(contact)
    val statusColor = if (contact.isOnline) {
        KChatColors.online
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    if (confirmRemove && onRemove != null) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Xóa khỏi danh bạ?") },
            text = {
                Text("Gỡ ${contact.name} khỏi danh bạ của bạn. Bạn vẫn có thể tìm và chat lại sau.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemove = false
                        onRemove()
                    },
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) {
                    Text("Hủy")
                }
            },
        )
    }

    KChatDetailScaffold(
        modifier = modifier,
        title = "Thông tin chi tiết",
        onBack = onBack,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = KChatDimens.screenPadding)
                        .padding(top = 8.dp, bottom = 12.dp),
                ) {
                    KChatPrimaryButton(
                        text = "Nhắn tin",
                        onClick = onMessage,
                    )
                    if (onRemove != null) {
                        TextButton(
                            onClick = { confirmRemove = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        ) {
                            Text(
                                text = "Xoá khỏi danh bạ",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KChatDimens.screenPadding)
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserAvatar(
                name = contact.name.ifBlank { "?" },
                size = KChatDimens.avatarProfile,
                imageUrl = contact.avatarUrl,
                isOnline = contact.isOnline,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Thông tin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Tên hiển thị",
                    subtitle = contact.name.ifBlank { "—" },
                )
                SettingsRow(
                    label = "Tên đăng nhập",
                    subtitle = contact.username.ifBlank { "—" }.let { value ->
                        if (value == "—" || value.startsWith("@")) value else "@$value"
                    },
                )
                SettingsRow(
                    label = "Email",
                    subtitle = contact.email.ifBlank { "—" },
                )
                SettingsRow(
                    label = "Số điện thoại",
                    subtitle = contact.phone.ifBlank { "—" },
                )
                SettingsRow(
                    label = "Trạng thái",
                    subtitle = statusText,
                    showDivider = false,
                )
            }
        }
    }
}

internal fun contactPresenceLabel(contact: ContactSummary): String {
    if (contact.isOnline) return "Đang trực tuyến"
    val raw = contact.subtitle.trim()
    if (raw.isBlank()) return "Ngoại tuyến"
    return when (raw.lowercase()) {
        "online", "trực tuyến", "đang trực tuyến" -> "Đang trực tuyến"
        "offline", "ngoại tuyến" -> "Ngoại tuyến"
        else -> raw
    }
}
