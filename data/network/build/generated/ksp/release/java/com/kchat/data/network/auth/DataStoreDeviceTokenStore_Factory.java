package com.kchat.data.network.auth;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DataStoreDeviceTokenStore_Factory implements Factory<DataStoreDeviceTokenStore> {
  private final Provider<Context> contextProvider;

  public DataStoreDeviceTokenStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataStoreDeviceTokenStore get() {
    return newInstance(contextProvider.get());
  }

  public static DataStoreDeviceTokenStore_Factory create(Provider<Context> contextProvider) {
    return new DataStoreDeviceTokenStore_Factory(contextProvider);
  }

  public static DataStoreDeviceTokenStore newInstance(Context context) {
    return new DataStoreDeviceTokenStore(context);
  }
}
