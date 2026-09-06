package com.kchat.session

import android.content.Context
import android.util.Log
import com.kchat.data.local.ChatLocalDataSource
import com.kchat.data.repository.EmergencyWipeStore
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.TokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

/**
 * Auto Backup / OEM restore can bring back DataStore tokens after uninstall.
 * [Context.getNoBackupFilesDir] is not restored, so a missing marker means this
 * process is a new install — drop any restored session so Login is shown.
 */
@Singleton
class FreshInstallSessionGuard @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStore: TokenStore,
    private val pinLockStore: PinLockStore,
    private val emergencyWipeStore: EmergencyWipeStore,
    private val settingsRepository: SettingsRepository,
    private val localDataSource: ChatLocalDataSource,
) {
    fun discardRestoredSessionIfNeeded() {
        val marker = File(context.noBackupFilesDir, MARKER_NAME)
        if (marker.exists()) return
        val info = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }.getOrNull()
        val isUpgrade = info != null && info.lastUpdateTime > info.firstInstallTime
        if (isUpgrade) {
            // First launch of this guard on an existing install — keep the session.
            writeMarker(marker)
            return
        }
        Log.i(TAG, "Fresh install — clearing any Auto Backup-restored session")
        runBlocking {
            runCatching { tokenStore.clear() }
            runCatching { pinLockStore.clear() }
            runCatching { emergencyWipeStore.reset() }
            runCatching { settingsRepository.clearLocal() }
            runCatching { localDataSource.clearAll() }
        }
        writeMarker(marker)
    }

    private fun writeMarker(marker: File) {
        runCatching {
            marker.parentFile?.mkdirs()
            marker.writeText("1")
        }
    }

    private companion object {
        const val TAG = "KChatInstall"
        const val MARKER_NAME = "install_session"
    }
}
