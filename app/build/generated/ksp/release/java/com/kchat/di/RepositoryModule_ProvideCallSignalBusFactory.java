package com.kchat.di;

import com.kchat.data.repository.CallSignalBus;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class RepositoryModule_ProvideCallSignalBusFactory implements Factory<CallSignalBus> {
  @Override
  public CallSignalBus get() {
    return provideCallSignalBus();
  }

  public static RepositoryModule_ProvideCallSignalBusFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CallSignalBus provideCallSignalBus() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideCallSignalBus());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvideCallSignalBusFactory INSTANCE = new RepositoryModule_ProvideCallSignalBusFactory();
  }
}
