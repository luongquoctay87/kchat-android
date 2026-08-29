package com.kchat.work;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.kchat.data.local.ChatLocalDataSource;
import dagger.internal.DaggerGenerated;
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
public final class LocalCacheCleanupWorker_Factory {
  private final Provider<ChatLocalDataSource> localDataSourceProvider;

  public LocalCacheCleanupWorker_Factory(Provider<ChatLocalDataSource> localDataSourceProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
  }

  public LocalCacheCleanupWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, localDataSourceProvider.get());
  }

  public static LocalCacheCleanupWorker_Factory create(
      Provider<ChatLocalDataSource> localDataSourceProvider) {
    return new LocalCacheCleanupWorker_Factory(localDataSourceProvider);
  }

  public static LocalCacheCleanupWorker newInstance(Context context, WorkerParameters params,
      ChatLocalDataSource localDataSource) {
    return new LocalCacheCleanupWorker(context, params, localDataSource);
  }
}
