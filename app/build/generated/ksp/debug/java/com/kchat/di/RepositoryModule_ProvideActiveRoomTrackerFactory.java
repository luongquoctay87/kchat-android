package com.kchat.di;

import com.kchat.data.repository.ActiveRoomTracker;
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
public final class RepositoryModule_ProvideActiveRoomTrackerFactory implements Factory<ActiveRoomTracker> {
  @Override
  public ActiveRoomTracker get() {
    return provideActiveRoomTracker();
  }

  public static RepositoryModule_ProvideActiveRoomTrackerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ActiveRoomTracker provideActiveRoomTracker() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideActiveRoomTracker());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvideActiveRoomTrackerFactory INSTANCE = new RepositoryModule_ProvideActiveRoomTrackerFactory();
  }
}
