package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.PinLockStore;
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
public final class PinSettingsViewModel_Factory implements Factory<PinSettingsViewModel> {
  private final Provider<PinLockStore> pinLockStoreProvider;

  public PinSettingsViewModel_Factory(Provider<PinLockStore> pinLockStoreProvider) {
    this.pinLockStoreProvider = pinLockStoreProvider;
  }

  @Override
  public PinSettingsViewModel get() {
    return newInstance(pinLockStoreProvider.get());
  }

  public static PinSettingsViewModel_Factory create(Provider<PinLockStore> pinLockStoreProvider) {
    return new PinSettingsViewModel_Factory(pinLockStoreProvider);
  }

  public static PinSettingsViewModel newInstance(PinLockStore pinLockStore) {
    return new PinSettingsViewModel(pinLockStore);
  }
}
