package com.kchat.di;

import com.kchat.data.fake.FakeCallRepository;
import com.kchat.data.network.repository.NetworkCallRepository;
import com.kchat.data.repository.CallRepository;
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
public final class RepositoryModule_ProvideCallRepositoryFactory implements Factory<CallRepository> {
  private final Provider<FakeCallRepository> fakeProvider;

  private final Provider<NetworkCallRepository> networkProvider;

  public RepositoryModule_ProvideCallRepositoryFactory(Provider<FakeCallRepository> fakeProvider,
      Provider<NetworkCallRepository> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public CallRepository get() {
    return provideCallRepository(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideCallRepositoryFactory create(
      Provider<FakeCallRepository> fakeProvider, Provider<NetworkCallRepository> networkProvider) {
    return new RepositoryModule_ProvideCallRepositoryFactory(fakeProvider, networkProvider);
  }

  public static CallRepository provideCallRepository(FakeCallRepository fake,
      NetworkCallRepository network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideCallRepository(fake, network));
  }
}
