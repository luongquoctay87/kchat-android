package com.kchat.core.model

/**
 * Search box input rules: letters (incl. Vietnamese), digits, spaces, common
 * punctuation for name/email/username/message keywords — no emoji or symbols.
 */
object SearchQueryRules {
    const val MAX_LENGTH = 64

    fun sanitize(raw: String, maxLength: Int = MAX_LENGTH): String {
        if (raw.isEmpty()) return raw
        val out = StringBuilder(raw.length.coerceAtMost(maxLength))
        var i = 0
        while (i < raw.length && out.length < maxLength) {
            val cp = raw.codePointAt(i)
            if (isAllowedCodePoint(cp)) {
                out.appendCodePoint(cp)
            }
            i += Character.charCount(cp)
        }
        return out.toString()
    }

    private fun isAllowedCodePoint(cp: Int): Boolean {
        // Emoji / pictographs are typically OTHER_SYMBOL or similar — not letters/digits.
        return when (Character.getType(cp)) {
            Character.UPPERCASE_LETTER.toInt(),
            Character.LOWERCASE_LETTER.toInt(),
            Character.TITLECASE_LETTER.toInt(),
            Character.MODIFIER_LETTER.toInt(),
            Character.OTHER_LETTER.toInt(),
            Character.DECIMAL_DIGIT_NUMBER.toInt(),
            Character.LETTER_NUMBER.toInt(),
            Character.SPACE_SEPARATOR.toInt(),
            Character.CONNECTOR_PUNCTUATION.toInt(),
            Character.DASH_PUNCTUATION.toInt(),
            Character.START_PUNCTUATION.toInt(),
            Character.END_PUNCTUATION.toInt(),
            Character.INITIAL_QUOTE_PUNCTUATION.toInt(),
            Character.FINAL_QUOTE_PUNCTUATION.toInt(),
            Character.OTHER_PUNCTUATION.toInt(),
            -> true
            else -> false
        }
    }
}
