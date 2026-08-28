package com.kchat.data.network.repository;

import com.kchat.data.local.ChatLocalDataSource;
import com.kchat.data.network.api.KChatApi;
import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.EmergencyWipeStore;
import com.kchat.data.repository.PinLockStore;
import com.kchat.data.repository.RealtimeCoordinator;
import com.kchat.data.repository.SettingsRepository;
import com.kchat.data.repository.TokenStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NetworkAuthRepository_Factory implements Factory<NetworkAuthRepository> {
  private final Provider<KChatApi> apiProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<ChatLocalDataSource> localDataSourceProvider;

  private final Provider<RealtimeCoordinator> realtimeCoordinatorProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<PinLockStore> pinLockStoreProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  public NetworkAuthRepository_Factory(Provider<KChatApi> apiProvider,
      Provider<TokenStore> tokenStoreProvider,
      Provider<ChatLocalDataSource> localDataSourceProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<PinLockStore> pinLockStoreProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    this.apiProvider = apiProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.localDataSourceProvider = localDataSourceProvider;
    this.realtimeCoordinatorProvider = realtimeCoordinatorProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.pinLockStoreProvider = pinLockStoreProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
  }

  @Override
  public NetworkAuthRepository get() {
    return newInstance(apiProvider.get(), tokenStoreProvider.get(), localDataSourceProvider.get(), realtimeCoordinatorProvider.get(), settingsRepositoryProvider.get(), pinLockStoreProvider.get(), contactsRepositoryProvider.get(), emergencyWipeStoreProvider.get());
  }

  public static NetworkAuthRepository_Factory create(Provider<KChatApi> apiProvider,
      Provider<TokenStore> tokenStoreProvider,
      Provider<ChatLocalDataSource> localDataSourceProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<PinLockStore> pinLockStoreProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    return new NetworkAuthRepository_Factory(apiProvider, tokenStoreProvider, localDataSourceProvider, realtimeCoordinatorProvider, settingsRepositoryProvider, pinLockStoreProvider, contactsRepositoryProvider, emergencyWipeStoreProvider);
  }

  public static NetworkAuthRepository newInstance(KChatApi api, TokenStore tokenStore,
      ChatLocalDataSource localDataSource, RealtimeCoordinator realtimeCoordinator,
      SettingsRepository settingsRepository, PinLockStore pinLockStore,
      ContactsRepository contactsRepository, EmergencyWipeStore emergencyWipeStore) {
    return new NetworkAuthRepository(api, tokenStore, localDataSource, realtimeCoordinator, settingsRepository, pinLockStore, contactsRepository, emergencyWipeStore);
  }
}
