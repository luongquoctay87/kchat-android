package com.kchat.core.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Marks a brief leave to system UI (picker / permission / camera) so PIN lock
 * is not applied for that background transition.
 */
interface TransientAppLeave {
    fun begin()
    fun end()
}

val LocalTransientAppLeave = staticCompositionLocalOf<TransientAppLeave?> { null }

inline fun TransientAppLeave?.runWithoutLock(block: () -> Unit) {
    if (this == null) {
        block()
        return
    }
    begin()
    try {
        block()
    } catch (t: Throwable) {
        end()
        throw t
    }
}
