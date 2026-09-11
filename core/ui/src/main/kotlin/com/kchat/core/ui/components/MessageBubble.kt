package com.kchat.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.ChatMessage
import com.kchat.core.model.FileDownloadStatus
import com.kchat.core.model.MessageType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onImageClick: (() -> Unit)? = null,
    onFileClick: (() -> Unit)? = null,
    fileDownloadStatus: FileDownloadStatus? = null,
    onLongClick: (() -> Unit)? = null,
    onReadReceiptClick: (() -> Unit)? = null,
    onReactionClick: ((String) -> Unit)? = null,
) {
    if (message.type == MessageType.Bot) {
        BotMessageBubble(
            title = message.botTitle.orEmpty(),
            service = message.botService,
            time = message.time,
            modifier = modifier,
        )
        return
    }
    if (message.type == MessageType.CallEvent) {
        Text(
            text = message.text.ifBlank { "Cuộc gọi" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 24.dp),
        )
        return
    }

    val maxBubbleWidth = (LocalConfiguration.current.screenWidthDp * 0.78f).dp
    val highlightBg by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = if (highlighted) 200 else 600),
        label = "messageHighlight",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(highlightBg)
            .padding(vertical = 2.dp, horizontal = 4.dp),
    ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
    ) {
        val senderName = message.senderName
        if (!message.isMine && senderName != null) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
        Row(
            horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
        ) {
            if (message.isMine) {
                MessageMeta(
                    time = message.time,
                    isRead = message.isRead,
                    onReadReceiptClick = onReadReceiptClick,
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
            Column(
                horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = maxBubbleWidth),
            ) {
                when (message.type) {
                    MessageType.Text -> TextBubble(message, onLongClick)
                    MessageType.Image -> ImageBubble(
                        message = message,
                        onClick = onImageClick,
                        onLongClick = onLongClick,
                    )
                    MessageType.File -> FileBubble(
                        message = message,
                        onClick = onFileClick,
                        onLongClick = onLongClick,
                        downloadStatus = fileDownloadStatus,
                    )
                    MessageType.Bot, MessageType.CallEvent -> Unit
                }
                ReactionRow(
                    reactions = message.reactions,
                    onReactionClick = onReactionClick,
                    modifier = Modifier.align(if (message.isMine) Alignment.End else Alignment.Start),
                )
            }
            if (!message.isMine) {
                MessageTime(message.time, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
    }
}

@Composable
private fun MessageMeta(
    time: String,
    isRead: Boolean,
    onReadReceiptClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(bottom = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (isRead) "✓✓" else "✓",
            style = MaterialTheme.typography.labelSmall,
            color = if (isRead) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isRead) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.then(
                if (isRead && onReadReceiptClick != null) {
                    Modifier.clickable(onClick = onReadReceiptClick)
                } else {
                    Modifier
                },
            ),
        )
    }
}

@Composable
private fun MessageTime(time: String, modifier: Modifier = Modifier) {
    Text(
        text = time,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun TextBubble(message: ChatMessage, onLongClick: (() -> Unit)?) {
    BubbleContainer(isMine = message.isMine, onLongClick = onLongClick) {
        message.replyTo?.let { ReplyQuoteBlock(it) }
        MessageFormattedBody(
            text = message.text,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (message.isEdited) {
            Text(
                text = "đã sửa",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
@Composable
private fun ImageBubble(message: ChatMessage, onClick: (() -> Unit)?, onLongClick: (() -> Unit)?) {
    BubbleContainer(
        isMine = message.isMine,
        onClick = onClick,
        onLongClick = onLongClick,
        padding = 4.dp,
    ) {
        val url = message.mediaUrl
        val imageModifier = Modifier
            .size(width = 180.dp, height = 120.dp)
            .clip(RoundedCornerShape(8.dp))
        if (!url.isNullOrBlank()) {
            val context = LocalContext.current
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                contentDescription = message.imageLabel ?: "Ảnh",
                modifier = imageModifier,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                },
                error = {
                    Box(
                        modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.BrokenImage,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                message.imageLabel ?: "Không xem được ảnh",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        } else {
            Box(
                modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Ảnh",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        message.imageLabel ?: "Ảnh",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FileBubble(
    message: ChatMessage,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    downloadStatus: FileDownloadStatus?,
) {
    val downloading = downloadStatus as? FileDownloadStatus.Downloading
    val saved = downloadStatus as? FileDownloadStatus.Saved
    BubbleContainer(
        isMine = message.isMine,
        onClick = if (downloading != null) null else onClick,
        onLongClick = onLongClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(message.fileName.orEmpty(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    when {
                        downloading != null -> {
                            val pct = downloading.progress?.let { (it * 100).toInt() }
                            if (pct != null) "Đang tải $pct%" else "Đang tải..."
                        }
                        saved != null -> listOfNotNull(
                            "Đã lưu",
                            message.fileSize?.takeIf { it.isNotBlank() },
                        ).joinToString(" · ")
                        else -> message.fileSize.orEmpty()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                downloading != null -> {
                    val progress = downloading.progress
                    if (progress != null) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
                saved != null -> {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = "Mở file",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Tải xuống",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BubbleContainer(
    isMine: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    padding: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = KChatDimens.bubbleRadius,
                    topEnd = KChatDimens.bubbleRadius,
                    bottomStart = if (isMine) KChatDimens.bubbleRadius else 4.dp,
                    bottomEnd = if (isMine) 4.dp else KChatDimens.bubbleRadius,
                ),
            )
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick,
                    )
                } else {
                    Modifier
                },
            )
            .background(if (isMine) KChatColors.bubbleSent else KChatColors.bubbleReceived)
            .padding(
                horizontal = if (padding == 0.dp) 14.dp else padding,
                vertical = if (padding == 0.dp) 10.dp else padding,
            ),
    ) {
        content()
    }
}
