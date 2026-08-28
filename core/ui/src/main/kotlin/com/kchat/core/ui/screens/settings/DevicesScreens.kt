package com.kchat.core.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.DeviceSession
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.components.KChatSettingsCard

@Composable
fun DevicesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    devices: List<DeviceSession> = emptyList(),
    isLoading: Boolean = false,
    onRevokeDevice: (String) -> Unit = {},
) {
    KChatDetailScaffold(
        modifier = modifier,
        title = "Thiết bị đăng nhập",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text(
                text = "Quản lý các thiết bị đang đăng nhập tài khoản của bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                isLoading && devices.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        CircularProgressIndicator()
                    }
                }
                devices.isEmpty() -> {
                    Text(
                        text = "Chưa có thiết bị nào được ghi nhận.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    KChatSettingsCard {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(devices, key = { it.id }) { device ->
                                DeviceRow(
                                    device = device,
                                    onRevoke = { onRevokeDevice(device.id) },
                                )
                                if (device != devices.last()) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đăng xuất thiết bị khác sẽ kết thúc phiên trên máy đó.\nThiết bị hiện tại không thể đăng xuất từ đây.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeviceRow(
    device: DeviceSession,
    onRevoke: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = device.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (device.isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (!device.isCurrent) {
            OutlinedButton(onClick = onRevoke) {
                Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
