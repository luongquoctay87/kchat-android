package com.kchat.data.fake;

import com.kchat.data.repository.LocalCacheCleanupScheduler;
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
public final class FakeSettingsRepository_Factory implements Factory<FakeSettingsRepository> {
  private final Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider;

  public FakeSettingsRepository_Factory(
      Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider) {
    this.localCacheCleanupSchedulerProvider = localCacheCleanupSchedulerProvider;
  }

  @Override
  public FakeSettingsRepository get() {
    return newInstance(localCacheCleanupSchedulerProvider.get());
  }

  public static FakeSettingsRepository_Factory create(
      Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider) {
    return new FakeSettingsRepository_Factory(localCacheCleanupSchedulerProvider);
  }

  public static FakeSettingsRepository newInstance(
      LocalCacheCleanupScheduler localCacheCleanupScheduler) {
    return new FakeSettingsRepository(localCacheCleanupScheduler);
  }
}
