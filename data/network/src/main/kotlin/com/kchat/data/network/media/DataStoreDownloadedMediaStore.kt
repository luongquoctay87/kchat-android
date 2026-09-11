package com.kchat.data.network.media

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kchat.core.model.MediaDownloadResult
import com.kchat.data.network.auth.ReplaceEmptyPreferences
import com.kchat.data.repository.DownloadedMediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.downloadedMediaDataStore by preferencesDataStore(
    name = "kchat_downloaded_media",
    corruptionHandler = ReplaceEmptyPreferences,
)

@Singleton
class DataStoreDownloadedMediaStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : DownloadedMediaStore {
    private val dataStore = context.downloadedMediaDataStore
    private val entriesKey = stringPreferencesKey("entries_json")

    override fun observe(): Flow<Map<String, MediaDownloadResult>> =
        dataStore.data.map { prefs -> decode(prefs[entriesKey]) }

    override suspend fun get(mediaUrl: String): MediaDownloadResult? =
        observe().first()[normalize(mediaUrl)]

    override suspend fun put(mediaUrl: String, result: MediaDownloadResult) {
        val key = normalize(mediaUrl)
        dataStore.edit { prefs ->
            val map = decode(prefs[entriesKey]).toMutableMap()
            map[key] = result
            prefs[entriesKey] = encode(map)
        }
    }

    override suspend fun remove(mediaUrl: String) {
        val key = normalize(mediaUrl)
        dataStore.edit { prefs ->
            val map = decode(prefs[entriesKey]).toMutableMap()
            if (map.remove(key) != null) {
                prefs[entriesKey] = encode(map)
            }
        }
    }

    override fun isAccessible(contentUri: String): Boolean {
        val uri = runCatching { Uri.parse(contentUri) }.getOrNull() ?: return false
        return runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        }.getOrDefault(false)
    }

    private fun normalize(mediaUrl: String): String = mediaUrl.trim()

    private fun decode(raw: String?): Map<String, MediaDownloadResult> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            buildMap {
                val keys = root.keys()
                while (keys.hasNext()) {
                    val url = keys.next()
                    val item = root.getJSONObject(url)
                    put(
                        url,
                        MediaDownloadResult(
                            contentUri = item.getString("contentUri"),
                            fileName = item.optString("fileName", "file"),
                            mimeType = item.optString("mimeType", "application/octet-stream"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun encode(map: Map<String, MediaDownloadResult>): String {
        val root = JSONObject()
        map.forEach { (url, result) ->
            root.put(
                url,
                JSONObject()
                    .put("contentUri", result.contentUri)
                    .put("fileName", result.fileName)
                    .put("mimeType", result.mimeType),
            )
        }
        return root.toString()
    }
}
