package com.kchat.data.network.auth;

import android.content.Context;
import com.kchat.data.repository.AccessTokenHolder;
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
public final class DataStoreTokenStore_Factory implements Factory<DataStoreTokenStore> {
  private final Provider<Context> contextProvider;

  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  public DataStoreTokenStore_Factory(Provider<Context> contextProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider) {
    this.contextProvider = contextProvider;
    this.accessTokenHolderProvider = accessTokenHolderProvider;
  }

  @Override
  public DataStoreTokenStore get() {
    return newInstance(contextProvider.get(), accessTokenHolderProvider.get());
  }

  public static DataStoreTokenStore_Factory create(Provider<Context> contextProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider) {
    return new DataStoreTokenStore_Factory(contextProvider, accessTokenHolderProvider);
  }

  public static DataStoreTokenStore newInstance(Context context,
      AccessTokenHolder accessTokenHolder) {
    return new DataStoreTokenStore(context, accessTokenHolder);
  }
}
