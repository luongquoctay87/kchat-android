package com.kchat.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kchat.core.design.KChatTheme
import com.kchat.core.ui.screens.ChatListScreen

@Composable
fun KChatScaffold(
    currentTab: KChatTab,
    onTabSelected: (KChatTab) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showWordmark: Boolean = false,
    showBottomBar: Boolean = true,
    chatUnreadCount: Int = 0,
    bottomTabs: List<KChatTab> = KChatTab.entries,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            KChatTopBar(
                title = title,
                showWordmark = showWordmark,
                actions = actions,
            )
        },
        bottomBar = {
            if (showBottomBar) {
                KChatBottomBar(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected,
                    chatUnreadCount = chatUnreadCount,
                    tabs = bottomTabs,
                )
            }
        },
        content = content,
    )
}

@Composable
fun KChatBottomBar(
    currentTab: KChatTab,
    onTabSelected: (KChatTab) -> Unit,
    modifier: Modifier = Modifier,
    chatUnreadCount: Int = 0,
    tabs: List<KChatTab> = KChatTab.entries,
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == KChatTab.Chat && chatUnreadCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(if (chatUnreadCount > 99) "99+" else chatUnreadCount.toString())
                                }
                            },
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                        )
                    }
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KChatScaffoldPreview() {
    KChatTheme {
        KChatScaffold(
            currentTab = KChatTab.Chat,
            onTabSelected = {},
            showWordmark = true,
            chatUnreadCount = 3,
        ) { padding ->
            ChatListScreen(
                onRoomClick = {},
                modifier = Modifier.padding(padding),
            )
        }
    }
}
