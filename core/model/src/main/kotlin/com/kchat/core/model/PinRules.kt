package com.kchat.core.model

object PinRules {
    const val LENGTH = 4

    fun validate(pin: String): String? = when {
        pin.length != LENGTH || pin.any { !it.isDigit() } ->
            "Mã phải gồm đúng $LENGTH chữ số"
        else -> null
    }

    fun validateDistinct(primary: String, secondary: String, secondaryLabel: String): String? =
        if (primary == secondary) "$secondaryLabel phải khác mã PIN" else null
}
