package com.kchat.di;

import com.kchat.data.fake.FakeTokenStore;
import com.kchat.data.network.auth.DataStoreTokenStore;
import com.kchat.data.repository.TokenStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class RepositoryModule_ProvideTokenStoreFactory implements Factory<TokenStore> {
  private final Provider<FakeTokenStore> fakeProvider;

  private final Provider<DataStoreTokenStore> networkProvider;

  public RepositoryModule_ProvideTokenStoreFactory(Provider<FakeTokenStore> fakeProvider,
      Provider<DataStoreTokenStore> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public TokenStore get() {
    return provideTokenStore(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideTokenStoreFactory create(
      Provider<FakeTokenStore> fakeProvider, Provider<DataStoreTokenStore> networkProvider) {
    return new RepositoryModule_ProvideTokenStoreFactory(fakeProvider, networkProvider);
  }

  public static TokenStore provideTokenStore(FakeTokenStore fake, DataStoreTokenStore network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideTokenStore(fake, network));
  }
}
