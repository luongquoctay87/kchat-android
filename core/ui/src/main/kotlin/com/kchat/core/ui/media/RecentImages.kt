package com.kchat.core.ui.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object RecentImages {
    private const val DEFAULT_LIMIT = 60

    fun query(context: Context, limit: Int = DEFAULT_LIMIT): List<Uri> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val uris = mutableListOf<Uri>()
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext() && uris.size < limit) {
                val id = cursor.getLong(idColumn)
                uris += ContentUris.withAppendedId(collection, id)
            }
        }
        return uris
    }
}
