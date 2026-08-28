package com.kchat.di;

import com.kchat.data.repository.ChatRepository;
import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.LocalCacheCleanupScheduler;
import com.kchat.data.repository.PushTokenSync;
import com.kchat.data.repository.RealtimeCoordinator;
import com.kchat.data.repository.SessionCoordinator;
import com.kchat.data.repository.SettingsRepository;
import com.kchat.push.FcmTokenHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class RepositoryModule_ProvideSessionCoordinatorFactory implements Factory<SessionCoordinator> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<RealtimeCoordinator> realtimeCoordinatorProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider;

  private final Provider<PushTokenSync> pushTokenSyncProvider;

  private final Provider<FcmTokenHandler> fcmTokenHandlerProvider;

  public RepositoryModule_ProvideSessionCoordinatorFactory(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider,
      Provider<PushTokenSync> pushTokenSyncProvider,
      Provider<FcmTokenHandler> fcmTokenHandlerProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.realtimeCoordinatorProvider = realtimeCoordinatorProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.localCacheCleanupSchedulerProvider = localCacheCleanupSchedulerProvider;
    this.pushTokenSyncProvider = pushTokenSyncProvider;
    this.fcmTokenHandlerProvider = fcmTokenHandlerProvider;
  }

  @Override
  public SessionCoordinator get() {
    return provideSessionCoordinator(chatRepositoryProvider.get(), contactsRepositoryProvider.get(), realtimeCoordinatorProvider.get(), settingsRepositoryProvider.get(), localCacheCleanupSchedulerProvider.get(), pushTokenSyncProvider.get(), fcmTokenHandlerProvider.get());
  }

  public static RepositoryModule_ProvideSessionCoordinatorFactory create(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider,
      Provider<PushTokenSync> pushTokenSyncProvider,
      Provider<FcmTokenHandler> fcmTokenHandlerProvider) {
    return new RepositoryModule_ProvideSessionCoordinatorFactory(chatRepositoryProvider, contactsRepositoryProvider, realtimeCoordinatorProvider, settingsRepositoryProvider, localCacheCleanupSchedulerProvider, pushTokenSyncProvider, fcmTokenHandlerProvider);
  }

  public static SessionCoordinator provideSessionCoordinator(ChatRepository chatRepository,
      ContactsRepository contactsRepository, RealtimeCoordinator realtimeCoordinator,
      SettingsRepository settingsRepository, LocalCacheCleanupScheduler localCacheCleanupScheduler,
      PushTokenSync pushTokenSync, FcmTokenHandler fcmTokenHandler) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideSessionCoordinator(chatRepository, contactsRepository, realtimeCoordinator, settingsRepository, localCacheCleanupScheduler, pushTokenSync, fcmTokenHandler));
  }
}
