package com.kchat.data.repository

/** In-memory access token for OkHttp interceptors (avoid runBlocking on TokenStore). */
class AccessTokenHolder {
    @Volatile
    var accessToken: String? = null
        private set

    @Volatile
    var userId: String? = null
        private set

    fun set(token: String?) {
        accessToken = token
        userId = null
    }

    fun set(token: String?, userId: String?) {
        accessToken = token
        this.userId = userId?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
    }
}
