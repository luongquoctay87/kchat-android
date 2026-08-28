package com.kchat.core.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.PinRules
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.SettingsRow
import com.kchat.core.ui.components.KChatOtpField
import com.kchat.core.ui.components.KChatPrimaryButton
import com.kchat.core.ui.components.KChatSettingsCard

enum class PinSettingsMode {
    Overview,
    Create,
    Change,
    Disable,
    CreateEmergency,
    ChangeEmergency,
    DisableEmergency,
}

@Composable
fun PinSettingsScreen(
    enabled: Boolean,
    emergencyEnabled: Boolean,
    mode: PinSettingsMode,
    currentPin: String,
    newPin: String,
    confirmPin: String,
    error: String?,
    isSaving: Boolean,
    onBack: () -> Unit,
    onModeChange: (PinSettingsMode) -> Unit,
    onCurrentPinChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (mode) {
        PinSettingsMode.Overview -> "Mã PIN khóa app"
        PinSettingsMode.Create -> "Tạo PIN"
        PinSettingsMode.Change -> "Đổi PIN"
        PinSettingsMode.Disable -> "Tắt PIN"
        PinSettingsMode.CreateEmergency -> "Tạo mã khẩn cấp"
        PinSettingsMode.ChangeEmergency -> "Đổi mã khẩn cấp"
        PinSettingsMode.DisableEmergency -> "Tắt mã khẩn cấp"
    }
    KChatDetailScaffold(
        modifier = modifier,
        title = title,
        onBack = {
            if (mode == PinSettingsMode.Overview) onBack() else onModeChange(PinSettingsMode.Overview)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            when (mode) {
                PinSettingsMode.Overview -> {
                    Text(
                        text = "PIN khóa app",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mở app bằng PIN ${PinRules.LENGTH} số. Không thay mật khẩu đăng nhập.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    KChatSettingsCard {
                        if (enabled) {
                            SettingsRow(
                                label = "Đổi PIN",
                                subtitle = "Đặt mã mới",
                                onClick = { onModeChange(PinSettingsMode.Change) },
                            )
                            SettingsRow(
                                label = "Tắt PIN",
                                subtitle = "Không hỏi PIN khi mở app",
                                onClick = { onModeChange(PinSettingsMode.Disable) },
                                showDivider = false,
                            )
                        } else {
                            SettingsRow(
                                label = "Tạo PIN ${PinRules.LENGTH} số",
                                subtitle = "Cần đăng nhập trước",
                                onClick = { onModeChange(PinSettingsMode.Create) },
                                showDivider = false,
                            )
                        }
                    }

                    if (enabled) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Mã khẩn cấp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nhập thay PIN trên màn khóa — xóa tin nhắn khẩn cấp khi cần. Khác PIN thường.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        KChatSettingsCard {
                            if (emergencyEnabled) {
                                SettingsRow(
                                    label = "Đổi mã khẩn cấp",
                                    subtitle = "Cần PIN hiện tại",
                                    onClick = { onModeChange(PinSettingsMode.ChangeEmergency) },
                                )
                                SettingsRow(
                                    label = "Tắt mã khẩn cấp",
                                    subtitle = "Không xóa tin nữa",
                                    onClick = { onModeChange(PinSettingsMode.DisableEmergency) },
                                    showDivider = false,
                                )
                            } else {
                                SettingsRow(
                                    label = "Tạo mã ${PinRules.LENGTH} số",
                                    subtitle = "Xoá tin nhắn khẩn cấp khi cần",
                                    onClick = { onModeChange(PinSettingsMode.CreateEmergency) },
                                    showDivider = false,
                                )
                            }
                        }
                    }
                }
                PinSettingsMode.Create -> {
                    PinFieldBlock(
                        label = "PIN mới",
                        value = newPin,
                        onValueChange = onNewPinChange,
                        isError = error != null,
                        autoFocus = true,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "Nhập lại",
                        value = confirmPin,
                        onValueChange = onConfirmPinChange,
                        isError = error != null,
                    )
                    PinError(error)
                    Spacer(modifier = Modifier.height(24.dp))
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang lưu..." else "Bật PIN",
                        onClick = onSubmit,
                        enabled = newPin.length == PinRules.LENGTH &&
                            confirmPin.length == PinRules.LENGTH && !isSaving,
                    )
                }
                PinSettingsMode.Change -> {
                    PinFieldBlock(
                        label = "PIN hiện tại",
                        value = currentPin,
                        onValueChange = onCurrentPinChange,
                        isError = error != null,
                        autoFocus = true,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "PIN mới",
                        value = newPin,
                        onValueChange = onNewPinChange,
                        isError = error != null,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "Nhập lại",
                        value = confirmPin,
                        onValueChange = onConfirmPinChange,
                        isError = error != null,
                    )
                    PinError(error)
                    Spacer(modifier = Modifier.height(24.dp))
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang lưu..." else "Đổi PIN",
                        onClick = onSubmit,
                        enabled = currentPin.length == PinRules.LENGTH &&
                            newPin.length == PinRules.LENGTH &&
                            confirmPin.length == PinRules.LENGTH && !isSaving,
                    )
                }
                PinSettingsMode.Disable -> {
                    Text(
                        text = "Nhập PIN để tắt khóa app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "PIN hiện tại",
                        value = currentPin,
                        onValueChange = onCurrentPinChange,
                        isError = error != null,
                        autoFocus = true,
                    )
                    PinError(error)
                    Spacer(modifier = Modifier.height(24.dp))
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang tắt..." else "Tắt PIN",
                        onClick = onSubmit,
                        enabled = currentPin.length == PinRules.LENGTH && !isSaving,
                    )
                }
                PinSettingsMode.CreateEmergency -> {
                    Text(
                        text = "Xác nhận PIN, rồi đặt mã khẩn cấp (khác PIN).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "PIN hiện tại",
                        value = currentPin,
                        onValueChange = onCurrentPinChange,
                        isError = error != null,
                        autoFocus = true,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "Mã mới",
                        value = newPin,
                        onValueChange = onNewPinChange,
                        isError = error != null,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "Nhập lại",
                        value = confirmPin,
                        onValueChange = onConfirmPinChange,
                        isError = error != null,
                    )
                    PinError(error)
                    Spacer(modifier = Modifier.height(24.dp))
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang lưu..." else "Lưu",
                        onClick = onSubmit,
                        enabled = currentPin.length == PinRules.LENGTH &&
                            newPin.length == PinRules.LENGTH &&
                            confirmPin.length == PinRules.LENGTH && !isSaving,
                    )
                }
                PinSettingsMode.ChangeEmergency -> {
                    PinFieldBlock(
                        label = "PIN hiện tại",
                        value = currentPin,
                        onValueChange = onCurrentPinChange,
                        isError = error != null,
                        autoFocus = true,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "Mã mới",
                        value = newPin,
                        onValueChange = onNewPinChange,
                        isError = error != null,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "Nhập lại",
                        value = confirmPin,
                        onValueChange = onConfirmPinChange,
                        isError = error != null,
                    )
                    PinError(error)
                    Spacer(modifier = Modifier.height(24.dp))
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang lưu..." else "Lưu",
                        onClick = onSubmit,
                        enabled = currentPin.length == PinRules.LENGTH &&
                            newPin.length == PinRules.LENGTH &&
                            confirmPin.length == PinRules.LENGTH && !isSaving,
                    )
                }
                PinSettingsMode.DisableEmergency -> {
                    Text(
                        text = "Nhập PIN để tắt mã khẩn cấp.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PinFieldBlock(
                        label = "PIN hiện tại",
                        value = currentPin,
                        onValueChange = onCurrentPinChange,
                        isError = error != null,
                        autoFocus = true,
                    )
                    PinError(error)
                    Spacer(modifier = Modifier.height(24.dp))
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang tắt..." else "Tắt mã khẩn cấp",
                        onClick = onSubmit,
                        enabled = currentPin.length == PinRules.LENGTH && !isSaving,
                    )
                }
            }
        }
    }
}

@Composable
private fun PinFieldBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    autoFocus: Boolean = false,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    KChatOtpField(
        value = value,
        onValueChange = onValueChange,
        length = PinRules.LENGTH,
        isError = isError,
        autoFocus = autoFocus,
        masked = true,
    )
}

@Composable
private fun PinError(error: String?) {
    error?.let {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
