package com.kchat.di;

import com.kchat.data.fake.FakePinLockStore;
import com.kchat.data.network.auth.DataStorePinLockStore;
import com.kchat.data.repository.PinLockStore;
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
public final class RepositoryModule_ProvidePinLockStoreFactory implements Factory<PinLockStore> {
  private final Provider<FakePinLockStore> fakeProvider;

  private final Provider<DataStorePinLockStore> networkProvider;

  public RepositoryModule_ProvidePinLockStoreFactory(Provider<FakePinLockStore> fakeProvider,
      Provider<DataStorePinLockStore> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public PinLockStore get() {
    return providePinLockStore(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvidePinLockStoreFactory create(
      Provider<FakePinLockStore> fakeProvider, Provider<DataStorePinLockStore> networkProvider) {
    return new RepositoryModule_ProvidePinLockStoreFactory(fakeProvider, networkProvider);
  }

  public static PinLockStore providePinLockStore(FakePinLockStore fake,
      DataStorePinLockStore network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.providePinLockStore(fake, network));
  }
}
