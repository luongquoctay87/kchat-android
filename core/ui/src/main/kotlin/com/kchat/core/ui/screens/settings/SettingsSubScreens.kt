package com.kchat.core.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kchat.core.design.FontScale
import com.kchat.core.design.KChatAppearance
import com.kchat.core.design.KChatDimens
import com.kchat.core.design.ThemeMode
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.SettingsRow
import com.kchat.core.ui.components.KChatPrimaryButton
import com.kchat.core.ui.components.KChatSettingsCard
import com.kchat.core.ui.components.KChatTextField
import com.kchat.core.ui.components.RadioOptionRow
import com.kchat.core.ui.components.UserAvatar

@Composable
fun ProfileScreen(
    displayName: String,
    email: String,
    username: String,
    phone: String = "",
    avatarUrl: String? = null,
    isSaving: Boolean = false,
    isUploadingAvatar: Boolean = false,
    error: String? = null,
    onSave: (displayName: String, phone: String) -> Unit,
    onChangeAvatar: (uri: Uri, mimeType: String?, displayName: String?) -> Unit,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editedName by remember(displayName) { mutableStateOf(displayName) }
    var editedPhone by remember(phone) { mutableStateOf(phone) }
    val context = LocalContext.current
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mime = context.contentResolver.getType(uri)
        onChangeAvatar(uri, mime, null)
    }
    val canEdit = !isSaving && !isUploadingAvatar
    val nameDirty = editedName.trim() != displayName.trim()
    val phoneDirty = editedPhone.trim() != phone.trim()
    val canSave = canEdit && editedName.isNotBlank() && (nameDirty || phoneDirty)

    KChatDetailScaffold(
        modifier = modifier,
        title = "Hồ sơ",
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
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    KChatPrimaryButton(
                        text = if (isSaving) "Đang lưu..." else "Lưu",
                        onClick = { onSave(editedName, editedPhone) },
                        enabled = canSave,
                    )
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
                name = editedName.ifBlank { displayName }.ifBlank { "?" },
                size = KChatDimens.avatarProfile,
                imageUrl = avatarUrl,
            )
            TextButton(
                onClick = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                enabled = canEdit,
            ) {
                Text(
                    text = if (isUploadingAvatar) "Đang tải ảnh..." else "Đổi ảnh",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ProfileSectionTitle("Thông tin")
            Spacer(modifier = Modifier.height(8.dp))
            KChatTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = "Tên hiển thị",
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(
                value = editedPhone,
                onValueChange = { editedPhone = it },
                label = "Số điện thoại",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Email",
                    subtitle = email.ifBlank { "—" },
                )
                SettingsRow(
                    label = "Tên đăng nhập",
                    subtitle = if (username.isBlank()) "—" else "@$username",
                    showDivider = false,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            ProfileSectionTitle("Bảo mật")
            Spacer(modifier = Modifier.height(8.dp))
            KChatSettingsCard {
                SettingsRow(
                    label = "Đổi mật khẩu",
                    onClick = onChangePassword,
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun ChangePasswordScreen(
    isSaving: Boolean = false,
    error: String? = null,
    success: String? = null,
    onSave: (current: String, newPassword: String, confirm: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    KChatDetailScaffold(
        modifier = modifier,
        title = "Đổi mật khẩu",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            KChatTextField(value = current, onValueChange = { current = it }, label = "Mật khẩu hiện tại", isPassword = true)
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(value = newPass, onValueChange = { newPass = it }, label = "Mật khẩu mới", isPassword = true)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(value = confirm, onValueChange = { confirm = it }, label = "Xác nhận mật khẩu", isPassword = true)
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (success != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            KChatPrimaryButton(
                text = if (isSaving) "Đang lưu..." else "Lưu",
                onClick = { onSave(current, newPass, confirm) },
                enabled = !isSaving && success == null,
            )
        }
    }
}

@Composable
fun AppearanceScreen(
    appearance: KChatAppearance,
    onAppearanceChange: (KChatAppearance) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KChatDetailScaffold(
        modifier = modifier,
        title = "Giao diện",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text("Chế độ giao diện", style = MaterialTheme.typography.titleMedium)
            RadioOptionRow(
                label = "Hệ thống",
                selected = appearance.themeMode == ThemeMode.System,
                onClick = { onAppearanceChange(appearance.copy(themeMode = ThemeMode.System)) },
            )
            RadioOptionRow(
                label = "Sáng",
                selected = appearance.themeMode == ThemeMode.Light,
                onClick = { onAppearanceChange(appearance.copy(themeMode = ThemeMode.Light)) },
            )
            RadioOptionRow(
                label = "Tối",
                selected = appearance.themeMode == ThemeMode.Dark,
                onClick = { onAppearanceChange(appearance.copy(themeMode = ThemeMode.Dark)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cỡ chữ", style = MaterialTheme.typography.titleMedium)
            RadioOptionRow(
                label = "S",
                selected = appearance.fontScale == FontScale.Small,
                onClick = { onAppearanceChange(appearance.copy(fontScale = FontScale.Small)) },
            )
            RadioOptionRow(
                label = "M",
                selected = appearance.fontScale == FontScale.Medium,
                onClick = { onAppearanceChange(appearance.copy(fontScale = FontScale.Medium)) },
            )
            RadioOptionRow(
                label = "L",
                selected = appearance.fontScale == FontScale.Large,
                onClick = { onAppearanceChange(appearance.copy(fontScale = FontScale.Large)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                SettingsRow(
                    label = "Nhấn Enter để gửi",
                    checked = appearance.enterToSend,
                    onCheckedChange = { onAppearanceChange(appearance.copy(enterToSend = it)) },
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    pushEnabled: Boolean,
    onPushEnabledChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KChatDetailScaffold(
        modifier = modifier,
        title = "Thông báo",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                SettingsRow(
                    label = "Thông báo đẩy",
                    checked = pushEnabled,
                    onCheckedChange = onPushEnabledChange,
                    showDivider = false,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tắt tiếng từng phòng: menu [⋮] trong cuộc trò chuyện",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
