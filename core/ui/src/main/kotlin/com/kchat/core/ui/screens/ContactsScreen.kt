package com.kchat.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.ContactSummary
import com.kchat.core.ui.components.KChatSearchField
import com.kchat.core.ui.components.UserAvatar
import com.kchat.core.ui.demo.SampleData

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    contacts: List<ContactSummary> = SampleData.contacts,
    onContactClick: (ContactSummary) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val filtered = contacts.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true)
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    fun hideKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val listState = rememberLazyListState()
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) hideKeyboard()
    }

    Column(modifier = modifier.fillMaxSize()) {
        KChatSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Tìm tên, email...",
            modifier = Modifier.padding(KChatDimens.screenPadding),
            keyboardActions = KeyboardActions(onSearch = { hideKeyboard() }),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                // Initial pass: clear focus without consuming — list clicks/scroll still work
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        hideKeyboard()
                    }
                },
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = if (query.isBlank()) {
                            "Chưa có liên hệ"
                        } else {
                            "Không tìm thấy \"$query\""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(KChatDimens.screenPadding),
                    )
                }
            } else {
                items(filtered, key = { it.id }) { contact ->
                    ContactRow(
                        contact = contact,
                        onClick = {
                            hideKeyboard()
                            onContactClick(contact)
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: ContactSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KChatDimens.screenPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(name = contact.name, imageUrl = contact.avatarUrl, isOnline = contact.isOnline)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = contact.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (contact.isOnline) {
                    KChatColors.online
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
