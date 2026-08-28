package com.kchat.data.fake;

import com.kchat.data.repository.AccessTokenHolder;
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
public final class FakeTokenStore_Factory implements Factory<FakeTokenStore> {
  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  public FakeTokenStore_Factory(Provider<AccessTokenHolder> accessTokenHolderProvider) {
    this.accessTokenHolderProvider = accessTokenHolderProvider;
  }

  @Override
  public FakeTokenStore get() {
    return newInstance(accessTokenHolderProvider.get());
  }

  public static FakeTokenStore_Factory create(
      Provider<AccessTokenHolder> accessTokenHolderProvider) {
    return new FakeTokenStore_Factory(accessTokenHolderProvider);
  }

  public static FakeTokenStore newInstance(AccessTokenHolder accessTokenHolder) {
    return new FakeTokenStore(accessTokenHolder);
  }
}
