package com.kchat.core.model

/** Chat-list titles: never keep a generic placeholder when a person name is known. */
object RoomTitles {
    private val placeholders = setOf(
        "chat",
        "direct",
        "cuộc gọi đến",
        "k-chat",
    )

    fun isPlaceholder(title: String?): Boolean {
        val trimmed = title?.trim().orEmpty()
        return trimmed.isEmpty() || trimmed.lowercase() in placeholders
    }

    /** First non-placeholder value, otherwise last non-blank, otherwise "Chat". */
    fun resolve(vararg candidates: String?): String {
        val trimmed = candidates.map { it?.trim().orEmpty() }.filter { it.isNotEmpty() }
        trimmed.firstOrNull { !isPlaceholder(it) }?.let { return it }
        return trimmed.lastOrNull().orEmpty().ifBlank { "Chat" }
    }
}
