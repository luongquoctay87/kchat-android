package com.kchat.di;

import com.kchat.data.network.api.KChatApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideKChatApiFactory implements Factory<KChatApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideKChatApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public KChatApi get() {
    return provideKChatApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideKChatApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideKChatApiFactory(retrofitProvider);
  }

  public static KChatApi provideKChatApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideKChatApi(retrofit));
  }
}
