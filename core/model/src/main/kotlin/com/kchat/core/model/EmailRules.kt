package com.kchat.core.model

object EmailRules {
    private val pattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    const val MAX_LENGTH = 255

    /** Synthetic domain for legacy username-only accounts (login/reset still relevant). */
    const val USERNAME_ONLY_SUFFIX = "@register.kchat.internal"

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
