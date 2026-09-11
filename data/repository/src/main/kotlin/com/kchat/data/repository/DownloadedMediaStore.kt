package com.kchat.data.repository

import com.kchat.core.model.MediaDownloadResult
import kotlinx.coroutines.flow.Flow

/** Persists Downloads URIs for chat media so files can be reopened without re-downloading. */
interface DownloadedMediaStore {
    fun observe(): Flow<Map<String, MediaDownloadResult>>

    suspend fun get(mediaUrl: String): MediaDownloadResult?

    suspend fun put(mediaUrl: String, result: MediaDownloadResult)

    suspend fun remove(mediaUrl: String)

    fun isAccessible(contentUri: String): Boolean
}
