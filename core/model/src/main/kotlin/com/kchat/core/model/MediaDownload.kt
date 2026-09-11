package com.kchat.core.model

/**
 * A chat media file saved to the device Downloads collection.
 */
data class MediaDownloadResult(
    val contentUri: String,
    val fileName: String,
    val mimeType: String = "application/octet-stream",
)

/** Per-file download UI status keyed by media URL. */
sealed interface FileDownloadStatus {
    data class Downloading(val progress: Float?) : FileDownloadStatus
    data class Saved(val contentUri: String, val fileName: String) : FileDownloadStatus
}
