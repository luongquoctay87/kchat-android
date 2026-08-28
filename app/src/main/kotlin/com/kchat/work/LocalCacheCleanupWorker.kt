package com.kchat.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kchat.core.model.MessageStorageOptions
import com.kchat.data.local.ChatLocalDataSource
import com.kchat.data.repository.LocalCacheCleanupScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@HiltWorker
class LocalCacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val localDataSource: ChatLocalDataSource,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val days = inputData.getInt(KEY_RETENTION_DAYS, MessageStorageOptions.DEFAULT_RETENTION_DAYS)
        if (!MessageStorageOptions.isValidCacheRetention(days)) {
            return Result.success()
        }
        localDataSource.purgeExpiredLocalMessages(days)
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "local_cache_cleanup"
        const val UNIQUE_RUN_NOW_NAME = "local_cache_cleanup_now"
        const val KEY_RETENTION_DAYS = "retention_days"

        fun inputData(retentionDays: Int) = workDataOf(KEY_RETENTION_DAYS to retentionDays)

        fun constraints() = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
    }
}

@Singleton
class WorkManagerLocalCacheCleanupScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalCacheCleanupScheduler {

    override fun schedule(retentionDays: Int?) {
        val workManager = WorkManager.getInstance(context)
        if (retentionDays == null || !MessageStorageOptions.isValidCacheRetention(retentionDays)) {
            workManager.cancelUniqueWork(LocalCacheCleanupWorker.UNIQUE_WORK_NAME)
            workManager.cancelUniqueWork(LocalCacheCleanupWorker.UNIQUE_RUN_NOW_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<LocalCacheCleanupWorker>(1, TimeUnit.DAYS)
            .setInputData(LocalCacheCleanupWorker.inputData(retentionDays))
            .setConstraints(LocalCacheCleanupWorker.constraints())
            .build()
        // CANCEL_AND_REENQUEUE so changing retention days updates inputData (UPDATE does not).
        workManager.enqueueUniquePeriodicWork(
            LocalCacheCleanupWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    override fun runNow(retentionDays: Int?) {
        val days = retentionDays?.takeIf { MessageStorageOptions.isValidCacheRetention(it) } ?: return
        val request = OneTimeWorkRequestBuilder<LocalCacheCleanupWorker>()
            .setInputData(LocalCacheCleanupWorker.inputData(days))
            .setConstraints(LocalCacheCleanupWorker.constraints())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            LocalCacheCleanupWorker.UNIQUE_RUN_NOW_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
