package com.kchat.core.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kchat.core.model.RoomSummary
import com.kchat.core.ui.components.RoomListItem
import com.kchat.core.ui.demo.SampleData

@Composable
fun ChatListScreen(
    onRoomClick: (RoomSummary) -> Unit,
    modifier: Modifier = Modifier,
    rooms: List<RoomSummary> = SampleData.rooms,
    typingRoomIds: Set<String> = emptySet(),
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(rooms, key = { it.id }) { room ->
            RoomListItem(
                room = room,
                isTyping = room.id.trim().lowercase() in typingRoomIds,
                onClick = { onRoomClick(room) },
            )
            HorizontalDivider()
        }
    }
}
