package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.AuthRepository;
import com.kchat.data.repository.PinLockStore;
import com.kchat.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<PinLockStore> pinLockStoreProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<PinLockStore> pinLockStoreProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.pinLockStoreProvider = pinLockStoreProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), authRepositoryProvider.get(), pinLockStoreProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<PinLockStore> pinLockStoreProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, authRepositoryProvider, pinLockStoreProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      AuthRepository authRepository, PinLockStore pinLockStore) {
    return new SettingsViewModel(settingsRepository, authRepository, pinLockStore);
  }
}
