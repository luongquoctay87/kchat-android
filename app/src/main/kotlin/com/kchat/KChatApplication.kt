package com.kchat

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KChatApplication : Application(), ImageLoaderFactory, Configuration.Provider {
    @Inject lateinit var imageLoader: ImageLoader
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
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
