package com.kchat.di;

import com.kchat.data.fake.FakeRealtimeCoordinator;
import com.kchat.data.network.repository.NetworkRealtimeCoordinator;
import com.kchat.data.repository.RealtimeCoordinator;
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
public final class RepositoryModule_ProvideRealtimeCoordinatorFactory implements Factory<RealtimeCoordinator> {
  private final Provider<FakeRealtimeCoordinator> fakeProvider;

  private final Provider<NetworkRealtimeCoordinator> networkProvider;

  public RepositoryModule_ProvideRealtimeCoordinatorFactory(
      Provider<FakeRealtimeCoordinator> fakeProvider,
      Provider<NetworkRealtimeCoordinator> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public RealtimeCoordinator get() {
    return provideRealtimeCoordinator(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideRealtimeCoordinatorFactory create(
      Provider<FakeRealtimeCoordinator> fakeProvider,
      Provider<NetworkRealtimeCoordinator> networkProvider) {
    return new RepositoryModule_ProvideRealtimeCoordinatorFactory(fakeProvider, networkProvider);
  }

  public static RealtimeCoordinator provideRealtimeCoordinator(FakeRealtimeCoordinator fake,
      NetworkRealtimeCoordinator network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideRealtimeCoordinator(fake, network));
  }
}
