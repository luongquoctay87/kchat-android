package com.kchat.core.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.ChatMessage
import com.kchat.core.model.ReactionCount
import com.kchat.core.model.MessageType
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.replyQuoteTextFor
import com.kchat.core.model.withEnrichedReply
import com.kchat.core.model.RoomMeta
import com.kchat.core.ui.ChatListEntry
import com.kchat.core.ui.KChatBackButton
import com.kchat.core.ui.KChatTopBar
import com.kchat.core.ui.buildChatListEntries
import com.kchat.core.ui.components.ChannelReadOnlyBar
import com.kchat.core.ui.components.ChatInputBar
import com.kchat.core.ui.components.EditPreviewBar
import com.kchat.core.ui.components.MentionPickerOverlay
import com.kchat.core.ui.components.MessageActionSheet
import com.kchat.core.ui.components.MessageBubble
import com.kchat.core.ui.components.PinnedBanner
import com.kchat.core.ui.components.RadioOptionRow
import com.kchat.core.ui.components.ReadReceiptSheet
import com.kchat.core.ui.components.ReplyPreviewBar
import com.kchat.core.ui.components.TypingIndicatorBubble
import com.kchat.core.ui.components.SheetCancelRow
import com.kchat.core.ui.components.SheetHandle
import com.kchat.core.ui.components.SheetOptionRow
import com.kchat.core.ui.demo.SampleData
import com.kchat.core.ui.media.ImagePickerSheetContent
import com.kchat.core.ui.media.PickAnyFile

private const val EDIT_WINDOW_MS = 15 * 60 * 1000L

