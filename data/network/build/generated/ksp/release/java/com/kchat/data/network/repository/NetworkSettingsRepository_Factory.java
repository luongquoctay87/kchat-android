package com.kchat.data.network.repository;

import android.content.Context;
import com.kchat.data.network.api.KChatApi;
import com.kchat.data.repository.DeviceTokenStore;
import com.kchat.data.repository.LocalCacheCleanupScheduler;
import com.kchat.data.repository.TokenStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "javax.inject.Named",
    "dagger.hilt.android.qualifiers.ApplicationContext"
})
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
public final class NetworkSettingsRepository_Factory implements Factory<NetworkSettingsRepository> {
  private final Provider<KChatApi> apiProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<DeviceTokenStore> deviceTokenStoreProvider;

  private final Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider;

  private final Provider<String> apiBaseUrlProvider;

  private final Provider<Context> contextProvider;

  public NetworkSettingsRepository_Factory(Provider<KChatApi> apiProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider,
      Provider<String> apiBaseUrlProvider, Provider<Context> contextProvider) {
    this.apiProvider = apiProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.deviceTokenStoreProvider = deviceTokenStoreProvider;
    this.localCacheCleanupSchedulerProvider = localCacheCleanupSchedulerProvider;
    this.apiBaseUrlProvider = apiBaseUrlProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkSettingsRepository get() {
    return newInstance(apiProvider.get(), tokenStoreProvider.get(), deviceTokenStoreProvider.get(), localCacheCleanupSchedulerProvider.get(), apiBaseUrlProvider.get(), contextProvider.get());
  }

  public static NetworkSettingsRepository_Factory create(Provider<KChatApi> apiProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<LocalCacheCleanupScheduler> localCacheCleanupSchedulerProvider,
      Provider<String> apiBaseUrlProvider, Provider<Context> contextProvider) {
    return new NetworkSettingsRepository_Factory(apiProvider, tokenStoreProvider, deviceTokenStoreProvider, localCacheCleanupSchedulerProvider, apiBaseUrlProvider, contextProvider);
  }

  public static NetworkSettingsRepository newInstance(KChatApi api, TokenStore tokenStore,
      DeviceTokenStore deviceTokenStore, LocalCacheCleanupScheduler localCacheCleanupScheduler,
      String apiBaseUrl, Context context) {
    return new NetworkSettingsRepository(api, tokenStore, deviceTokenStore, localCacheCleanupScheduler, apiBaseUrl, context);
  }
}
