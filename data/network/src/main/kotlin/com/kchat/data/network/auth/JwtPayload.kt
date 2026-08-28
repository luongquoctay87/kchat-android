package com.kchat.data.network.auth

import android.util.Base64
import org.json.JSONObject

internal object JwtPayload {
    /** JWT `sub` claim (user id) without signature verification — token is already ours. */
    fun subject(accessToken: String?): String? {
        if (accessToken.isNullOrBlank()) return null
        val parts = accessToken.split('.')
        if (parts.size < 2) return null
        return runCatching {
            val decoded = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            )
            JSONObject(String(decoded, Charsets.UTF_8))
                .optString("sub")
                .trim()
                .lowercase()
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
