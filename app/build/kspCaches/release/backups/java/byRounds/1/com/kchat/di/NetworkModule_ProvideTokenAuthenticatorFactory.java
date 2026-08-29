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
public final class NetworkModule_ProvideTokenAuthenticatorFactory implements Factory<TokenAuthenticator> {
  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  private final Provider<DeviceTokenStore> deviceTokenStoreProvider;

  public NetworkModule_ProvideTokenAuthenticatorFactory(Provider<TokenStore> tokenStoreProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<DeviceTokenStore> deviceTokenStoreProvider) {
    this.tokenStoreProvider = tokenStoreProvider;
    this.accessTokenHolderProvider = accessTokenHolderProvider;
    this.deviceTokenStoreProvider = deviceTokenStoreProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return provideTokenAuthenticator(tokenStoreProvider.get(), accessTokenHolderProvider.get(), deviceTokenStoreProvider.get());
  }

  public static NetworkModule_ProvideTokenAuthenticatorFactory create(
      Provider<TokenStore> tokenStoreProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<DeviceTokenStore> deviceTokenStoreProvider) {
    return new NetworkModule_ProvideTokenAuthenticatorFactory(tokenStoreProvider, accessTokenHolderProvider, deviceTokenStoreProvider);
  }

  public static TokenAuthenticator provideTokenAuthenticator(TokenStore tokenStore,
      AccessTokenHolder accessTokenHolder, DeviceTokenStore deviceTokenStore) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTokenAuthenticator(tokenStore, accessTokenHolder, deviceTokenStore));
  }
}
