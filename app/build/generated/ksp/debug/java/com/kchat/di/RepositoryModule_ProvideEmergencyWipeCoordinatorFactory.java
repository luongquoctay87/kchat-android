package com.kchat.di;

import com.kchat.data.network.emergency.EmergencyWipeCoordinatorImpl;
import com.kchat.data.repository.EmergencyWipeCoordinator;
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
public final class RepositoryModule_ProvideEmergencyWipeCoordinatorFactory implements Factory<EmergencyWipeCoordinator> {
  private final Provider<EmergencyWipeCoordinatorImpl> implProvider;

  public RepositoryModule_ProvideEmergencyWipeCoordinatorFactory(
      Provider<EmergencyWipeCoordinatorImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public EmergencyWipeCoordinator get() {
    return provideEmergencyWipeCoordinator(implProvider.get());
  }

  public static RepositoryModule_ProvideEmergencyWipeCoordinatorFactory create(
      Provider<EmergencyWipeCoordinatorImpl> implProvider) {
    return new RepositoryModule_ProvideEmergencyWipeCoordinatorFactory(implProvider);
  }

  public static EmergencyWipeCoordinator provideEmergencyWipeCoordinator(
      EmergencyWipeCoordinatorImpl impl) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideEmergencyWipeCoordinator(impl));
  }
}
