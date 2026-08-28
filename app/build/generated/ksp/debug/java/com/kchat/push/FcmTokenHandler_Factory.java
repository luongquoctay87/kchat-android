package com.kchat.push;

import com.kchat.data.repository.DeviceTokenStore;
import com.kchat.data.repository.PushTokenRegistrar;
import com.kchat.data.repository.TokenStore;
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
public final class FcmTokenHandler_Factory implements Factory<FcmTokenHandler> {
  private final Provider<PushTokenRegistrar> pushTokenRegistrarProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<DeviceTokenStore> deviceTokenStoreProvider;

  public FcmTokenHandler_Factory(Provider<PushTokenRegistrar> pushTokenRegistrarProvider,
      Provider<TokenStore> tokenStoreProvider,
      Provider<DeviceTokenStore> deviceTokenStoreProvider) {
    this.pushTokenRegistrarProvider = pushTokenRegistrarProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.deviceTokenStoreProvider = deviceTokenStoreProvider;
  }

  @Override
  public FcmTokenHandler get() {
    return newInstance(pushTokenRegistrarProvider.get(), tokenStoreProvider.get(), deviceTokenStoreProvider.get());
  }

  public static FcmTokenHandler_Factory create(
      Provider<PushTokenRegistrar> pushTokenRegistrarProvider,
      Provider<TokenStore> tokenStoreProvider,
      Provider<DeviceTokenStore> deviceTokenStoreProvider) {
    return new FcmTokenHandler_Factory(pushTokenRegistrarProvider, tokenStoreProvider, deviceTokenStoreProvider);
  }

  public static FcmTokenHandler newInstance(PushTokenRegistrar pushTokenRegistrar,
      TokenStore tokenStore, DeviceTokenStore deviceTokenStore) {
    return new FcmTokenHandler(pushTokenRegistrar, tokenStore, deviceTokenStore);
  }
}
