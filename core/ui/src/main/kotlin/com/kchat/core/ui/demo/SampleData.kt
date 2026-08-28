package com.kchat.core.ui.demo

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

/** @deprecated Use repositories; kept for @Preview defaults. */
typealias DemoMessage = ChatMessage

object SampleData {
    val currentUserEmail = "user@company.com"

    val rooms = listOf(
        RoomSummary("room-1", "Nguyễn Văn A", "OK gửi file nhé", "14:32", unreadCount = 3, isOnline = true),
        RoomSummary("room-2", "Team Backend", "Minh: deploy xong", "Hôm qua", isGroup = true, memberCount = 4),
        RoomSummary("room-3", "#ops-alerts", "[Bot] Deploy failed", "09:15", isChannel = true),
    )

    val contacts = listOf(
        ContactSummary("u-1", "Trần Thị B", "Trực tuyến", isOnline = true, email = "b@company.com"),
        ContactSummary("u-2", "Lê Văn C", "2 giờ trước", isOnline = false, email = "c@company.com"),
        ContactSummary("u-3", "Phạm Văn D", "Hôm qua", isOnline = false, email = "d@company.com"),
        ContactSummary("u-4", "Hoàng Thị E", "Trực tuyến", isOnline = true, email = "e@company.com"),
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
        com.kchat.core.model.SearchResult("s1", "Nguyễn Văn A", "10:06", "...gửi file invoice..."),
        com.kchat.core.model.SearchResult("s2", "Bạn", "10:07", "...OK, đang xem invoice..."),
    )

    val devices = listOf(
        DeviceSession("d1", "Pixel 8", "Android 15 · Đang dùng", isCurrent = true),
        DeviceSession("d2", "Samsung S24", "Android 14"),
        DeviceSession("d3", "Pixel Tablet", "Android 15"),
    )

    val groupMembers = listOf(
        GroupMember("me", "Bạn", username = "me", role = "admin", isMe = true),
        GroupMember("u-1", "Trần Thị B", username = "tranthib"),
        GroupMember("u-2", "Lê Văn C", username = "levanc"),
        GroupMember("u-3", "Phạm Văn D", username = "phamvand"),
    )

    const val pinnedMessage = "Link deploy: https://ci.company.com/run/12345"

    fun messagesForRoom(roomId: String): List<ChatMessage> = when (roomId) {
        "room-2" -> listOf(
            ChatMessage("g1", text = "deploy xong rồi", senderName = "Minh", isMine = false, time = "10:01"),
            ChatMessage("g2", text = "OK team", isMine = false, time = "10:01"),
            ChatMessage("g3", text = "Nhận rồi", isMine = true, time = "10:02", replyTo = ReplyQuote("Minh", "deploy xong rồi"), isRead = true),
        )
        "room-3" -> listOf(
            ChatMessage("c1", type = MessageType.Bot, botTitle = "⚠ Deploy failed", botService = "Service: k-chat-api", isMine = false, time = "09:15"),
            ChatMessage("c2", text = "Đang xử lý", senderName = "Admin", isMine = false, time = "09:16"),
        )
        else -> listOf(
            ChatMessage("m1", text = "Chào bạn!", isMine = false, time = "10:01"),
            ChatMessage("m2", text = "Ổn nhé", isMine = true, time = "10:02", isRead = true, readByCount = 2),
            ChatMessage("m3", type = MessageType.Image, imageLabel = "Ảnh", isMine = false, time = "10:05"),
            ChatMessage("m4", type = MessageType.File, fileName = "invoice.pdf", fileSize = "120 KB", isMine = true, time = "10:06", isRead = true),
            ChatMessage("m5", text = "Chúc mừng!", isMine = false, time = "10:10", reactions = listOf(ReactionCount("👍", 2), ReactionCount("❤️", 1))),
        )
    }

    fun roomMeta(roomId: String): RoomMeta = when (roomId) {
        "room-2" -> RoomMeta(isGroup = true, memberCount = 4, onlineCount = 3, myRole = "owner")
        "room-3" -> RoomMeta(isChannel = true, memberCount = 128, myRole = "member")
        "room-1" -> RoomMeta(isOnline = true, showTyping = true)
        else -> RoomMeta()
    }
}
