package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.AuthRepository;
import com.kchat.data.repository.EmergencyWipeCoordinator;
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
public final class PinLockViewModel_Factory implements Factory<PinLockViewModel> {
  private final Provider<PinLockStore> pinLockStoreProvider;

  private final Provider<EmergencyWipeCoordinator> emergencyWipeCoordinatorProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public PinLockViewModel_Factory(Provider<PinLockStore> pinLockStoreProvider,
      Provider<EmergencyWipeCoordinator> emergencyWipeCoordinatorProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.pinLockStoreProvider = pinLockStoreProvider;
    this.emergencyWipeCoordinatorProvider = emergencyWipeCoordinatorProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public PinLockViewModel get() {
    return newInstance(pinLockStoreProvider.get(), emergencyWipeCoordinatorProvider.get(), authRepositoryProvider.get());
  }

  public static PinLockViewModel_Factory create(Provider<PinLockStore> pinLockStoreProvider,
      Provider<EmergencyWipeCoordinator> emergencyWipeCoordinatorProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new PinLockViewModel_Factory(pinLockStoreProvider, emergencyWipeCoordinatorProvider, authRepositoryProvider);
  }

  public static PinLockViewModel newInstance(PinLockStore pinLockStore,
      EmergencyWipeCoordinator emergencyWipeCoordinator, AuthRepository authRepository) {
    return new PinLockViewModel(pinLockStore, emergencyWipeCoordinator, authRepository);
  }
}
