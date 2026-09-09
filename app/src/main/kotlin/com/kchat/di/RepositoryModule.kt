package com.kchat.di

import com.kchat.BuildConfig
import com.kchat.data.network.auth.DataStoreDeviceTokenStore
import com.kchat.data.network.auth.DataStorePinLockStore
import com.kchat.data.network.auth.DataStoreTokenStore
import com.kchat.data.network.emergency.DataStoreEmergencyWipeStore
import com.kchat.data.network.emergency.EmergencyWipeCoordinatorImpl
import com.kchat.data.network.repository.NetworkAuthRepository
import com.kchat.data.network.repository.NetworkCallRepository
import com.kchat.data.network.repository.NetworkChatRepository
import com.kchat.data.network.repository.NetworkContactsRepository
import com.kchat.data.network.repository.NetworkPushTokenRegistrar
import com.kchat.data.network.repository.NetworkRealtimeCoordinator
import com.kchat.data.network.repository.NetworkSettingsRepository
import com.kchat.data.repository.AccessTokenHolder
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.AppForegroundTracker
import com.kchat.data.repository.IncomingMessageNotifier
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.CallRepository
import com.kchat.data.repository.PinLockTransientLeave
import com.kchat.data.repository.CallSignalBus
import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.ContactsRepository
import com.kchat.data.repository.EmergencyWipeCoordinator
import com.kchat.data.repository.EmergencyWipeStore
import com.kchat.data.repository.DeviceTokenStore
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.PushNavigationStore
import com.kchat.data.repository.PushTokenRegistrar
import com.kchat.data.repository.PushTokenSync
import com.kchat.data.repository.LocalCacheCleanupScheduler
import com.kchat.data.repository.RealtimeCoordinator
import com.kchat.data.repository.SessionCoordinator
import com.kchat.data.repository.SettingsRepository
import com.kchat.data.repository.TokenStore
import com.kchat.data.repository.TypingStateStore
import com.kchat.push.FirebasePushTokenSync
import com.kchat.push.FcmTokenHandler
import com.kchat.push.IncomingMessageNotifierImpl
import com.kchat.push.NoOpPushTokenSync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAccessTokenHolder(): AccessTokenHolder = AccessTokenHolder()

    @Provides
    @Singleton
    fun provideActiveRoomTracker(): ActiveRoomTracker = ActiveRoomTracker()

    @Provides
    @Singleton
    fun provideTypingStateStore(): TypingStateStore = TypingStateStore()

    @Provides
    @Singleton
    fun provideCallSignalBus(): CallSignalBus = CallSignalBus()

    @Provides
    @Singleton
    fun provideAppForegroundTracker(): AppForegroundTracker = AppForegroundTracker()

    @Provides
    @Singleton
    fun providePinLockTransientLeave(): PinLockTransientLeave = PinLockTransientLeave()

    @Provides
    @Singleton
    fun provideIncomingMessageNotifier(impl: IncomingMessageNotifierImpl): IncomingMessageNotifier = impl

    @Provides
    @Singleton
    fun providePushNavigationStore(): PushNavigationStore = PushNavigationStore()

    @Provides
    @Singleton
    fun providePushTokenSync(
        firebase: FirebasePushTokenSync,
        noop: NoOpPushTokenSync,
    ): PushTokenSync = if (BuildConfig.FCM_ENABLED) firebase else noop

    @Provides
    @Singleton
    fun provideSessionCoordinator(
        chatRepository: ChatRepository,
        contactsRepository: ContactsRepository,
        realtimeCoordinator: RealtimeCoordinator,
        settingsRepository: SettingsRepository,
        localCacheCleanupScheduler: LocalCacheCleanupScheduler,
        pushTokenSync: PushTokenSync,
        fcmTokenHandler: FcmTokenHandler,
    ): SessionCoordinator = SessionCoordinator(
        chatRepository,
        contactsRepository,
        realtimeCoordinator,
        settingsRepository,
        localCacheCleanupScheduler,
        pushTokenSync,
        pendingFcmFlush = { fcmTokenHandler.flushPendingToken() },
    )

    @Provides
    @Singleton
    fun provideTokenStore(network: DataStoreTokenStore): TokenStore = network

    @Provides
    @Singleton
    fun provideDeviceTokenStore(network: DataStoreDeviceTokenStore): DeviceTokenStore = network

    @Provides
    @Singleton
    fun provideEmergencyWipeStore(impl: DataStoreEmergencyWipeStore): EmergencyWipeStore = impl

    @Provides
    @Singleton
    fun provideEmergencyWipeCoordinator(impl: EmergencyWipeCoordinatorImpl): EmergencyWipeCoordinator = impl

    @Provides
    @Singleton
    fun providePinLockStore(network: DataStorePinLockStore): PinLockStore = network

    @Provides
    @Singleton
    fun provideAuthRepository(network: NetworkAuthRepository): AuthRepository = network

    @Provides
    @Singleton
    fun provideChatRepository(network: NetworkChatRepository): ChatRepository = network

    @Provides
    @Singleton
    fun provideContactsRepository(network: NetworkContactsRepository): ContactsRepository = network

    @Provides
    @Singleton
    fun provideSettingsRepository(network: NetworkSettingsRepository): SettingsRepository = network

    @Provides
    @Singleton
    fun provideCallRepository(network: NetworkCallRepository): CallRepository = network

    @Provides
    @Singleton
    fun providePushTokenRegistrar(network: NetworkPushTokenRegistrar): PushTokenRegistrar = network

    @Provides
    @Singleton
    fun provideRealtimeCoordinator(network: NetworkRealtimeCoordinator): RealtimeCoordinator = network
}
