package com.kchat.core.model

/**
 * Validates registration form fields aligned with backend [RegisterRequest] rules.
 * Email is required (OTP); username is entered by the user (optional suggest from email).
 */
object RegisterValidation {
    private val usernamePattern = Regex("^[a-zA-Z0-9_]{3,64}$")

    const val USERNAME_HINT = "3–64 ký tự: chữ, số hoặc _"
    const val DISPLAY_NAME_MAX = 128

    data class Parsed(
        val displayName: String,
        val email: String,
        val username: String,
    )

    data class FieldErrors(
        val displayName: String? = null,
        val email: String? = null,
        val username: String? = null,
        val password: String? = null,
        val confirm: String? = null,
    ) {
        val hasAny: Boolean
            get() = displayName != null || email != null || username != null ||
                password != null || confirm != null
    }

    fun canSubmit(
        displayName: String,
        email: String,
        username: String,
        password: String,
        confirm: String,
    ): Boolean = validate(displayName, email, username, password, confirm) == null

    fun validate(
        displayName: String,
        email: String,
        username: String,
        password: String,
        confirm: String,
    ): String? {
        validateDisplayName(displayName)?.let { return it }
        EmailRules.validateForRegistration(email)?.let { return it }
        validateUsername(username)?.let { return it }
        PasswordRules.validate(password)?.let { return it }
        if (password != confirm) return "Mật khẩu xác nhận không khớp"
        return null
    }

    /** Per-field errors for UI; prefer this over a single banner when possible. */
    fun validateFields(
        displayName: String,
        email: String,
        username: String,
        password: String,
        confirm: String,
    ): FieldErrors {
        val confirmError = when {
            confirm.isEmpty() -> "Vui lòng xác nhận mật khẩu"
            password != confirm -> "Mật khẩu xác nhận không khớp"
            else -> null
        }
        return FieldErrors(
            displayName = validateDisplayName(displayName),
            email = EmailRules.validateForRegistration(email),
            username = validateUsername(username),
            password = PasswordRules.validate(password),
            confirm = confirmError,
        )
    }

    fun validateDisplayName(displayName: String): String? {
        val name = displayName.trim()
        return when {
            name.isBlank() -> "Tên hiển thị không được để trống"
            name.length > DISPLAY_NAME_MAX -> "Tên hiển thị tối đa $DISPLAY_NAME_MAX ký tự"
            else -> null
        }
    }

    fun validateUsername(username: String): String? {
        val value = username.trim()
        return when {
            value.isBlank() -> "Username không được để trống"
            value.length < 3 -> "Username phải có ít nhất 3 ký tự"
            value.length > 64 -> "Username tối đa 64 ký tự"
            !usernamePattern.matches(value) -> "Username chỉ gồm chữ, số và _"
            else -> null
        }
    }

    fun parse(displayName: String, email: String, username: String): Parsed {
        return Parsed(
            displayName = displayName.trim(),
            email = email.trim().lowercase(),
            username = username.trim(),
        )
    }

    fun suggestUsernameFromEmail(email: String): String {
        val localPart = email.substringBefore('@').lowercase()
        val candidate = localPart
            .replace(Regex("[^a-z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(64)
        return if (usernamePattern.matches(candidate)) candidate else ""
    }

    /** Keep only ASCII characters allowed by backend username rules. */
    fun sanitizeUsernameInput(raw: String): String =
        buildString(raw.length.coerceAtMost(64)) {
            for (c in raw) {
                if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '_') {
                    append(c)
                    if (length >= 64) break
                }
            }
        }
}
