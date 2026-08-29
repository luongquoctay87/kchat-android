package com.kchat.data.network.repository;

import com.kchat.data.network.api.KChatApi;
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
public final class NetworkCallRepository_Factory implements Factory<NetworkCallRepository> {
  private final Provider<KChatApi> apiProvider;

  public NetworkCallRepository_Factory(Provider<KChatApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public NetworkCallRepository get() {
    return newInstance(apiProvider.get());
  }

  public static NetworkCallRepository_Factory create(Provider<KChatApi> apiProvider) {
    return new NetworkCallRepository_Factory(apiProvider);
  }

  public static NetworkCallRepository newInstance(KChatApi api) {
    return new NetworkCallRepository(api);
  }
}
