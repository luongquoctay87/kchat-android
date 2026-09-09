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

    /** Checks whether token is missing, expired, or expiring within [skewSeconds]. */
    fun isExpiredOrExpiring(accessToken: String?, skewSeconds: Long = 60L): Boolean {
        if (accessToken.isNullOrBlank()) return true
        val parts = accessToken.split('.')
        if (parts.size < 2) return true
        return runCatching {
            val decoded = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            )
            val exp = JSONObject(String(decoded, Charsets.UTF_8)).optLong("exp", 0L)
            if (exp == 0L) return@runCatching false
            val nowSec = System.currentTimeMillis() / 1000L
            nowSec + skewSeconds >= exp
        }.getOrDefault(true)
    }
}
