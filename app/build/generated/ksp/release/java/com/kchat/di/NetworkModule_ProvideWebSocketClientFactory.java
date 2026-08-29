package com.kchat.di;

import com.kchat.data.network.ws.KChatWebSocketClient;
import com.kchat.data.repository.AccessTokenHolder;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideWebSocketClientFactory implements Factory<KChatWebSocketClient> {
  private final Provider<OkHttpClient> wsClientProvider;

  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  public NetworkModule_ProvideWebSocketClientFactory(Provider<OkHttpClient> wsClientProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider) {
    this.wsClientProvider = wsClientProvider;
    this.accessTokenHolderProvider = accessTokenHolderProvider;
  }

  @Override
  public KChatWebSocketClient get() {
    return provideWebSocketClient(wsClientProvider.get(), accessTokenHolderProvider.get());
  }

  public static NetworkModule_ProvideWebSocketClientFactory create(
      Provider<OkHttpClient> wsClientProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider) {
    return new NetworkModule_ProvideWebSocketClientFactory(wsClientProvider, accessTokenHolderProvider);
  }

  public static KChatWebSocketClient provideWebSocketClient(OkHttpClient wsClient,
      AccessTokenHolder accessTokenHolder) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideWebSocketClient(wsClient, accessTokenHolder));
  }
}
