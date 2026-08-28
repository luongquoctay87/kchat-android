package com.kchat.core.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.components.KChatSettingsCard
import com.kchat.core.ui.components.RadioOptionRow

/** Sentinel sent to API to clear default disappearing (stored as NULL). */
const val CLEAR_DEFAULT_DISAPPEARING = 0

enum class CacheRetentionOption(val days: Int, val label: String) {
    Week(7, "7 ngày"),
    Month(30, "30 ngày"),
    Quarter(90, "90 ngày"),
}

enum class DefaultDisappearingOption(val seconds: Int, val label: String) {
    Off(CLEAR_DEFAULT_DISAPPEARING, "Tắt"),
    Day1(86_400, "24 giờ"),
    Week(604_800, "7 ngày"),
    Month(2_592_000, "30 ngày"),
    Quarter(7_776_000, "90 ngày"),
}

data class MessageStorageSettings(
    val cacheRetentionDays: Int = 30,
    val defaultDisappearingSeconds: Int? = null,
)

fun MessageStorageSettings.cacheLabel(): String =
    CacheRetentionOption.entries.firstOrNull { it.days == cacheRetentionDays }?.label
        ?: "${cacheRetentionDays} ngày"

fun MessageStorageSettings.disappearingLabel(): String =
    DefaultDisappearingOption.entries.firstOrNull { it.seconds == (defaultDisappearingSeconds ?: CLEAR_DEFAULT_DISAPPEARING) }?.label
        ?: "Tắt"

@Composable
fun StorageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    storage: MessageStorageSettings = MessageStorageSettings(),
    onStorageChange: (MessageStorageSettings) -> Unit = {},
) {
    var current by remember { mutableStateOf(storage) }

    fun commit(updated: MessageStorageSettings) {
        current = updated
        onStorageChange(updated)
    }

    KChatDetailScaffold(
        modifier = modifier,
        title = "Lưu trữ tin nhắn",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text(
                text = "Quản lý bộ nhớ đệm trên máy và thời gian tin biến mất mặc định cho cuộc chat mới.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Giữ bộ nhớ đệm trên máy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tin cũ trên thiết bị sẽ được dọn sau ${current.cacheLabel()}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                CacheRetentionOption.entries.forEachIndexed { index, option ->
                    RadioOptionRow(
                        label = option.label,
                        selected = current.cacheRetentionDays == option.days,
                        onClick = { commit(current.copy(cacheRetentionDays = option.days)) },
                    )
                    if (index == CacheRetentionOption.entries.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tin biến mất (mặc định)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Áp dụng khi tạo cuộc chat mới: ${current.disappearingLabel()}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                DefaultDisappearingOption.entries.forEachIndexed { index, option ->
                    val selectedSeconds = current.defaultDisappearingSeconds ?: CLEAR_DEFAULT_DISAPPEARING
                    RadioOptionRow(
                        label = option.label,
                        selected = selectedSeconds == option.seconds,
                        onClick = {
                            val seconds = if (option.seconds == CLEAR_DEFAULT_DISAPPEARING) null else option.seconds
                            commit(current.copy(defaultDisappearingSeconds = seconds))
                        },
                    )
                    if (index == DefaultDisappearingOption.entries.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Theo từng phòng: menu [⋮] trong cuộc trò chuyện.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
