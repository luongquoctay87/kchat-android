package com.kchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.MentionUser
import com.kchat.core.model.ReactionCount
import com.kchat.core.model.ReadReceipt
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kchat.core.model.MessageType
import com.kchat.core.model.ReplyQuote

@Composable
fun ReplyPreviewBar(
    replyTo: ReplyQuote,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        ReplyQuotePreviewContent(
            quote = replyTo,
            titlePrefix = "Trả lời ",
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Hủy trả lời")
        }
    }
}

@Composable
fun EditPreviewBar(
    previewText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreviewBar(
        title = "Sửa tin nhắn",
        subtitle = previewText.ifBlank { "…" },
        dismissContentDescription = "Hủy sửa",
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
private fun PreviewBar(
    title: String,
    subtitle: String,
    dismissContentDescription: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = dismissContentDescription)
        }
    }
}

@Composable
fun PinnedBanner(
    text: String,
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Bỏ ghim", modifier = Modifier)
                }
            }
        }
    }
}

@Composable
fun ChannelReadOnlyBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ) {
        Text(
            text = "Chỉ admin được gửi tin",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ReactionRow(
    reactions: List<ReactionCount>,
    modifier: Modifier = Modifier,
    onReactionClick: ((String) -> Unit)? = null,
) {
    if (reactions.isEmpty()) return
    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        reactions.forEach { reaction ->
            val selected = reaction.reactedByMe
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shadowElevation = 1.dp,
                modifier = if (onReactionClick != null) {
                    Modifier.clickable { onReactionClick(reaction.emoji) }
                } else {
                    Modifier
                },
            ) {
                Text(
                    text = "${reaction.emoji} ${reaction.count}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
fun MentionPickerOverlay(
    users: List<MentionUser>,
    onSelect: (MentionUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    ) {
        Column {
            users.take(4).forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(user) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(name = user.displayName, size = 36.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

val KChatReactionEmojis = listOf("👍", "❤️", "😂", "😮", "😢")

@Composable
fun MessageActionSheet(
    onReaction: (String) -> Unit,
    onReply: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    canPin: Boolean = true,
    myReactionEmojis: Set<String> = emptySet(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            KChatReactionEmojis.forEach { emoji ->
                val selected = emoji in myReactionEmojis
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.clickable { onReaction(emoji) },
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
        HorizontalDivider()
        SheetOptionRow(label = "Trả lời", icon = Icons.AutoMirrored.Filled.Reply, onClick = onReply)
        if (canPin) {
            SheetOptionRow(label = "Ghim", icon = Icons.Default.PushPin, onClick = onPin)
        }
        SheetOptionRow(
            label = "Sửa",
            icon = Icons.Default.Edit,
            onClick = onEdit,
            enabled = canEdit,
        )
        SheetOptionRow(
            label = "Xóa",
            icon = Icons.Default.Delete,
            onClick = onDelete,
            destructive = true,
            enabled = canDelete,
        )
    }
}

@Composable
fun ReadReceiptSheet(
    receipts: List<ReadReceipt>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Đã xem (${receipts.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        if (receipts.isEmpty()) {
            Text(
                text = "Chưa có ai xem",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        } else {
            receipts.forEach { receipt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(name = receipt.name, size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = receipt.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (receipt.time.isNotBlank()) {
                        Text(
                            text = receipt.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ReplyQuoteBlock(quote: ReplyQuote, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        ReplyQuotePreviewContent(
            quote = quote,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun ReplyQuotePreviewContent(
    quote: ReplyQuote,
    modifier: Modifier = Modifier,
    titlePrefix: String = "",
) {
    val mediaUrl = quote.mediaUrl
    val showReplyText = quote.text.isNotBlank() &&
        !(quote.previewType == MessageType.Image &&
            quote.text == "Ảnh" &&
            !mediaUrl.isNullOrBlank())

    Column(modifier = modifier) {
        Text(
            text = titlePrefix + quote.author,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (quote.previewType == MessageType.Image && !mediaUrl.isNullOrBlank()) {
            ReplyImageThumbnail(
                url = mediaUrl,
                label = quote.text.ifBlank { "Ảnh" },
                modifier = Modifier.padding(top = 4.dp),
            )
        } else if (quote.previewType == MessageType.Image || quote.text == "Ảnh") {
            Text(
                text = "Ảnh",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (showReplyText) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = if (quote.previewType == MessageType.Image) 4.dp else 2.dp),
            )
        }
    }
}

@Composable
private fun ReplyImageThumbnail(
    url: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageModifier = Modifier
        .size(width = 120.dp, height = 72.dp)
        .clip(RoundedCornerShape(6.dp))

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .allowHardware(false)
            .build(),
        contentDescription = label,
        modifier = modifier.then(imageModifier),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            }
        },
        error = {
            Box(
                modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
fun BotMessageBubble(
    title: String,
    service: String?,
    time: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(KChatDimens.bubbleRadius))
                .background(KChatColors.botAlert.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(KChatDimens.bubbleRadius),
                )
                .padding(14.dp),
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                service?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
        )
    }
}
