package com.kchat.di;

import com.kchat.data.fake.FakeAuthRepository;
import com.kchat.data.network.repository.NetworkAuthRepository;
import com.kchat.data.repository.AuthRepository;
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
public final class RepositoryModule_ProvideAuthRepositoryFactory implements Factory<AuthRepository> {
  private final Provider<FakeAuthRepository> fakeProvider;

  private final Provider<NetworkAuthRepository> networkProvider;

  public RepositoryModule_ProvideAuthRepositoryFactory(Provider<FakeAuthRepository> fakeProvider,
      Provider<NetworkAuthRepository> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public AuthRepository get() {
    return provideAuthRepository(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideAuthRepositoryFactory create(
      Provider<FakeAuthRepository> fakeProvider, Provider<NetworkAuthRepository> networkProvider) {
    return new RepositoryModule_ProvideAuthRepositoryFactory(fakeProvider, networkProvider);
  }

  public static AuthRepository provideAuthRepository(FakeAuthRepository fake,
      NetworkAuthRepository network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideAuthRepository(fake, network));
  }
}
