package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.CallSignalBus;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class IncomingCallViewModel_Factory implements Factory<IncomingCallViewModel> {
  private final Provider<CallSignalBus> busProvider;

  public IncomingCallViewModel_Factory(Provider<CallSignalBus> busProvider) {
    this.busProvider = busProvider;
  }

  @Override
  public IncomingCallViewModel get() {
    return newInstance(busProvider.get());
  }

  public static IncomingCallViewModel_Factory create(Provider<CallSignalBus> busProvider) {
    return new IncomingCallViewModel_Factory(busProvider);
  }

  public static IncomingCallViewModel newInstance(CallSignalBus bus) {
    return new IncomingCallViewModel(bus);
  }
}
