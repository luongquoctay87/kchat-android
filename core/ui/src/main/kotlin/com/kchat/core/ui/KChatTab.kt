package com.kchat.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class KChatTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Chat("chat_list", "Chat", Icons.AutoMirrored.Filled.Chat),
    Contacts("contacts", "Danh bạ", Icons.Default.Contacts),
    Settings("settings", "Cài đặt", Icons.Default.Settings),
}
