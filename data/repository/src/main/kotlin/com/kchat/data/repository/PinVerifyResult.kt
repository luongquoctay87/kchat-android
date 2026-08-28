package com.kchat.data.repository

enum class PinVerifyResult {
    Unlocked,
    /** Correct emergency code — wipe data and show a fake PIN error. */
    EmergencyWipeMessages,
    Wrong,
}
