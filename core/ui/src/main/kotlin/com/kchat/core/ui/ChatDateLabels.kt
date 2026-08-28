package com.kchat.core.ui

import com.kchat.core.model.ChatMessage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val chatZone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
private val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

internal sealed interface ChatListEntry {
    data class Bubble(val message: ChatMessage) : ChatListEntry
    data class DaySeparator(val label: String, val dayKey: String) : ChatListEntry
}

internal fun buildChatListEntries(messages: List<ChatMessage>): List<ChatListEntry> {
    if (messages.isEmpty()) return emptyList()
    val newestFirst = messages.asReversed()
    val entries = ArrayList<ChatListEntry>(newestFirst.size * 2)
    for (index in newestFirst.indices) {
        val message = newestFirst[index]
        entries.add(ChatListEntry.Bubble(message))
        val older = newestFirst.getOrNull(index + 1)
        val currentDay = dayOf(message)
        val olderDay = older?.let(::dayOf)
        if (currentDay != null && (older == null || olderDay != currentDay)) {
            entries.add(
                ChatListEntry.DaySeparator(
                    label = dayLabel(currentDay),
                    dayKey = currentDay.toString(),
                ),
            )
        }
    }
    return entries
}

private fun dayOf(message: ChatMessage): LocalDate? {
    val millis = message.createdAtMillis ?: return null
    return Instant.ofEpochMilli(millis).atZone(chatZone).toLocalDate()
}

private fun dayLabel(day: LocalDate, now: LocalDate = LocalDate.now(chatZone)): String = when (day) {
    now -> "Hôm nay"
    now.minusDays(1) -> "Hôm qua"
    else -> day.format(dayMonth)
}
