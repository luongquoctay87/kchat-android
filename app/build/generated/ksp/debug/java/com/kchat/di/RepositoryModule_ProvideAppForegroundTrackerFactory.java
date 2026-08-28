package com.kchat.di;

import com.kchat.data.repository.AppForegroundTracker;
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
public final class RepositoryModule_ProvideAppForegroundTrackerFactory implements Factory<AppForegroundTracker> {
  @Override
  public AppForegroundTracker get() {
    return provideAppForegroundTracker();
  }

  public static RepositoryModule_ProvideAppForegroundTrackerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppForegroundTracker provideAppForegroundTracker() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideAppForegroundTracker());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvideAppForegroundTrackerFactory INSTANCE = new RepositoryModule_ProvideAppForegroundTrackerFactory();
  }
}
