package com.kchat.di;

import com.kchat.data.fake.FakePushTokenRegistrar;
import com.kchat.data.network.repository.NetworkPushTokenRegistrar;
import com.kchat.data.repository.PushTokenRegistrar;
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
public final class RepositoryModule_ProvidePushTokenRegistrarFactory implements Factory<PushTokenRegistrar> {
  private final Provider<FakePushTokenRegistrar> fakeProvider;

  private final Provider<NetworkPushTokenRegistrar> networkProvider;

  public RepositoryModule_ProvidePushTokenRegistrarFactory(
      Provider<FakePushTokenRegistrar> fakeProvider,
      Provider<NetworkPushTokenRegistrar> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public PushTokenRegistrar get() {
    return providePushTokenRegistrar(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvidePushTokenRegistrarFactory create(
      Provider<FakePushTokenRegistrar> fakeProvider,
      Provider<NetworkPushTokenRegistrar> networkProvider) {
    return new RepositoryModule_ProvidePushTokenRegistrarFactory(fakeProvider, networkProvider);
  }

  public static PushTokenRegistrar providePushTokenRegistrar(FakePushTokenRegistrar fake,
      NetworkPushTokenRegistrar network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.providePushTokenRegistrar(fake, network));
  }
}
