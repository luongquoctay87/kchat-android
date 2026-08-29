package com.kchat.data.fake;

import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.EmergencyWipeStore;
import com.kchat.data.repository.PinLockStore;
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
public final class FakeAuthRepository_Factory implements Factory<FakeAuthRepository> {
  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<PinLockStore> pinLockStoreProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  public FakeAuthRepository_Factory(Provider<TokenStore> tokenStoreProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<PinLockStore> pinLockStoreProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    this.tokenStoreProvider = tokenStoreProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.pinLockStoreProvider = pinLockStoreProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
  }

  @Override
  public FakeAuthRepository get() {
    return newInstance(tokenStoreProvider.get(), settingsRepositoryProvider.get(), pinLockStoreProvider.get(), contactsRepositoryProvider.get(), emergencyWipeStoreProvider.get());
  }

  public static FakeAuthRepository_Factory create(Provider<TokenStore> tokenStoreProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<PinLockStore> pinLockStoreProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    return new FakeAuthRepository_Factory(tokenStoreProvider, settingsRepositoryProvider, pinLockStoreProvider, contactsRepositoryProvider, emergencyWipeStoreProvider);
  }

  public static FakeAuthRepository newInstance(TokenStore tokenStore,
      SettingsRepository settingsRepository, PinLockStore pinLockStore,
      ContactsRepository contactsRepository, EmergencyWipeStore emergencyWipeStore) {
    return new FakeAuthRepository(tokenStore, settingsRepository, pinLockStore, contactsRepository, emergencyWipeStore);
  }
}
