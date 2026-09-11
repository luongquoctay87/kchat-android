package com.kchat.data.network.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.kchat.core.model.MediaDownloadResult
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal object MediaDownloadWriter {
    fun sanitizeFileName(fileName: String): String {
        val trimmed = fileName.trim().ifBlank { "file" }
        return trimmed.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    fun guessMime(fileName: String, fallback: String?): String {
        if (!fallback.isNullOrBlank() && fallback != "application/octet-stream") {
            return fallback
        }
        val ext = fileName.substringAfterLast('.', "").lowercase()
        if (ext.isBlank()) return fallback ?: "application/octet-stream"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: fallback
            ?: "application/octet-stream"
    }

    fun writeToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        input: InputStream,
        contentLength: Long,
        onProgress: ((bytesRead: Long, contentLength: Long) -> Unit)?,
    ): MediaDownloadResult {
        val safeName = sanitizeFileName(fileName)
        val mime = guessMime(safeName, mimeType)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(context, safeName, mime, input, contentLength, onProgress)
        } else {
            writeViaLegacyDownloads(context, safeName, mime, input, contentLength, onProgress)
        }
        return MediaDownloadResult(
            contentUri = uri.toString(),
            fileName = safeName,
            mimeType = mime,
        )
    }

    private fun writeViaMediaStore(
        context: Context,
        fileName: String,
        mimeType: String,
        input: InputStream,
        contentLength: Long,
        onProgress: ((bytesRead: Long, contentLength: Long) -> Unit)?,
    ): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values)
            ?: error("Không tạo được file trong Downloads")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                copyWithProgress(input, output, contentLength, onProgress)
            } ?: error("Không ghi được file")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (t: Throwable) {
            runCatching { resolver.delete(uri, null, null) }
            throw t
        }
    }

    @Suppress("DEPRECATION")
    private fun writeViaLegacyDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        input: InputStream,
        contentLength: Long,
        onProgress: ((bytesRead: Long, contentLength: Long) -> Unit)?,
    ): Uri {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists() && !dir.mkdirs()) {
            error("Không tạo được thư mục Downloads")
        }
        val target = uniqueFile(dir, fileName)
        target.outputStream().use { output ->
            copyWithProgress(input, output, contentLength, onProgress)
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(target.absolutePath),
            arrayOf(mimeType),
            null,
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.files",
            target,
        )
    }

    private fun uniqueFile(dir: File, fileName: String): File {
        val base = File(dir, fileName)
        if (!base.exists()) return base
        val dot = fileName.lastIndexOf('.')
        val name = if (dot > 0) fileName.substring(0, dot) else fileName
        val ext = if (dot > 0) fileName.substring(dot) else ""
        var index = 1
        while (true) {
            val candidate = File(dir, "$name ($index)$ext")
            if (!candidate.exists()) return candidate
            index++
        }
    }

    private fun copyWithProgress(
        input: InputStream,
        output: OutputStream,
        contentLength: Long,
        onProgress: ((bytesRead: Long, contentLength: Long) -> Unit)?,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead = 0L
        var lastEmitted = 0L
        onProgress?.invoke(0L, contentLength)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            output.write(buffer, 0, n)
            bytesRead += n
            if (onProgress != null &&
                (bytesRead - lastEmitted >= PROGRESS_EMIT_BYTES || bytesRead == contentLength)
            ) {
                onProgress(bytesRead, contentLength)
                lastEmitted = bytesRead
            }
        }
        output.flush()
        onProgress?.invoke(bytesRead, contentLength)
    }

    private const val PROGRESS_EMIT_BYTES = 64L * 1024
}
