package com.kchat

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.kchat.data.repository.AppForegroundTracker
import com.kchat.push.PushNotificationHelper
import com.kchat.session.FreshInstallSessionGuard
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KChatApplication : Application(), ImageLoaderFactory, Configuration.Provider {
    @Inject lateinit var imageLoader: ImageLoader
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var freshInstallSessionGuard: FreshInstallSessionGuard
    @Inject lateinit var pushNotificationHelper: PushNotificationHelper
    @Inject lateinit var appForegroundTracker: AppForegroundTracker

    override fun onCreate() {
        super.onCreate()
        freshInstallSessionGuard.discardRestoredSessionIfNeeded()
        pushNotificationHelper.ensureChannel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> appForegroundTracker.onForeground()
                    Lifecycle.Event.ON_STOP -> appForegroundTracker.onBackground()
                    else -> Unit
                }
            },
        )
        // Last-resort: never let an uncaught coroutine/OkHttp failure kill the process.
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            if (error is retrofit2.HttpException || error.cause is retrofit2.HttpException) {
                Log.e("KChat", "Swallowed uncaught HttpException on ${thread.name}", error)
                return@setDefaultUncaughtExceptionHandler
            }
            previous?.uncaughtException(thread, error)
        }
        if (BuildConfig.DEBUG) {
            Log.i(
                "KChat",
                "APP_ENV=${BuildConfig.APP_ENV} API=${BuildConfig.API_BASE_URL}",
            )
        }
    }

    override fun newImageLoader(): ImageLoader = imageLoader

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
