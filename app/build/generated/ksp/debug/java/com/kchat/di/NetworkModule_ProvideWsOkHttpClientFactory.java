package com.kchat.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
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
public final class NetworkModule_ProvideWsOkHttpClientFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return provideWsOkHttpClient();
  }

  public static NetworkModule_ProvideWsOkHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient provideWsOkHttpClient() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideWsOkHttpClient());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideWsOkHttpClientFactory INSTANCE = new NetworkModule_ProvideWsOkHttpClientFactory();
  }
}
