package com.kchat.data.fake;

import com.kchat.data.repository.CallSignalBus;
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
public final class FakeCallRepository_Factory implements Factory<FakeCallRepository> {
  private final Provider<CallSignalBus> callSignalBusProvider;

  public FakeCallRepository_Factory(Provider<CallSignalBus> callSignalBusProvider) {
    this.callSignalBusProvider = callSignalBusProvider;
  }

  @Override
  public FakeCallRepository get() {
    return newInstance(callSignalBusProvider.get());
  }

  public static FakeCallRepository_Factory create(Provider<CallSignalBus> callSignalBusProvider) {
    return new FakeCallRepository_Factory(callSignalBusProvider);
  }

  public static FakeCallRepository newInstance(CallSignalBus callSignalBus) {
    return new FakeCallRepository(callSignalBus);
  }
}
