package com.kchat.di;

import com.kchat.data.fake.FakeDeviceTokenStore;
import com.kchat.data.network.auth.DataStoreDeviceTokenStore;
import com.kchat.data.repository.DeviceTokenStore;
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
public final class RepositoryModule_ProvideDeviceTokenStoreFactory implements Factory<DeviceTokenStore> {
  private final Provider<FakeDeviceTokenStore> fakeProvider;

  private final Provider<DataStoreDeviceTokenStore> networkProvider;

  public RepositoryModule_ProvideDeviceTokenStoreFactory(
      Provider<FakeDeviceTokenStore> fakeProvider,
      Provider<DataStoreDeviceTokenStore> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public DeviceTokenStore get() {
    return provideDeviceTokenStore(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideDeviceTokenStoreFactory create(
      Provider<FakeDeviceTokenStore> fakeProvider,
      Provider<DataStoreDeviceTokenStore> networkProvider) {
    return new RepositoryModule_ProvideDeviceTokenStoreFactory(fakeProvider, networkProvider);
  }

  public static DeviceTokenStore provideDeviceTokenStore(FakeDeviceTokenStore fake,
      DataStoreDeviceTokenStore network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideDeviceTokenStore(fake, network));
  }
}
