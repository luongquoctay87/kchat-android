package com.kchat.data.fake

import com.kchat.core.model.ChatMessage
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.DeviceSession
import com.kchat.core.model.GroupMember
import com.kchat.core.model.MentionUser
import com.kchat.core.model.MessageType
import com.kchat.core.model.ReactionCount
import com.kchat.core.model.ReadReceipt
import com.kchat.core.model.ReplyQuote
import com.kchat.core.model.RoomMeta
import com.kchat.core.model.RoomSummary
import com.kchat.core.model.SearchResult

object FakeSampleData {
    val currentUserEmail = "user@company.com"

    val rooms = listOf(
        RoomSummary(
            id = "room-1",
            title = "Nguyễn Văn A",
            preview = "OK gửi file nhé",
            time = "14:32",
            unreadCount = 3,
            isOnline = true,
        ),
        RoomSummary(
            id = "room-2",
            title = "Team Backend",
            preview = "Minh: deploy xong",
            time = "Hôm qua",
            isGroup = true,
            memberCount = 4,
        ),
        RoomSummary(
            id = "room-3",
            title = "#ops-alerts",
            preview = "[Bot] Deploy failed",
            time = "09:15",
            isChannel = true,
            memberCount = 128,
            myRole = "member",
        ),
    )

    val contacts = listOf(
        ContactSummary("u-1", "Trần Thị B", "online", isOnline = true, email = "b@company.com"),
        ContactSummary("u-2", "Lê Văn C", "2 giờ trước", isOnline = false, email = "c@company.com"),
        ContactSummary("u-3", "Phạm Văn D", "Hôm qua", isOnline = false, email = "d@company.com"),
        ContactSummary("u-4", "Hoàng Thị E", "online", isOnline = true, email = "e@company.com"),
    )

    val mentionUsers = listOf(
        MentionUser("minh", "Minh"),
        MentionUser("minhle", "Minh Lê"),
        MentionUser("nguyenva", "Nguyễn Văn A"),
    )

    val readReceipts = listOf(
        ReadReceipt("Trần Thị B", "10:03"),
        ReadReceipt("Lê Văn C", "10:04"),
    )

    val searchResults = listOf(
        SearchResult("s1", "Nguyễn Văn A", "10:06", "...gửi file invoice..."),
        SearchResult("s2", "Bạn", "10:07", "...OK, đang xem invoice..."),
    )

    val devices = listOf(
        DeviceSession("d1", "Pixel 8", "Android 15 · Đang dùng", isCurrent = true),
        DeviceSession("d2", "Samsung S24", "Android 14"),
        DeviceSession("d3", "Pixel Tablet", "Android 15"),
    )

    private val directMessages = listOf(
        ChatMessage("m1", text = "Chào bạn!", isMine = false, time = "10:01"),
        ChatMessage(
            "m2",
            text = "Ổn nhé",
            isMine = true,
            time = "10:02",
            isRead = true,
            readByCount = 2,
        ),
        ChatMessage(
            "m3",
            type = MessageType.Image,
            imageLabel = "Ảnh",
            isMine = false,
            time = "10:05",
        ),
        ChatMessage(
            "m4",
            type = MessageType.File,
            fileName = "invoice.pdf",
            fileSize = "120 KB",
            isMine = true,
            time = "10:06",
            isRead = true,
        ),
        ChatMessage(
            "m5",
            text = "Chúc mừng!",
            isMine = false,
            time = "10:10",
            reactions = listOf(ReactionCount("👍", 2), ReactionCount("❤️", 1)),
        ),
    )

    private val groupMessages = listOf(
        ChatMessage(
            "g1",
            text = "deploy xong rồi",
            senderName = "Minh",
            isMine = false,
            time = "10:01",
        ),
        ChatMessage("g2", text = "OK team", isMine = false, time = "10:01"),
        ChatMessage(
            "g3",
            text = "Nhận rồi",
            isMine = true,
            time = "10:02",
            replyTo = ReplyQuote("Minh", "deploy xong rồi"),
            isRead = true,
        ),
    )

    private val channelMessages = listOf(
        ChatMessage(
            "c1",
            type = MessageType.Bot,
            botTitle = "⚠ Deploy failed",
            botService = "Service: k-chat-api",
            text = "",
            isMine = false,
            time = "09:15",
        ),
        ChatMessage(
            "c2",
            text = "Đang xử lý",
            senderName = "Admin",
            isMine = false,
            time = "09:16",
        ),
    )

    val groupMembers = listOf(
        GroupMember("me", "Bạn", username = "me", role = "admin", isMe = true),
        GroupMember("u-1", "Trần Thị B", username = "tranthib"),
        GroupMember("u-2", "Lê Văn C", username = "levanc"),
        GroupMember("u-3", "Phạm Văn D", username = "phamvand"),
    )

    const val pinnedMessage = "Link deploy: https://ci.company.com/run/12345"

    fun messagesForRoom(roomId: String): List<ChatMessage> = when (roomId) {
        "room-2" -> groupMessages
        "room-3" -> channelMessages
        else -> directMessages
    }

    fun roomMeta(roomId: String): RoomMeta = when (roomId) {
        "room-2" -> RoomMeta(
            isGroup = true,
            memberCount = 4,
            onlineCount = 3,
            myRole = "owner",
            disappearingAfterSeconds = null,
        )
        "room-3" -> RoomMeta(isChannel = true, memberCount = 128, myRole = "member")
        "room-1" -> RoomMeta(isOnline = true, showTyping = true, disappearingAfterSeconds = 86_400)
        else -> RoomMeta()
    }
}
