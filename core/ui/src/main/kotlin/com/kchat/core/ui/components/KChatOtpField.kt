package com.kchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kchat.core.design.KChatDimens

@Composable
fun KChatOtpField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    autoFocus: Boolean = true,
    masked: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    val digits = value.filter { it.isDigit() }.take(length)

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    BasicTextField(
        value = digits,
        onValueChange = { raw ->
            onValueChange(raw.filter { it.isDigit() }.take(length))
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                repeat(length) { index ->
                    val char = digits.getOrNull(index)?.toString().orEmpty()
                    val isActive = digits.length == index || (digits.length == length && index == length - 1)
                    OtpCell(
                        digit = char,
                        isActive = isActive && !isError,
                        isError = isError,
                        masked = masked,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Composable
private fun OtpCell(
    digit: String,
    isActive: Boolean,
    isError: Boolean,
    masked: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val container = when {
        isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(KChatDimens.cardRadius))
            .background(container)
            .border(
                width = if (isActive || isError) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(KChatDimens.cardRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = when {
                digit.isEmpty() -> "·"
                masked -> "●"
                else -> digit
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 0.sp,
            ),
            color = if (digit.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
        )
    }
}
