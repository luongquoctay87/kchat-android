package com.kchat.core.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.ContactSummary
import com.kchat.core.ui.components.KChatSearchField
import com.kchat.core.ui.components.SheetCancelRow
import com.kchat.core.ui.components.SheetHandle
import com.kchat.core.ui.components.SheetOptionRow
import com.kchat.core.ui.components.UserAvatar
import com.kchat.core.ui.demo.SampleData

private enum class ContactRowAction {
    Add,
    Added,
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    contacts: List<ContactSummary> = SampleData.contacts,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    searchResults: List<ContactSummary> = emptyList(),
    isSearching: Boolean = false,
    searchError: String? = null,
    actionError: String? = null,
    onContactClick: (ContactSummary) -> Unit = {},
    onAddContact: (ContactSummary) -> Unit = {},
    onRemoveContact: (ContactSummary) -> Unit = {},
    onViewContactDetail: (ContactSummary) -> Unit = {},
) {
    val trimmedQuery = query.trim()
    val searchingDirectory = trimmedQuery.length >= 2
    val filteredContacts = contacts.filter {
        trimmedQuery.isEmpty() ||
            it.name.contains(trimmedQuery, ignoreCase = true) ||
            it.email.contains(trimmedQuery, ignoreCase = true)
    }
    val otherResults = if (searchingDirectory) {
        val contactIds = contacts.map { it.id }.toSet()
        searchResults.filter { it.id !in contactIds }
    } else {
        emptyList()
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    fun hideKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    var menuContact by remember { mutableStateOf<ContactSummary?>(null) }
    var pendingRemove by remember { mutableStateOf<ContactSummary?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val listState = rememberLazyListState()
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) hideKeyboard()
    }

    fun openContactMenu(contact: ContactSummary) {
        hideKeyboard()
        menuContact = contact
    }

    menuContact?.let { contact ->
        ModalBottomSheet(
            onDismissRequest = { menuContact = null },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                SheetHandle()
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
                SheetOptionRow(
                    label = "Xem thông tin chi tiết",
                    icon = Icons.Outlined.Info,
                    onClick = {
                        menuContact = null
                        onViewContactDetail(contact)
                    },
                )
                SheetOptionRow(
                    label = "Xoá khỏi danh bạ",
                    icon = Icons.Outlined.PersonRemove,
                    destructive = true,
                    onClick = {
                        menuContact = null
                        pendingRemove = contact
                    },
                )
                HorizontalDivider()
                SheetCancelRow(onClick = { menuContact = null })
            }
        }
    }

    pendingRemove?.let { contact ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Xóa khỏi danh bạ?") },
            text = {
                Text("Gỡ ${contact.name} khỏi danh bạ của bạn. Bạn vẫn có thể tìm và chat lại sau.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRemove = null
                        onRemoveContact(contact)
                    },
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) {
                    Text("Hủy")
                }
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        KChatSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Tìm danh bạ hoặc người dùng...",
            modifier = Modifier.padding(KChatDimens.screenPadding),
            keyboardActions = KeyboardActions(onSearch = { hideKeyboard() }),
        )
        if (actionError != null) {
            Text(
                text = actionError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = KChatDimens.screenPadding),
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        hideKeyboard()
                    }
                },
        ) {
            if (!searchingDirectory) {
                if (filteredContacts.isEmpty()) {
                    item {
                        ContactsEmptyState(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                        )
                    }
                } else {
                    items(filteredContacts, key = { "c-${it.id}" }) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = {
                                hideKeyboard()
                                onContactClick(contact)
                            },
                            onLongClick = { openContactMenu(contact) },
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                item {
                    SectionHeader("Trong danh bạ")
                }
                if (filteredContacts.isEmpty()) {
                    item {
                        EmptyHint("Không có liên hệ khớp \"$trimmedQuery\"")
                    }
                } else {
                    items(filteredContacts, key = { "c-${it.id}" }) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = {
                                hideKeyboard()
                                onContactClick(contact)
                            },
                            onLongClick = { openContactMenu(contact) },
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    SectionHeader("Người dùng khác")
                }
                when {
                    isSearching -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(KChatDimens.screenPadding),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                                Text(
                                    text = "Đang tìm...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    searchError != null -> {
                        item { EmptyHint(searchError) }
                    }
                    otherResults.isEmpty() -> {
                        item { EmptyHint("Không tìm thấy người dùng khác") }
                    }
                    else -> {
                        items(otherResults, key = { "s-${it.id}" }) { contact ->
                            ContactRow(
                                contact = contact,
                                action = if (contact.isContact) {
                                    ContactRowAction.Added
                                } else {
                                    ContactRowAction.Add
                                },
                                onAction = {
                                    hideKeyboard()
                                    onAddContact(contact)
                                },
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
    }
}

@Composable
private fun ContactsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = KChatDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Danh bạ trống",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tìm người dùng bằng ô tìm kiếm phía trên",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = KChatDimens.screenPadding,
            end = KChatDimens.screenPadding,
            top = 12.dp,
            bottom = 4.dp,
        ),
    )
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(KChatDimens.screenPadding),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactRow(
    contact: ContactSummary,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    action: ContactRowAction? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                },
            )
            .padding(horizontal = KChatDimens.screenPadding, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(name = contact.name, imageUrl = contact.avatarUrl, isOnline = contact.isOnline)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = contact.subtitle.ifBlank { contact.email },
                style = MaterialTheme.typography.bodyMedium,
                color = if (contact.isOnline) {
                    KChatColors.online
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (action != null && onAction != null) {
            val (icon, contentDescription, enabled, tint) = when (action) {
                ContactRowAction.Add -> ContactActionVisual(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Thêm vào danh bạ",
                    enabled = true,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                )
                ContactRowAction.Added -> ContactActionVisual(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Đã thêm",
                    enabled = false,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
            IconButton(onClick = onAction, enabled = enabled) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                )
            }
        }
    }
}

private data class ContactActionVisual(
    val icon: ImageVector,
    val contentDescription: String,
    val enabled: Boolean,
    val tint: androidx.compose.ui.graphics.Color,
)
