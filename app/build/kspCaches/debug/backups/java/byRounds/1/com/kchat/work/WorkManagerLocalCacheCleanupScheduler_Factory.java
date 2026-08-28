package com.kchat.work;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class WorkManagerLocalCacheCleanupScheduler_Factory implements Factory<WorkManagerLocalCacheCleanupScheduler> {
  private final Provider<Context> contextProvider;

  public WorkManagerLocalCacheCleanupScheduler_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WorkManagerLocalCacheCleanupScheduler get() {
    return newInstance(contextProvider.get());
  }

  public static WorkManagerLocalCacheCleanupScheduler_Factory create(
      Provider<Context> contextProvider) {
    return new WorkManagerLocalCacheCleanupScheduler_Factory(contextProvider);
  }

  public static WorkManagerLocalCacheCleanupScheduler newInstance(Context context) {
    return new WorkManagerLocalCacheCleanupScheduler(context);
  }
}