private enum class ChatSheet {
    Attach,
    ImagePicker,
    Menu,
    Mute,
    Disappearing,
    MessageActions,
    ReadReceipts,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    roomId: String,
    roomTitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    roomMeta: RoomMeta = SampleData.roomMeta(roomId),
    messages: List<ChatMessage> = SampleData.messagesForRoom(roomId),
    onSendMessage: ((String, ReplyQuote?) -> Unit)? = null,
    /** When send fails, restore this text into the composer. */
    restoreDraft: String? = null,
    restoreReply: ReplyQuote? = null,
    onRestoreDraftConsumed: () -> Unit = {},
    onEditMessage: (messageId: String, text: String) -> Unit = { _, _ -> },
    onDeleteMessage: (messageId: String) -> Unit = {},
    onReactMessage: (messageId: String, emoji: String) -> Unit = { _, _ -> },
    onSendMedia: (android.net.Uri, String?, String?) -> Unit = { _, _, _ -> },
    onInputChanged: (String) -> Unit = {},
    mentionUsersProvider: (String) -> List<com.kchat.core.model.MentionUser> = { query ->
        SampleData.mentionUsers.filter {
            query.isBlank() || it.username.startsWith(query, ignoreCase = true)
        }
    },
    readReceipts: List<com.kchat.core.model.ReadReceipt> = SampleData.readReceipts,
    onLoadReadReceipts: (messageId: String) -> Unit = {},
    pinnedMessage: String? = if (roomId == "room-2") SampleData.pinnedMessage else null,
    pinnedMessageId: String? = null,
    onPinMessage: (messageId: String) -> Unit = {},
    onUnpinMessage: () -> Unit = {},
    onOpenGroupInfo: () -> Unit = {},
    onOpenImage: (url: String, title: String) -> Unit = { _, _ -> },
    onOpenFile: (url: String, fileName: String) -> Unit = { _, _ -> },
    onOpenSearch: () -> Unit = {},
    onOpenCall: (CallType) -> Unit = {},
    onDisappearingChange: (seconds: Int?) -> Unit = {},
    onMuteChange: (durationSeconds: Int) -> Unit = {},
    scrollToMessageId: String? = null,
    onScrollToMessageConsumed: () -> Unit = {},
    onEnsureMessageLoaded: (messageId: String) -> Unit = {},
    enterToSend: Boolean = true,
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var localMessages by remember { mutableStateOf(messages) }
    LaunchedEffect(messages) {
        localMessages = messages
    }
    val pickFile = rememberLauncherForActivityResult(PickAnyFile()) { uri ->
        uri?.let { onSendMedia(it, null, null) }
    }
    var activeSheet by remember { mutableStateOf<ChatSheet?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetScope = rememberCoroutineScope()

    fun closeSheet() {
        sheetScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                activeSheet = null
            }
        }
    }

    fun dismissSheetRequest() {
        when (activeSheet) {
            ChatSheet.Disappearing, ChatSheet.Mute -> activeSheet = ChatSheet.Menu
            else -> closeSheet()
        }
    }
    var disappearingOption by remember {
        mutableIntStateOf(disappearingIndex(roomMeta.disappearingAfterSeconds))
    }
    LaunchedEffect(roomMeta.disappearingAfterSeconds) {
        disappearingOption = disappearingIndex(roomMeta.disappearingAfterSeconds)
    }
    var replyTarget by remember { mutableStateOf<ReplyQuote?>(null) }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }
    var pinnedText by remember(pinnedMessage) { mutableStateOf(pinnedMessage) }
    var pinnedId by remember { mutableStateOf(pinnedMessageId) }
    LaunchedEffect(pinnedMessage) {
        pinnedText = pinnedMessage
    }
    LaunchedEffect(pinnedMessageId) {
        pinnedMessageId?.let { pinnedId = it }
    }
    var scrollTargetId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(restoreDraft) {
        val draft = restoreDraft ?: return@LaunchedEffect
        input = TextFieldValue(draft, TextRange(draft.length))
        replyTarget = restoreReply
        onInputChanged(draft)
        onRestoreDraftConsumed()
    }
    val disappearingLabels = listOf("Tắt", "24 giờ", "7 ngày", "30 ngày", "90 ngày")
    val disappearingSeconds = listOf(null, 86_400, 604_800, 2_592_000, 7_776_000)

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    fun hideKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    // Keep viewport pinned to newest only when already at bottom and a new message arrives.
    // Never fight search jump / highlight (and ignore IME size changes — reverseLayout handles that).
    var previousMessageCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(localMessages.size, scrollToMessageId, scrollTargetId, highlightedMessageId) {
        val count = localMessages.size
        val previous = previousMessageCount
        previousMessageCount = count
        val suppressingJump = scrollToMessageId != null || scrollTargetId != null || highlightedMessageId != null
        val grew = count > previous
        val atNewest = listState.firstVisibleItemIndex <= 1
        if (count > 0 && !suppressingJump && grew && atNewest) {
            listState.animateScrollToItem(0)
        }
    }
    // Keep latest visible when keyboard opens while already at newest (reverseLayout).
    LaunchedEffect(imeBottomPx) {
        if (
            localMessages.isNotEmpty() &&
            scrollToMessageId == null &&
            scrollTargetId == null &&
            highlightedMessageId == null &&
            listState.firstVisibleItemIndex <= 1
        ) {
            listState.scrollToItem(0)
        }
    }

    fun openSheet(sheet: ChatSheet) {
        hideKeyboard()
        activeSheet = sheet
    }

    val mentionQuery = remember(input.text) { extractActiveMentionQuery(input.text) }
    val showMentionPicker = mentionQuery != null
    val mentionUsers = if (mentionQuery != null) {
        mentionUsersProvider(mentionQuery)
    } else {
        emptyList()
    }

    val displayTitle = when {
        roomMeta.isChannel -> roomTitle
        roomMeta.isGroup && roomMeta.memberCount > 0 -> "$roomTitle (${roomMeta.memberCount})"
        else -> roomTitle
    }

    val subtitle = when {
        roomMeta.isChannel -> "${roomMeta.memberCount} thành viên"
        roomMeta.isGroup && roomMeta.onlineCount > 0 -> "${roomMeta.onlineCount} trực tuyến"
        roomMeta.isGroup && roomMeta.memberCount > 0 -> "${roomMeta.memberCount} thành viên"
        roomMeta.isOnline -> "trực tuyến"
        else -> null
    }

    val subtitleColor = when {
        roomMeta.isChannel -> MaterialTheme.colorScheme.onSurfaceVariant
        roomMeta.isOnline || (roomMeta.isGroup && roomMeta.onlineCount > 0) ->
            KChatColors.online
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val typingCaption = when {
        roomMeta.isGroup || roomMeta.isChannel -> "đang nhập..."
        else -> "$roomTitle đang nhập..."
    }

    Scaffold(
        modifier = modifier,
        containerColor = KChatColors.chatBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            KChatTopBar(
                title = displayTitle,
                subtitle = subtitle,
                subtitleColor = subtitleColor,
                navigationIcon = { KChatBackButton(onClick = onBack) },
                actions = {
                    if (!roomMeta.isGroup && !roomMeta.isChannel) {
                        IconButton(onClick = {
                            hideKeyboard()
                            onOpenCall(CallType.Voice)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Gọi thoại")
                        }
                        IconButton(onClick = {
                            hideKeyboard()
                            onOpenCall(CallType.Video)
                        }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Gọi video")
                        }
                    }
                    IconButton(onClick = { openSheet(ChatSheet.Menu) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                },
            )
        },
    ) { padding ->
        val displayMessages = remember(localMessages) {
            localMessages.map { it.withEnrichedReply(localMessages) }
        }
        val listEntries = remember(displayMessages) { buildChatListEntries(displayMessages) }
        LaunchedEffect(scrollToMessageId, scrollTargetId, listEntries) {
            val target = scrollToMessageId ?: scrollTargetId ?: return@LaunchedEffect
            val index = listEntries.indexOfFirst { entry ->
                entry is ChatListEntry.Bubble && entry.message.id == target
            }
            if (index < 0) {
                onEnsureMessageLoaded(target)
                return@LaunchedEffect
            }
            hideKeyboard()
            val lazyIndex = index + 1 // +1: leading spacer item
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .filter { it > lazyIndex }
                .first()
            highlightedMessageId = target
            scrollToChatMessage(listState, lazyIndex)
            if (scrollToMessageId != null) {
                onScrollToMessageConsumed()
            } else {
                scrollTargetId = null
            }
        }
        LaunchedEffect(highlightedMessageId) {
            val target = highlightedMessageId ?: return@LaunchedEffect
            delay(5_000)
            if (highlightedMessageId == target) {
                highlightedMessageId = null
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            pinnedText?.let { pinned ->
                PinnedBanner(
                    text = pinned,
                    onClick = pinnedId?.let { id ->
                        {
                            hideKeyboard()
                            scrollTargetId = id
                        }
                    },
                    onDismiss = if (roomMeta.canPost) {
                        {
                            pinnedText = null
                            onUnpinMessage()
                        }
                    } else {
                        null
                    },
                )
            }
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = KChatDimens.screenPadding)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            hideKeyboard()
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "typing-indicator") {
                    TypingIndicatorBubble(
                        visible = roomMeta.showTyping,
                        caption = typingCaption,
                    )
                }
                item { Box(modifier = Modifier.height(4.dp)) }
                items(
                    items = listEntries,
                    key = { entry ->
                        when (entry) {
                            is ChatListEntry.Bubble -> entry.message.id
                            is ChatListEntry.DaySeparator -> "day-${entry.dayKey}"
                        }
                    },
                ) { entry ->
                    when (entry) {
                        is ChatListEntry.Bubble -> {
                            val message = entry.message
                            MessageBubble(
                                message = message,
                                highlighted = message.id == highlightedMessageId,
                                onImageClick = {
                                    hideKeyboard()
                                    val url = message.mediaUrl
                                    if (!url.isNullOrBlank()) {
                                        onOpenImage(url, message.imageLabel ?: message.fileName ?: "Ảnh")
                                    }
                                },
                                onFileClick = {
                                    hideKeyboard()
                                    val url = message.mediaUrl
                                    if (!url.isNullOrBlank()) {
                                        onOpenFile(url, message.fileName ?: "file")
                                    }
                                },
                                onLongClick = {
                                    selectedMessageId = message.id
                                    openSheet(ChatSheet.MessageActions)
                                },
                                onReadReceiptClick = {
                                    if (message.isMine && message.isRead) {
                                        onLoadReadReceipts(message.id)
                                        openSheet(ChatSheet.ReadReceipts)
                                    }
                                },
                                onReactionClick = { emoji -> onReactMessage(message.id, emoji) },
                            )
                        }
                        is ChatListEntry.DaySeparator -> DateSeparator(entry.label)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding(),
            ) {
                if (showMentionPicker && mentionUsers.isNotEmpty()) {
                    MentionPickerOverlay(
                        users = mentionUsers,
                        onSelect = { user ->
                            val newText = replaceActiveMention(input.text, user.username)
                            input = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length),
                            )
                        },
                    )
                }
                replyTarget?.let { ReplyPreviewBar(it, onDismiss = { replyTarget = null }) }
                editingMessageId?.let {
                    EditPreviewBar(
                        previewText = input.text,
                        onDismiss = {
                            editingMessageId = null
                            input = TextFieldValue("")
                        },
                    )
                }
                if (!roomMeta.canPost) {
                    ChannelReadOnlyBar()
                } else {
                    ChatInputBar(
                        value = input,
                        enterToSend = enterToSend,
                        onValueChange = {
                            input = it
                            if (editingMessageId == null) onInputChanged(it.text)
                        },
                        onSend = {
                            if (input.text.isNotBlank()) {
                                val text = input.text.trim()
                                val editId = editingMessageId
                                if (editId != null) {
                                    onEditMessage(editId, text)
                                    if (onSendMessage == null) {
                                        localMessages = localMessages.map { msg ->
                                            if (msg.id == editId) msg.copy(text = text, isEdited = true) else msg
                                        }
                                    }
                                    editingMessageId = null
                                } else {
                                    val reply = replyTarget
                                    if (onSendMessage != null) {
                                        onSendMessage(text, reply)
                                    } else {
                                        localMessages = localMessages + ChatMessage(
                                            id = "local-${localMessages.size}",
                                            text = text,
                                            isMine = true,
                                            time = "vừa xong",
                                            createdAtMillis = System.currentTimeMillis(),
                                            replyTo = reply,
                                        )
                                    }
                                    replyTarget = null
                                }
                                input = TextFieldValue("")
                            }
                        },
                        onAttachClick = { openSheet(ChatSheet.Attach) },
                    )
                }
            }
        }
    }

    if (activeSheet != null) {
        LaunchedEffect(activeSheet) {
            sheetState.show()
        }
        BackHandler(onBack = ::dismissSheetRequest)
        ModalBottomSheet(
            onDismissRequest = ::dismissSheetRequest,
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                SheetHandle()
                when (activeSheet) {
                    ChatSheet.Attach -> {
                        SheetOptionRow(
                            label = "Thư viện ảnh",
                            icon = Icons.Default.PhotoLibrary,
                            onClick = { activeSheet = ChatSheet.ImagePicker },
                        )
                        SheetOptionRow(
                            label = "Chọn tệp tin",
                            icon = Icons.Default.InsertDriveFile,
                            onClick = {
                                activeSheet = null
                                pickFile.launch(Unit)
                            },
                        )
                        HorizontalDivider()
                        SheetCancelRow(onClick = ::closeSheet)
                    }
                    ChatSheet.ImagePicker -> {
                        ImagePickerSheetContent(
                            onImagePicked = { uri ->
                                onSendMedia(uri, null, null)
                                closeSheet()
                            },
                            onCancel = ::closeSheet,
                        )
                    }
                    ChatSheet.Menu -> {
                        if (roomMeta.isMuted) {
                            SheetOptionRow(
                                label = "Bật lại thông báo room",
                                icon = Icons.Default.NotificationsOff,
                                onClick = {
                                    onMuteChange(com.kchat.core.model.RoomMuteDurations.UNMUTE)
                                    closeSheet()
                                },
                            )
                        } else {
                            SheetOptionRow(
                                label = "Tắt thông báo room",
                                icon = Icons.Default.NotificationsOff,
                                onClick = { activeSheet = ChatSheet.Mute },
                            )
                        }
                        if (!roomMeta.isChannel) {
                            SheetOptionRow(
                                label = "Tin biến mất",
                                icon = Icons.Default.HourglassBottom,
                                onClick = { activeSheet = ChatSheet.Disappearing },
                            )
                        }
                        if (roomMeta.isGroup) {
                            SheetOptionRow(
                                label = "Thông tin nhóm",
                                icon = Icons.Outlined.Info,
                                onClick = {
                                    closeSheet()
                                    onOpenGroupInfo()
                                },
                            )
                        }
                        if (roomMeta.isChannel) {
                            SheetOptionRow(
                                label = "Thông tin kênh",
                                icon = Icons.Outlined.Info,
                                onClick = {
                                    closeSheet()
                                    onOpenGroupInfo()
                                },
                            )
                        }
                        SheetOptionRow(
                            label = "Tìm trong cuộc trò chuyện",
                            icon = Icons.Default.Search,
                            onClick = {
                                closeSheet()
                                onOpenSearch()
                            },
                        )
                        SheetCancelRow(onClick = ::closeSheet)
                    }
                    ChatSheet.Mute -> {
                        Text(
                            "Tắt thông báo room",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                        Text(
                            "Bạn vẫn nhận tin trong app; chỉ tắt thông báo đẩy khi có FCM.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        )
                        listOf(
                            "8 giờ" to com.kchat.core.model.RoomMuteDurations.EIGHT_HOURS,
                            "1 tuần" to com.kchat.core.model.RoomMuteDurations.ONE_WEEK,
                            "Vĩnh viễn" to com.kchat.core.model.RoomMuteDurations.FOREVER,
                        ).forEach { (label, seconds) ->
                            SheetOptionRow(
                                label = label,
                                icon = Icons.Default.NotificationsOff,
                                onClick = {
                                    onMuteChange(seconds)
                                    closeSheet()
                                },
                            )
                        }
                        SheetCancelRow(onClick = { activeSheet = ChatSheet.Menu })
                    }
                    ChatSheet.Disappearing -> {
                        Text(
                            "Tin biến mất",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                        if (!roomMeta.canChangeDisappearing) {
                            Text(
                                "Chỉ owner/admin mới đổi được tuỳ chọn này.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            )
                        }
                        disappearingLabels.forEachIndexed { index, label ->
                            RadioOptionRow(
                                label = label,
                                selected = disappearingOption == index,
                                onClick = {
                                    if (!roomMeta.canChangeDisappearing) return@RadioOptionRow
                                    disappearingOption = index
                                    onDisappearingChange(disappearingSeconds[index])
                                    closeSheet()
                                },
                            )
                        }
                        SheetCancelRow(onClick = { activeSheet = ChatSheet.Menu })
                    }
                    ChatSheet.MessageActions -> {
                        val selected = selectedMessageId?.let { id -> localMessages.find { it.id == id } }
                        if (selected != null) {
                            val withinWindow = selected.createdAtMillis
                                ?.let { System.currentTimeMillis() - it <= EDIT_WINDOW_MS }
                                ?: true
                            val canModify = selected.isMine && withinWindow
                            MessageActionSheet(
                                canEdit = canModify &&
                                    selected.type == com.kchat.core.model.MessageType.Text &&
                                    selected.text.isNotBlank(),
                                canDelete = canModify,
                                canPin = roomMeta.canPost,
                                myReactionEmojis = selected.reactions
                                    .filter { it.reactedByMe }
                                    .map { it.emoji }
                                    .toSet(),
                                onReaction = { emoji ->
                                    onReactMessage(selected.id, emoji)
                                    if (onSendMessage == null) {
                                        localMessages = localMessages.map { msg ->
                                            if (msg.id == selected.id) applyLocalReactionToggle(msg, emoji) else msg
                                        }
                                    }
                                    closeSheet()
                                },
                                onReply = {
                                    editingMessageId = null
                                    replyTarget = ReplyQuote(
                                        author = if (selected.isMine) "Bạn" else (selected.senderName ?: roomTitle),
                                        text = replyQuoteTextFor(selected),
                                        messageId = selected.id,
                                        mediaUrl = if (selected.type == MessageType.Image) selected.mediaUrl else null,
                                        previewType = when (selected.type) {
                                            MessageType.Image -> MessageType.Image
                                            MessageType.File -> MessageType.File
                                            else -> null
                                        },
                                    )
                                    closeSheet()
                                },
                                onPin = {
                                    pinnedId = selected.id
                                    onPinMessage(selected.id)
                                    pinnedText = selected.text.ifBlank { selected.fileName ?: "Tin nhắn" }
                                    closeSheet()
                                },
                                onEdit = {
                                    replyTarget = null
                                    editingMessageId = selected.id
                                    input = TextFieldValue(
                                        text = selected.text,
                                        selection = TextRange(selected.text.length),
                                    )
                                    closeSheet()
                                },
                                onDelete = {
                                    onDeleteMessage(selected.id)
                                    if (editingMessageId == selected.id) {
                                        editingMessageId = null
                                        input = TextFieldValue("")
                                    }
                                    if (onSendMessage == null) {
                                        localMessages = localMessages.filterNot { it.id == selected.id }
                                    }
                                    closeSheet()
                                },
                            )
                        }
                    }
                    ChatSheet.ReadReceipts -> {
                        ReadReceiptSheet(receipts = readReceipts)
                        SheetCancelRow(onClick = ::closeSheet)
                    }
                    null -> Unit
                }
                Box(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DateSeparator(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

private fun applyLocalReactionToggle(message: ChatMessage, emoji: String): ChatMessage {
    val existing = message.reactions.toMutableList()
    val idx = existing.indexOfFirst { it.emoji == emoji }
    if (idx >= 0 && existing[idx].reactedByMe) {
        val next = existing[idx].count - 1
        if (next <= 0) existing.removeAt(idx)
        else existing[idx] = existing[idx].copy(count = next, reactedByMe = false)
    } else if (idx >= 0) {
        existing[idx] = existing[idx].copy(count = existing[idx].count + 1, reactedByMe = true)
    } else {
        existing.add(ReactionCount(emoji, 1, reactedByMe = true))
    }
    return message.copy(reactions = existing)
}

private suspend fun scrollToChatMessage(listState: LazyListState, lazyIndex: Int) {
    listState.scrollToItem(lazyIndex)
    val viewportHeight = listState.layoutInfo.viewportSize.height
    if (viewportHeight > 0) {
        listState.animateScrollToItem(
            index = lazyIndex,
            scrollOffset = -(viewportHeight / 3),
        )
    } else {
        listState.animateScrollToItem(lazyIndex)
    }
}

/** Active @mention query at the caret-end style token (last @… without spaces). */
private fun extractActiveMentionQuery(input: String): String? {
    val at = input.lastIndexOf('@')
    if (at < 0) return null
    if (at > 0 && !input[at - 1].isWhitespace()) return null
    val after = input.substring(at + 1)
    if (after.any { it.isWhitespace() }) return null
    return after
}

private fun replaceActiveMention(input: String, username: String): String {
    val at = input.lastIndexOf('@')
    if (at < 0) return input
    return input.substring(0, at) + "@$username "
}

private fun disappearingIndex(seconds: Int?): Int = when (seconds) {
    null, 0 -> 0
    86_400 -> 1
    604_800 -> 2
    2_592_000 -> 3
    7_776_000 -> 4
    else -> 0
}
