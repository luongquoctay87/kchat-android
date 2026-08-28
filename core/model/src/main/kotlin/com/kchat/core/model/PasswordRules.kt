package com.kchat.core.model

/** Aligned with backend [com.kchat.common.validation.PasswordRules]. */
object PasswordRules {
    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 128
    const val HINT =
        "Ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"

    private val pattern =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{$MIN_LENGTH,$MAX_LENGTH}$")

    fun isStrong(password: String): Boolean = pattern.matches(password)

    fun validate(password: String): String? = when {
        password.length < MIN_LENGTH ->
            "Mật khẩu phải có ít nhất $MIN_LENGTH ký tự"
        password.length > MAX_LENGTH ->
            "Mật khẩu tối đa $MAX_LENGTH ký tự"
        password.none { it in 'a'..'z' } ->
            "Mật khẩu phải có ít nhất một chữ thường"
        password.none { it in 'A'..'Z' } ->
            "Mật khẩu phải có ít nhất một chữ hoa"
        password.none { it in '0'..'9' } ->
            "Mật khẩu phải có ít nhất một chữ số"
        password.none { it !in 'a'..'z' && it !in 'A'..'Z' && it !in '0'..'9' } ->
            "Mật khẩu phải có ít nhất một ký tự đặc biệt"
        !isStrong(password) -> HINT
        else -> null
    }
}
