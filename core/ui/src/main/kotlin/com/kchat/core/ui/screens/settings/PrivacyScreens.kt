package com.kchat.core.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import com.kchat.core.ui.SettingsRow
import com.kchat.core.ui.components.KChatSettingsCard
import com.kchat.core.ui.components.RadioOptionRow
import java.util.Calendar

enum class DmPrivacyPolicy {
    Everyone,
    Contacts,
    Nobody,
}

data class QuietHours(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val endHour: Int = 7,
)

fun DmPrivacyPolicy.label(): String = when (this) {
    DmPrivacyPolicy.Everyone -> "Mọi người"
    DmPrivacyPolicy.Contacts -> "Danh bạ của tôi"
    DmPrivacyPolicy.Nobody -> "Không ai"
}

fun QuietHours.summaryLabel(): String {
    if (!enabled) return "Tắt"
    return "${startHour.toString().padStart(2, '0')}:00 – ${endHour.toString().padStart(2, '0')}:00"
}

/** True when quiet hours are on and the current local hour falls in the window (supports overnight). */
fun QuietHours.isActiveNow(nowHour: Int): Boolean {
    if (!enabled) return false
    if (startHour == endHour) return false
    return if (startHour < endHour) {
        nowHour in startHour until endHour
    } else {
        nowHour >= startHour || nowHour < endHour
    }
}

private fun quietHourLabel(hour: Int): String = "${hour.toString().padStart(2, '0')}:00"

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onDmPolicy: () -> Unit,
    onQuietHours: () -> Unit,
    onAppPin: () -> Unit = {},
    modifier: Modifier = Modifier,
    showOnline: Boolean = true,
    onShowOnlineChange: (Boolean) -> Unit = {},
    dmPolicy: DmPrivacyPolicy = DmPrivacyPolicy.Everyone,
    quietHours: QuietHours = QuietHours(),
    pinEnabled: Boolean = false,
) {
    KChatDetailScaffold(
        modifier = modifier,
        title = "Quyền riêng tư",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text(
                text = "Trạng thái",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Hiển thị trạng thái trực tuyến",
                    subtitle = "Cho phép mọi người thấy bạn đang trực tuyến",
                    checked = showOnline,
                    onCheckedChange = onShowOnlineChange,
                    showDivider = false,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tin nhắn & thời gian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Ai được nhắn tin riêng",
                    subtitle = dmPolicy.label(),
                    onClick = onDmPolicy,
                )
                SettingsRow(
                    label = "Giờ yên lặng",
                    subtitle = quietHours.summaryLabel(),
                    onClick = onQuietHours,
                    showDivider = false,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Trong giờ yên lặng, thông báo sẽ được tắt. Tin nhắn vẫn nhận bình thường.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Khóa ứng dụng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Mã PIN khóa app",
                    subtitle = if (pinEnabled) "Đã bật — hỏi PIN khi mở app" else "Chưa bật",
                    onClick = onAppPin,
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
fun PrivacyDmScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    selected: DmPrivacyPolicy = DmPrivacyPolicy.Everyone,
    onSelectedChange: (DmPrivacyPolicy) -> Unit = {},
) {
    // Local selection while on this screen; re-init only when composable is recreated.
    var current by remember { mutableStateOf(selected) }

    KChatDetailScaffold(
        modifier = modifier,
        title = "Ai được nhắn tin riêng",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text(
                text = "Chọn ai có thể gửi tin nhắn trực tiếp cho bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatSettingsCard {
                DmPrivacyPolicy.entries.forEachIndexed { index, policy ->
                    RadioOptionRow(
                        label = policy.label(),
                        selected = current == policy,
                        onClick = {
                            current = policy
                            onSelectedChange(policy)
                        },
                    )
                    if (index == DmPrivacyPolicy.entries.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (current) {
                    DmPrivacyPolicy.Everyone -> "Mọi người trong tổ chức có thể nhắn tin riêng với bạn."
                    DmPrivacyPolicy.Contacts -> "Chỉ người đã từng chat chung với bạn (cùng phòng/nhóm) mới có thể bắt đầu chat riêng mới."
                    DmPrivacyPolicy.Nobody -> "Không ai có thể bắt đầu cuộc trò chuyện riêng mới với bạn. Các cuộc chat cũ vẫn mở bình thường."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val QuietHourOptions = (0..23).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourDropdown(
    label: String,
    selectedHour: Int,
    onHourSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = quietHourLabel(selectedHour),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp),
        ) {
            QuietHourOptions.forEach { hour ->
                DropdownMenuItem(
                    text = { Text(quietHourLabel(hour)) },
                    onClick = {
                        onHourSelected(hour)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun PrivacyQuietHoursScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    quietHours: QuietHours = QuietHours(),
    onQuietHoursChange: (QuietHours) -> Unit = {},
) {
    // Local draft while on this screen; re-init only when composable is recreated (same as DM).
    var current by remember { mutableStateOf(quietHours) }
    val nowHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isActiveNow = current.isActiveNow(nowHour)
    val invalidWindow = current.enabled && current.startHour == current.endHour

    fun commit(updated: QuietHours) {
        current = updated
        if (updated.enabled && updated.startHour == updated.endHour) return
        onQuietHoursChange(updated)
    }

    KChatDetailScaffold(
        modifier = modifier,
        title = "Giờ yên lặng",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text(
                text = "Tắt thông báo đẩy trong khung giờ bạn chọn. Tin nhắn vẫn nhận bình thường.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Bật giờ yên lặng",
                    subtitle = current.summaryLabel(),
                    checked = current.enabled,
                    onCheckedChange = { enabled -> commit(current.copy(enabled = enabled)) },
                    showDivider = false,
                )
            }
            if (isActiveNow) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Đang trong giờ yên lặng — thông báo đẩy tạm tắt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HourDropdown(
                    label = "Từ",
                    selectedHour = current.startHour,
                    onHourSelected = { hour -> commit(current.copy(startHour = hour)) },
                    enabled = current.enabled,
                    modifier = Modifier.weight(1f),
                )
                HourDropdown(
                    label = "Đến",
                    selectedHour = current.endHour,
                    onHourSelected = { hour -> commit(current.copy(endHour = hour)) },
                    enabled = current.enabled,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (invalidWindow) {
                    "Giờ bắt đầu và kết thúc phải khác nhau."
                } else {
                    "Áp dụng mỗi ngày, kể cả qua đêm (ví dụ 22:00–07:00)."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (invalidWindow) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
