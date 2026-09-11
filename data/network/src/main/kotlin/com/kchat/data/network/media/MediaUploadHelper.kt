package com.kchat.data.network.media

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

internal object MediaUploadHelper {
    const val MAX_BYTES = 100L * 1024 * 1024
    const val AVATAR_MAX_BYTES = 5L * 1024 * 1024

    private val AVATAR_MIME = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
    )

    fun isAllowedAvatarMime(mime: String?): Boolean {
        if (mime.isNullOrBlank()) return false
        return mime.trim().lowercase() in AVATAR_MIME
    }

    fun resolveMime(resolver: ContentResolver, uri: Uri, hint: String?): String {
        val fromResolver = resolver.getType(uri)
        if (!fromResolver.isNullOrBlank() && !fromResolver.contains("*")) {
            return fromResolver
        }
        if (!hint.isNullOrBlank() && !hint.contains("*")) {
            return hint
        }
        return "application/octet-stream"
    }

    fun resolveDisplayName(resolver: ContentResolver, uri: Uri, fallback: String?): String {
        if (!fallback.isNullOrBlank()) return fallback
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                if (!name.isNullOrBlank()) return name
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "file"
    }

    fun contentLength(resolver: ContentResolver, uri: Uri): Long {
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getLong(0)
            }
        }
        return resolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
    }

    fun requestBody(context: Context, uri: Uri, mime: String, contentLength: Long): RequestBody {
        val mediaType: MediaType? = mime.toMediaTypeOrNull()
        val resolver = context.contentResolver
        return object : RequestBody() {
            override fun contentType(): MediaType? = mediaType

            override fun contentLength(): Long = contentLength

            override fun writeTo(sink: BufferedSink) {
                resolver.openInputStream(uri)?.use { input ->
                    sink.writeAll(input.source())
                } ?: error("Không đọc được file")
            }
        }
    }
}
