package com.kchat.data.network.repository;

import android.content.Context;
import com.kchat.data.local.ChatLocalDataSource;
import com.kchat.data.network.api.KChatApi;
import com.kchat.data.repository.EmergencyWipeStore;
import com.kchat.data.repository.TypingStateStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "javax.inject.Named"
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
public final class NetworkChatRepository_Factory implements Factory<NetworkChatRepository> {
  private final Provider<KChatApi> apiProvider;

  private final Provider<ChatLocalDataSource> localDataSourceProvider;

  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  private final Provider<TypingStateStore> typingStateStoreProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<Context> contextProvider;

  private final Provider<String> apiBaseUrlProvider;

  public NetworkChatRepository_Factory(Provider<KChatApi> apiProvider,
      Provider<ChatLocalDataSource> localDataSourceProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider,
      Provider<TypingStateStore> typingStateStoreProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<Context> contextProvider,
      Provider<String> apiBaseUrlProvider) {
    this.apiProvider = apiProvider;
    this.localDataSourceProvider = localDataSourceProvider;
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
    this.typingStateStoreProvider = typingStateStoreProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.contextProvider = contextProvider;
    this.apiBaseUrlProvider = apiBaseUrlProvider;
  }

  @Override
  public NetworkChatRepository get() {
    return newInstance(apiProvider.get(), localDataSourceProvider.get(), emergencyWipeStoreProvider.get(), typingStateStoreProvider.get(), okHttpClientProvider.get(), contextProvider.get(), apiBaseUrlProvider.get());
  }

  public static NetworkChatRepository_Factory create(Provider<KChatApi> apiProvider,
      Provider<ChatLocalDataSource> localDataSourceProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider,
      Provider<TypingStateStore> typingStateStoreProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<Context> contextProvider,
      Provider<String> apiBaseUrlProvider) {
    return new NetworkChatRepository_Factory(apiProvider, localDataSourceProvider, emergencyWipeStoreProvider, typingStateStoreProvider, okHttpClientProvider, contextProvider, apiBaseUrlProvider);
  }

  public static NetworkChatRepository newInstance(KChatApi api, ChatLocalDataSource localDataSource,
      EmergencyWipeStore emergencyWipeStore, TypingStateStore typingStateStore,
      OkHttpClient okHttpClient, Context context, String apiBaseUrl) {
    return new NetworkChatRepository(api, localDataSource, emergencyWipeStore, typingStateStore, okHttpClient, context, apiBaseUrl);
  }
}
