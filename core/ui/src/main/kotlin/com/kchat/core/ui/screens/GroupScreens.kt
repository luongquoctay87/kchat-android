package com.kchat.core.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.GroupMember
import com.kchat.core.model.RoomSummary
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.components.KChatPrimaryButton
import com.kchat.core.ui.components.KChatSearchField
import com.kchat.core.ui.components.KChatTextField
import com.kchat.core.ui.components.UserAvatar
import kotlinx.coroutines.launch

@Composable
fun CreateGroupScreen(
    contacts: List<ContactSummary>,
    creating: Boolean,
    error: String?,
    onBack: () -> Unit,
    onCreate: suspend (name: String, memberIds: List<String>) -> Result<RoomSummary>,
    onCreated: (roomId: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var groupName by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var localError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val filtered = contacts.filter {
        it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
    }

    KChatDetailScaffold(
        modifier = modifier,
        title = "Tạo nhóm",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            KChatTextField(value = groupName, onValueChange = { groupName = it }, label = "Tên nhóm")
            Spacer(modifier = Modifier.height(12.dp))
            KChatSearchField(value = query, onValueChange = { query = it }, placeholder = "Tìm thành viên...")
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filtered, key = { it.id }) { contact ->
                    MemberSelectRow(
                        contact = contact,
                        selected = contact.id in selected,
                        onToggle = {
                            selected = if (contact.id in selected) {
                                selected - contact.id
                            } else {
                                selected + contact.id
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
            val message = localError ?: error
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            KChatPrimaryButton(
                text = if (creating) "Đang tạo..." else "Tạo nhóm",
                onClick = {
                    scope.launch {
                        localError = null
                        onCreate(groupName.trim(), selected.toList())
                            .onSuccess { room -> onCreated(room.id, room.title) }
                            .onFailure { localError = it.message ?: "Tạo nhóm thất bại" }
                    }
                },
                enabled = !creating && groupName.isNotBlank() && selected.isNotEmpty(),
            )
        }
    }
}

@Composable
private fun MemberSelectRow(
    contact: ContactSummary,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        UserAvatar(name = contact.name, imageUrl = contact.avatarUrl, isOnline = contact.isOnline)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun GroupInfoScreen(
    groupName: String,
    members: List<GroupMember>,
    contacts: List<ContactSummary>,
    loading: Boolean,
    busy: Boolean,
    error: String?,
    onBack: () -> Unit,
    onAddMembers: suspend (List<String>) -> Result<Unit>,
    onRemoveMember: suspend (String) -> Result<Unit>,
    onLeave: suspend () -> Result<Unit>,
    onLeft: () -> Unit,
    modifier: Modifier = Modifier,
    isChannel: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var menuMemberId by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val me = members.find { it.isMe }
    val isOwner = me?.role == "owner"
    val canManage = !isChannel && (isOwner || me?.role == "admin")
    val memberIds = members.map { it.id }.toSet()

    KChatDetailScaffold(
        modifier = modifier,
        title = if (isChannel) "Thông tin kênh" else "Thông tin nhóm",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserAvatar(
                name = if (isChannel) "#" else groupName,
                size = KChatDimens.avatarLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (isChannel) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chỉ admin được gửi tin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Thành viên (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (canManage) {
                    IconButton(onClick = { showAdd = true }, enabled = !busy) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm thành viên")
                    }
                }
            }
            if (loading && members.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(members, key = { it.id }) { member ->
                    val canRemove = canManage &&
                        !member.isMe &&
                        member.role != "owner" &&
                        (isOwner || member.role != "admin")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UserAvatar(name = member.name, isOnline = member.isOnline)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodyLarge)
                            roleLabel(member.role)?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (canRemove) {
                            IconButton(
                                onClick = { menuMemberId = member.id },
                                enabled = !busy,
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                            }
                            DropdownMenu(
                                expanded = menuMemberId == member.id,
                                onDismissRequest = { menuMemberId = null },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Xóa khỏi nhóm") },
                                    onClick = {
                                        menuMemberId = null
                                        scope.launch {
                                            onRemoveMember(member.id)
                                                .onFailure { actionError = it.message }
                                        }
                                    },
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
            val message = actionError ?: error
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (!isChannel) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            onLeave()
                                .onSuccess { onLeft() }
                                .onFailure { actionError = it.message ?: "Rời nhóm thất bại" }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (busy) "Đang xử lý..." else "Rời nhóm",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddMembersDialog(
            contacts = contacts.filter { it.id !in memberIds },
            onDismiss = { if (!busy) showAdd = false },
            onConfirm = { ids ->
                scope.launch {
                    onAddMembers(ids)
                        .onSuccess { showAdd = false }
                        .onFailure { actionError = it.message }
                }
            },
        )
    }
}

private fun roleLabel(role: String?): String? = when (role) {
    "owner" -> "Chủ nhóm"
    "admin" -> "Admin"
    else -> null
}

@Composable
private fun AddMembersDialog(
    contacts: List<ContactSummary>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var selected by remember { mutableStateOf(setOf<String>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thành viên") },
        text = {
            if (contacts.isEmpty()) {
                Text("Không còn ai để thêm")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(contacts, key = { it.id }) { contact ->
                        MemberSelectRow(
                            contact = contact,
                            selected = contact.id in selected,
                            onToggle = {
                                selected = if (contact.id in selected) {
                                    selected - contact.id
                                } else {
                                    selected + contact.id
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selected.toList()) },
                enabled = selected.isNotEmpty(),
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}
