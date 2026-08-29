package com.kchat.work;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class LocalCacheCleanupWorker_AssistedFactory_Impl implements LocalCacheCleanupWorker_AssistedFactory {
  private final LocalCacheCleanupWorker_Factory delegateFactory;

  LocalCacheCleanupWorker_AssistedFactory_Impl(LocalCacheCleanupWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public LocalCacheCleanupWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<LocalCacheCleanupWorker_AssistedFactory> create(
      LocalCacheCleanupWorker_Factory delegateFactory) {
    return InstanceFactory.create(new LocalCacheCleanupWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<LocalCacheCleanupWorker_AssistedFactory> createFactoryProvider(
      LocalCacheCleanupWorker_Factory delegateFactory) {
    return InstanceFactory.create(new LocalCacheCleanupWorker_AssistedFactory_Impl(delegateFactory));
  }
}
