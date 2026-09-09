package com.kchat.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kchat.BuildConfig
import com.kchat.data.network.api.KChatApi
import com.kchat.data.network.auth.JwtAuthInterceptor
import com.kchat.data.network.auth.TokenAuthenticator
import com.kchat.data.network.auth.UnauthorizedSessionInterceptor
import com.kchat.data.network.ws.KChatWebSocketClient
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenStore: TokenStore,
        accessTokenHolder: AccessTokenHolder,
        deviceTokenStore: DeviceTokenStore,
    ): TokenAuthenticator = TokenAuthenticator(
        tokenStore = tokenStore,
        accessTokenHolder = accessTokenHolder,
        deviceTokenStore = deviceTokenStore,
        apiBaseUrl = BuildConfig.API_BASE_URL,
    )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        accessTokenHolder: AccessTokenHolder,
        tokenStore: TokenStore,
        deviceTokenStore: DeviceTokenStore,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(JwtAuthInterceptor(accessTokenHolder, tokenStore, deviceTokenStore))
            .addInterceptor(UnauthorizedSessionInterceptor(tokenStore))
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** Dedicated client for WS — no JWT interceptor / authenticator. */
    @Provides
    @Singleton
    @Named("ws")
    fun provideWsOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("apiBaseUrl")
    fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
        accessTokenHolder: AccessTokenHolder,
        tokenStore: TokenStore,
        deviceTokenStore: DeviceTokenStore,
        tokenAuthenticator: TokenAuthenticator,
    ): coil.ImageLoader {
        // No BODY logging — binary image responses must not go through HttpLoggingInterceptor.BODY
        val imageClient = OkHttpClient.Builder()
            .addInterceptor(JwtAuthInterceptor(accessTokenHolder, tokenStore, deviceTokenStore))
            .addInterceptor(UnauthorizedSessionInterceptor(tokenStore))
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        return coil.ImageLoader.Builder(context)
            .okHttpClient(imageClient)
            .crossfade(true)
            .allowHardware(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideKChatApi(retrofit: Retrofit): KChatApi = retrofit.create(KChatApi::class.java)

    @Provides
    @Singleton
    fun provideWebSocketClient(
        @Named("ws") wsClient: OkHttpClient,
        accessTokenHolder: AccessTokenHolder,
    ): KChatWebSocketClient = KChatWebSocketClient(
        okHttpClient = wsClient,
        accessTokenHolder = accessTokenHolder,
        wsBaseUrl = BuildConfig.WS_BASE_URL,
    )
}
