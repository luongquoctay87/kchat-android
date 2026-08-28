package com.kchat.di;

import com.kchat.data.network.auth.TokenAuthenticator;
import com.kchat.data.repository.AccessTokenHolder;
import com.kchat.data.repository.DeviceTokenStore;
import com.kchat.data.repository.TokenStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<DeviceTokenStore> deviceTokenStoreProvider;

  private final Provider<TokenAuthenticator> tokenAuthenticatorProvider;

  public NetworkModule_ProvideOkHttpClientFactory(
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    this.accessTokenHolderProvider = accessTokenHolderProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.deviceTokenStoreProvider = deviceTokenStoreProvider;
    this.tokenAuthenticatorProvider = tokenAuthenticatorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(accessTokenHolderProvider.get(), tokenStoreProvider.get(), deviceTokenStoreProvider.get(), tokenAuthenticatorProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(accessTokenHolderProvider, tokenStoreProvider, deviceTokenStoreProvider, tokenAuthenticatorProvider);
  }

  public static OkHttpClient provideOkHttpClient(AccessTokenHolder accessTokenHolder,
      TokenStore tokenStore, DeviceTokenStore deviceTokenStore,
      TokenAuthenticator tokenAuthenticator) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOkHttpClient(accessTokenHolder, tokenStore, deviceTokenStore, tokenAuthenticator));
  }
}
