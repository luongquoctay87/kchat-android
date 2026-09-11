package com.kchat.core.model

object EmailRules {
    private val pattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    const val MAX_LENGTH = 255

    /** Synthetic domain for legacy username-only accounts (login/reset still relevant). */
    const val USERNAME_ONLY_SUFFIX = "@register.kchat.internal"

    /**
     * Keep only common email characters while typing.
     * At most one `@` is allowed.
     */
    fun sanitizeEmailInput(raw: String): String {
        val out = StringBuilder(raw.length.coerceAtMost(MAX_LENGTH))
        var sawAt = false
        for (c in raw) {
            val allowed = c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' ||
                c == '.' || c == '_' || c == '%' || c == '+' || c == '-' || c == '@'
            if (!allowed) continue
            if (c == '@') {
                if (sawAt) continue
                sawAt = true
            }
            out.append(c)
            if (out.length >= MAX_LENGTH) break
        }
        return out.toString()
    }

    fun validate(email: String): String? {
        val normalized = email.trim()
        return when {
            normalized.isBlank() -> "Vui lòng nhập email"
            normalized.length > MAX_LENGTH -> "Email tối đa $MAX_LENGTH ký tự"
            normalized.lowercase().endsWith(USERNAME_ONLY_SUFFIX) ->
                "Tài khoản chỉ có tên đăng nhập — liên hệ quản trị viên"
            !pattern.matches(normalized) -> "Email không hợp lệ"
            else -> null
        }
    }

    fun validateForRegistration(email: String): String? {
        val normalized = email.trim()
        return when {
            normalized.isBlank() -> "Vui lòng nhập email"
            normalized.length > MAX_LENGTH -> "Email tối đa $MAX_LENGTH ký tự"
            normalized.lowercase().endsWith(USERNAME_ONLY_SUFFIX) -> "Email không hợp lệ"
            !pattern.matches(normalized) -> "Email không hợp lệ"
            else -> null
        }
    }
}
