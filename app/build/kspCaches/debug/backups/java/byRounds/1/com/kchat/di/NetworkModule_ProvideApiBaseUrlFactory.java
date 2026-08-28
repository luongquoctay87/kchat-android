package com.kchat.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NetworkModule_ProvideApiBaseUrlFactory implements Factory<String> {
  @Override
  public String get() {
    return provideApiBaseUrl();
  }

  public static NetworkModule_ProvideApiBaseUrlFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static String provideApiBaseUrl() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideApiBaseUrl());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideApiBaseUrlFactory INSTANCE = new NetworkModule_ProvideApiBaseUrlFactory();
  }
}
