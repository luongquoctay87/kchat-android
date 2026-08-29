package com.kchat.data.network.repository;

import com.kchat.data.network.api.KChatApi;
import com.kchat.data.repository.EmergencyWipeStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NetworkContactsRepository_Factory implements Factory<NetworkContactsRepository> {
  private final Provider<KChatApi> apiProvider;

  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  private final Provider<String> apiBaseUrlProvider;

  public NetworkContactsRepository_Factory(Provider<KChatApi> apiProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider,
      Provider<String> apiBaseUrlProvider) {
    this.apiProvider = apiProvider;
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
    this.apiBaseUrlProvider = apiBaseUrlProvider;
  }

  @Override
  public NetworkContactsRepository get() {
    return newInstance(apiProvider.get(), emergencyWipeStoreProvider.get(), apiBaseUrlProvider.get());
  }

  public static NetworkContactsRepository_Factory create(Provider<KChatApi> apiProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider,
      Provider<String> apiBaseUrlProvider) {
    return new NetworkContactsRepository_Factory(apiProvider, emergencyWipeStoreProvider, apiBaseUrlProvider);
  }

  public static NetworkContactsRepository newInstance(KChatApi api,
      EmergencyWipeStore emergencyWipeStore, String apiBaseUrl) {
    return new NetworkContactsRepository(api, emergencyWipeStore, apiBaseUrl);
  }
}
