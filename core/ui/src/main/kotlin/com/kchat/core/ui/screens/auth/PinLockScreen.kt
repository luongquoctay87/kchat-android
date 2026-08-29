package com.kchat.core.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.PinRules
import com.kchat.core.ui.AuthFormColumn
import com.kchat.core.ui.KChatAuthScaffold
import com.kchat.core.ui.components.KChatOtpField

@Composable
fun PinLockScreen(
    pin: String,
    onPinChange: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isVerifying: Boolean = false,
    error: String? = null,
) {
    BackHandler { }

    KChatAuthScaffold(modifier = modifier) { padding ->
        AuthFormColumn(
            modifier = Modifier
                .padding(padding)
                .padding(KChatDimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Nhập mã PIN",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mã ${PinRules.LENGTH} số để mở khóa ứng dụng trên thiết bị này",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            KChatOtpField(
                value = pin,
                onValueChange = onPinChange,
                length = PinRules.LENGTH,
                isError = error != null,
                masked = true,
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
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onLogout, enabled = !isVerifying) {
                Text("Đăng xuất")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
