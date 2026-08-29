package com.kchat.core.navigation.viewmodel;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.kchat.data.repository.CallRepository;
import com.kchat.data.repository.CallSignalBus;
import com.kchat.data.repository.RealtimeCoordinator;
import com.kchat.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CallViewModel_Factory implements Factory<CallViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<CallRepository> callRepositoryProvider;

  private final Provider<CallSignalBus> callSignalBusProvider;

  private final Provider<RealtimeCoordinator> realtimeCoordinatorProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<Context> appContextProvider;

  public CallViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<CallSignalBus> callSignalBusProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<Context> appContextProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.callRepositoryProvider = callRepositoryProvider;
    this.callSignalBusProvider = callSignalBusProvider;
    this.realtimeCoordinatorProvider = realtimeCoordinatorProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public CallViewModel get() {
    return newInstance(savedStateHandleProvider.get(), callRepositoryProvider.get(), callSignalBusProvider.get(), realtimeCoordinatorProvider.get(), settingsRepositoryProvider.get(), appContextProvider.get());
  }

  public static CallViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<CallSignalBus> callSignalBusProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<Context> appContextProvider) {
    return new CallViewModel_Factory(savedStateHandleProvider, callRepositoryProvider, callSignalBusProvider, realtimeCoordinatorProvider, settingsRepositoryProvider, appContextProvider);
  }

  public static CallViewModel newInstance(SavedStateHandle savedStateHandle,
      CallRepository callRepository, CallSignalBus callSignalBus,
      RealtimeCoordinator realtimeCoordinator, SettingsRepository settingsRepository,
      Context appContext) {
    return new CallViewModel(savedStateHandle, callRepository, callSignalBus, realtimeCoordinator, settingsRepository, appContext);
  }
}
