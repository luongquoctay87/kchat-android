package com.kchat.di;

import android.content.Context;
import coil.ImageLoader;
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
public final class NetworkModule_ProvideImageLoaderFactory implements Factory<ImageLoader> {
  private final Provider<Context> contextProvider;

  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<DeviceTokenStore> deviceTokenStoreProvider;

  private final Provider<TokenAuthenticator> tokenAuthenticatorProvider;

  public NetworkModule_ProvideImageLoaderFactory(Provider<Context> contextProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    this.contextProvider = contextProvider;
    this.accessTokenHolderProvider = accessTokenHolderProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.deviceTokenStoreProvider = deviceTokenStoreProvider;
    this.tokenAuthenticatorProvider = tokenAuthenticatorProvider;
  }

  @Override
  public ImageLoader get() {
    return provideImageLoader(contextProvider.get(), accessTokenHolderProvider.get(), tokenStoreProvider.get(), deviceTokenStoreProvider.get(), tokenAuthenticatorProvider.get());
  }

  public static NetworkModule_ProvideImageLoaderFactory create(Provider<Context> contextProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    return new NetworkModule_ProvideImageLoaderFactory(contextProvider, accessTokenHolderProvider, tokenStoreProvider, deviceTokenStoreProvider, tokenAuthenticatorProvider);
  }

  public static ImageLoader provideImageLoader(Context context, AccessTokenHolder accessTokenHolder,
      TokenStore tokenStore, DeviceTokenStore deviceTokenStore,
      TokenAuthenticator tokenAuthenticator) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideImageLoader(context, accessTokenHolder, tokenStore, deviceTokenStore, tokenAuthenticator));
  }
}
