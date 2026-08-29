package com.kchat.core.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.kchat.core.ui.AuthFormColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.PasswordRules
import com.kchat.core.model.RegisterValidation
import com.kchat.core.ui.KChatAuthScaffold
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.components.KChatOtpField
import com.kchat.core.ui.components.KChatPrimaryButton
import com.kchat.core.ui.components.KChatTextField
import com.kchat.core.ui.components.KChatWordmark
import com.kchat.core.ui.components.WordmarkSize

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    identifier: String = "",
    password: String = "",
    isLoading: Boolean = false,
    error: String? = null,
    infoMessage: String? = null,
    /** Debug-only env badge: "Local" / "Staging"; null on prod. */
    debugBackendLabel: String? = null,
    onIdentifierChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLogin: () -> Unit = onLoginSuccess,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val keyboardOpen = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    KChatAuthScaffold(modifier = modifier) { padding ->
        AuthFormColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = KChatDimens.screenPadding)
                .padding(vertical = if (keyboardOpen) 8.dp else KChatDimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            KChatWordmark(
                size = if (keyboardOpen) WordmarkSize.Compact else WordmarkSize.Hero,
            )
            if (!keyboardOpen) {
                debugBackendLabel?.let { label ->
                    Spacer(modifier = Modifier.height(12.dp))
                    EnvBadge(label = label)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Đăng nhập để tiếp tục trò chuyện",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            infoMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(if (keyboardOpen) 16.dp else 28.dp))
            KChatTextField(
                value = identifier,
                onValueChange = onIdentifierChange,
                label = "Email / tên đăng nhập",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { runCatching { passwordFocusRequester.requestFocus() } },
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Mật khẩu",
                isPassword = true,
                modifier = Modifier.focusRequester(passwordFocusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (!isLoading && identifier.isNotBlank() && password.isNotBlank()) {
                            onLogin()
                        }
                    },
                ),
            )
            error?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            KChatPrimaryButton(
                text = if (isLoading) "Đang đăng nhập..." else "Đăng nhập",
                onClick = onLogin,
                enabled = !isLoading,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onRegister) { Text("Đăng ký") }
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                TextButton(onClick = onForgotPassword) { Text("Quên mật khẩu?") }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    displayName: String = "",
    username: String = "",
    email: String = "",
    password: String = "",
    confirm: String = "",
    isSendingOtp: Boolean = false,
    showOtpDialog: Boolean = false,
    otp: String = "",
    isVerifyingOtp: Boolean = false,
    formError: String? = null,
    usernameError: String? = null,
    passwordError: String? = null,
    confirmError: String? = null,
    otpError: String? = null,
    onDisplayNameChange: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onConfirmChange: (String) -> Unit = {},
    onOtpChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onVerifyOtp: () -> Unit = {},
    onResendOtp: () -> Unit = {},
    onDismissOtp: () -> Unit = {},
) {
    if (showOtpDialog) {
        RegisterOtpDialog(
            email = email.trim(),
            otp = otp,
            isVerifying = isVerifyingOtp,
            isResending = isSendingOtp,
            error = otpError,
            onOtpChange = onOtpChange,
            onVerify = onVerifyOtp,
            onResend = onResendOtp,
            onDismiss = onDismissOtp,
        )
    }

    KChatAuthScaffold(
        modifier = modifier,
        title = "Đăng ký",
        onBack = onLogin,
    ) { padding ->
        AuthFormColumn(
            modifier = Modifier
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Text(
                text = "Tạo tài khoản mới",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Xác minh email bằng mã OTP trước khi hoàn tất.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            KChatTextField(value = displayName, onValueChange = onDisplayNameChange, label = "Tên hiển thị")
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "Username",
                error = usernameError,
                supportingText = if (usernameError == null) RegisterValidation.USERNAME_HINT else null,
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(value = email, onValueChange = onEmailChange, label = "Email")
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Mật khẩu",
                isPassword = true,
                error = passwordError,
                supportingText = if (passwordError == null) PasswordRules.HINT else null,
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(
                value = confirm,
                onValueChange = onConfirmChange,
                label = "Xác nhận mật khẩu",
                isPassword = true,
                error = confirmError,
            )
            formError?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))
            KChatPrimaryButton(
                text = if (isSendingOtp) "Đang gửi OTP..." else "Tiếp tục",
                onClick = onSubmit,
                enabled = !isSendingOtp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Đã có tài khoản? Đăng nhập", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun RegisterOtpDialog(
    email: String,
    otp: String,
    isVerifying: Boolean,
    isResending: Boolean,
    error: String?,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isVerifying) onDismiss() },
        icon = {
            AuthIconBadge(Icons.Outlined.MarkEmailRead)
        },
        title = {
            Text(
                text = "Xác minh email",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nhập mã 6 số đã gửi tới",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
                )
                KChatOtpField(
                    value = otp,
                    onValueChange = onOtpChange,
                    isError = error != null,
                )
                error?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                TextButton(
                    onClick = onResend,
                    enabled = !isResending && !isVerifying,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(if (isResending) "Đang gửi lại..." else "Gửi lại mã")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onVerify,
                enabled = otp.length == 6 && !isVerifying,
            ) {
                Text(if (isVerifying) "Đang xác minh..." else "Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isVerifying) {
                Text("Hủy")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    email: String = "",
    isLoading: Boolean = false,
    error: String? = null,
    onEmailChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    KChatDetailScaffold(
        modifier = modifier,
        title = "Quên mật khẩu",
        onBack = onBack,
    ) { padding ->
        AuthFormColumn(
            modifier = Modifier
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AuthIconBadge(Icons.Outlined.LockReset)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Khôi phục truy cập",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Nhập đúng email đã đăng ký. Nếu tài khoản tồn tại, mã OTP sẽ được gửi trong vài phút.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tài khoản chỉ có tên đăng nhập → liên hệ quản trị viên.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            KChatTextField(value = email, onValueChange = onEmailChange, label = "Email")
            error?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))
            KChatPrimaryButton(
                text = if (isLoading) "Đang gửi..." else "Gửi mã OTP",
                onClick = onSubmit,
                enabled = email.isNotBlank() && !isLoading,
            )
        }
    }
}

@Composable
fun VerifyResetOtpScreen(
    email: String,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onResend: () -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier,
    otp: String = "",
    isLoading: Boolean = false,
    isResending: Boolean = false,
    error: String? = null,
    resendMessage: String? = null,
    resendError: String? = null,
    onOtpChange: (String) -> Unit = {},
) {
    KChatDetailScaffold(
        modifier = modifier,
        title = "Nhập mã OTP",
        onBack = onBack,
    ) { padding ->
        AuthFormColumn(
            modifier = Modifier
                .padding(padding)
                .padding(KChatDimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AuthIconBadge(Icons.Outlined.Email)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kiểm tra hộp thư",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mã 6 số đã gửi đến",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "Hết hạn sau 30 phút · kiểm tra cả spam",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(28.dp))
            KChatOtpField(
                value = otp,
                onValueChange = onOtpChange,
                isError = error != null,
            )
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
            Spacer(modifier = Modifier.height(28.dp))
            KChatPrimaryButton(
                text = if (isLoading) "Đang xác minh..." else "Xác nhận",
                onClick = onVerify,
                enabled = otp.length == 6 && !isLoading,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onResend, enabled = !isResending && !isLoading) {
                Text(if (isResending) "Đang gửi lại..." else "Gửi lại mã OTP")
            }
            resendMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
            resendError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Quay lại đăng nhập")
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    password: String = "",
    confirm: String = "",
    isLoading: Boolean = false,
    error: String? = null,
    onPasswordChange: (String) -> Unit = {},
    onConfirmChange: (String) -> Unit = {},
) {
    val canSubmit = PasswordRules.validate(password) == null && password == confirm

    KChatAuthScaffold(modifier = modifier) { padding ->
        AuthFormColumn(
            modifier = Modifier
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            AuthIconBadge(Icons.Outlined.LockReset)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đặt mật khẩu mới",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Chọn mật khẩu mạnh để bảo vệ tài khoản.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            KChatTextField(value = password, onValueChange = onPasswordChange, label = "Mật khẩu mới", isPassword = true)
            Text(
                text = PasswordRules.HINT,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            KChatTextField(value = confirm, onValueChange = onConfirmChange, label = "Xác nhận mật khẩu", isPassword = true)
            error?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))
            KChatPrimaryButton(
                text = if (isLoading) "Đang lưu..." else "Xác nhận",
                onClick = onConfirm,
                enabled = canSubmit && !isLoading,
            )
        }
    }
}

@Composable
private fun EnvBadge(label: String) {
    val isStaging = label.equals("Staging", ignoreCase = true)
    val container = if (isStaging) {
        Color(0xFFFFF3E0) // amber soft
    } else {
        Color(0xFFE8F5E9) // green soft (Local)
    }
    val content = if (isStaging) {
        Color(0xFFE65100)
    } else {
        Color(0xFF1B5E20)
    }
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = content,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(container)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun AuthIconBadge(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp),
        )
    }
}
