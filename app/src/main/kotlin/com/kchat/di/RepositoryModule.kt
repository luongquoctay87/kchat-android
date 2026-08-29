package com.kchat.di

import com.kchat.BuildConfig
import com.kchat.data.fake.FakeAuthRepository
import com.kchat.data.fake.FakeCallRepository
import com.kchat.data.fake.FakeChatRepository
import com.kchat.data.fake.FakeContactsRepository
import com.kchat.data.fake.FakeDeviceTokenStore
import com.kchat.data.fake.FakePinLockStore
import com.kchat.data.fake.FakePushTokenRegistrar
import com.kchat.data.fake.FakeRealtimeCoordinator
import com.kchat.data.fake.FakeSettingsRepository
import com.kchat.data.fake.FakeTokenStore
import com.kchat.data.fake.NoOpIncomingMessageNotifier
import com.kchat.data.fake.NoOpPushTokenSync
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
    fun provideIncomingMessageNotifier(
        noop: NoOpIncomingMessageNotifier,
        impl: IncomingMessageNotifierImpl,
    ): IncomingMessageNotifier = if (BuildConfig.USE_FAKE_DATA) noop else impl

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
    fun provideTokenStore(
        fake: FakeTokenStore,
        network: DataStoreTokenStore,
    ): TokenStore = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideDeviceTokenStore(
        fake: FakeDeviceTokenStore,
        network: DataStoreDeviceTokenStore,
    ): DeviceTokenStore = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideEmergencyWipeStore(impl: DataStoreEmergencyWipeStore): EmergencyWipeStore = impl

    @Provides
    @Singleton
    fun provideEmergencyWipeCoordinator(impl: EmergencyWipeCoordinatorImpl): EmergencyWipeCoordinator = impl

    @Provides
    @Singleton
    fun providePinLockStore(
        fake: FakePinLockStore,
        network: DataStorePinLockStore,
    ): PinLockStore = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideAuthRepository(
        fake: FakeAuthRepository,
        network: NetworkAuthRepository,
    ): AuthRepository = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideChatRepository(
        fake: FakeChatRepository,
        network: NetworkChatRepository,
    ): ChatRepository = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideContactsRepository(
        fake: FakeContactsRepository,
        network: NetworkContactsRepository,
    ): ContactsRepository = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideSettingsRepository(
        fake: FakeSettingsRepository,
        network: NetworkSettingsRepository,
    ): SettingsRepository = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideCallRepository(
        fake: FakeCallRepository,
        network: NetworkCallRepository,
    ): CallRepository = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun providePushTokenRegistrar(
        fake: FakePushTokenRegistrar,
        network: NetworkPushTokenRegistrar,
    ): PushTokenRegistrar = if (BuildConfig.USE_FAKE_DATA) fake else network

    @Provides
    @Singleton
    fun provideRealtimeCoordinator(
        fake: FakeRealtimeCoordinator,
        network: NetworkRealtimeCoordinator,
    ): RealtimeCoordinator = if (BuildConfig.USE_FAKE_DATA) fake else network
}
