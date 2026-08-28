package com.kchat.di;

import com.kchat.data.network.emergency.DataStoreEmergencyWipeStore;
import com.kchat.data.repository.EmergencyWipeStore;
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
public final class RepositoryModule_ProvideEmergencyWipeStoreFactory implements Factory<EmergencyWipeStore> {
  private final Provider<DataStoreEmergencyWipeStore> implProvider;

  public RepositoryModule_ProvideEmergencyWipeStoreFactory(
      Provider<DataStoreEmergencyWipeStore> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public EmergencyWipeStore get() {
    return provideEmergencyWipeStore(implProvider.get());
  }

  public static RepositoryModule_ProvideEmergencyWipeStoreFactory create(
      Provider<DataStoreEmergencyWipeStore> implProvider) {
    return new RepositoryModule_ProvideEmergencyWipeStoreFactory(implProvider);
  }

  public static EmergencyWipeStore provideEmergencyWipeStore(DataStoreEmergencyWipeStore impl) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideEmergencyWipeStore(impl));
  }
}
