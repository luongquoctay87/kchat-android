package com.kchat.di;

import com.kchat.data.fake.FakeSettingsRepository;
import com.kchat.data.network.repository.NetworkSettingsRepository;
import com.kchat.data.repository.SettingsRepository;
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
public final class RepositoryModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepository> {
  private final Provider<FakeSettingsRepository> fakeProvider;

  private final Provider<NetworkSettingsRepository> networkProvider;

  public RepositoryModule_ProvideSettingsRepositoryFactory(
      Provider<FakeSettingsRepository> fakeProvider,
      Provider<NetworkSettingsRepository> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public SettingsRepository get() {
    return provideSettingsRepository(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideSettingsRepositoryFactory create(
      Provider<FakeSettingsRepository> fakeProvider,
      Provider<NetworkSettingsRepository> networkProvider) {
    return new RepositoryModule_ProvideSettingsRepositoryFactory(fakeProvider, networkProvider);
  }

  public static SettingsRepository provideSettingsRepository(FakeSettingsRepository fake,
      NetworkSettingsRepository network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideSettingsRepository(fake, network));
  }
}
