package com.kchat.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.RoomSummary

@Composable
fun RoomListItem(
    room: RoomSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTyping: Boolean = false,
) {
    val hasUnread = room.unreadCount > 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KChatDimens.screenPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            name = if (room.isChannel) "#" else room.title,
            imageUrl = room.avatarUrl,
            isOnline = room.isOnline && !room.isChannel,
            backgroundColor = if (room.isChannel) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = room.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (room.isMuted) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = "Đã tắt thông báo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp).size(16.dp),
                    )
                }
            }
            Text(
                text = if (isTyping) "đang nhập..." else room.preview,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = when {
                    isTyping -> FontWeight.Medium
                    hasUnread -> FontWeight.Medium
                    else -> FontWeight.Normal
                },
                color = when {
                    isTyping -> KChatColors.online
                    hasUnread -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = room.time,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                color = if (hasUnread) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            if (hasUnread) {
                Spacer(modifier = Modifier.padding(top = 4.dp))
                UnreadBadge(count = room.unreadCount)
            }
        }
    }
}
