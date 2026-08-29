package com.kchat.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kchat.core.model.PinnedMessage

/**
 * Compact pin strip: one preview row by default; expand to browse / unpin.
 * - Tap preview → jump to that message
 * - Chevron → expand / collapse list
 */
@Composable
fun PinnedMessagesBar(
    pins: List<PinnedMessage>,
    onClick: (messageId: String) -> Unit,
    onUnpin: ((messageId: String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (pins.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val preview = pins.first()
    val scroll = rememberScrollState()
    val canExpand = pins.size > 1

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PinnedHeaderRow(
                previewText = preview.text,
                count = pins.size,
                expanded = expanded && canExpand,
                onPreviewClick = {
                    if (preview.messageId.isNotBlank()) onClick(preview.messageId)
                },
                onToggleExpand = { if (canExpand) expanded = !expanded },
                onUnpinPreview = onUnpin
                    ?.takeIf { !canExpand && preview.messageId.isNotBlank() }
                    ?.let { unpin -> { unpin(preview.messageId) } },
            )

            AnimatedVisibility(
                visible = expanded && canExpand,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 168.dp)
                            .verticalScroll(scroll),
                    ) {
                        pins.forEachIndexed { index, pin ->
                            PinnedListItem(
                                index = index + 1,
                                pin = pin,
                                onClick = {
                                    if (pin.messageId.isNotBlank()) {
                                        onClick(pin.messageId)
                                        expanded = false
                                    }
                                },
                                onUnpin = onUnpin
                                    ?.takeIf { pin.messageId.isNotBlank() }
                                    ?.let { unpin -> { unpin(pin.messageId) } },
                            )
                            if (index < pins.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 36.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinnedHeaderRow(
    previewText: String,
    count: Int,
    expanded: Boolean,
    onPreviewClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onUnpinPreview: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.PushPin,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = previewText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onPreviewClick),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count > 1) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Thu gọn" else "Mở danh sách ghim",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else if (onUnpinPreview != null) {
            IconButton(onClick = onUnpinPreview) {
                Icon(Icons.Default.Close, contentDescription = "Bỏ ghim")
            }
        }
    }
}

@Composable
private fun PinnedListItem(
    index: Int,
    pin: PinnedMessage,
    onClick: () -> Unit,
    onUnpin: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = pin.text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (onUnpin != null) {
            IconButton(onClick = onUnpin) {
                Icon(Icons.Default.Close, contentDescription = "Bỏ ghim")
            }
        }
    }
}
