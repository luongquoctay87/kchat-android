package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.ActiveRoomTracker;
import com.kchat.data.repository.AppForegroundTracker;
import com.kchat.data.repository.AuthRepository;
import com.kchat.data.repository.PinLockStore;
import com.kchat.data.repository.PinLockTransientLeave;
import com.kchat.data.repository.RealtimeCoordinator;
import com.kchat.data.repository.SessionCoordinator;
import com.kchat.data.repository.TokenStore;
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
public final class SessionViewModel_Factory implements Factory<SessionViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<SessionCoordinator> sessionCoordinatorProvider;

  private final Provider<RealtimeCoordinator> realtimeCoordinatorProvider;

  private final Provider<PinLockStore> pinLockStoreProvider;

  private final Provider<PinLockTransientLeave> pinLockTransientLeaveProvider;

  private final Provider<AppForegroundTracker> appForegroundTrackerProvider;

  private final Provider<ActiveRoomTracker> activeRoomTrackerProvider;

  public SessionViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<TokenStore> tokenStoreProvider,
      Provider<SessionCoordinator> sessionCoordinatorProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<PinLockStore> pinLockStoreProvider,
      Provider<PinLockTransientLeave> pinLockTransientLeaveProvider,
      Provider<AppForegroundTracker> appForegroundTrackerProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.sessionCoordinatorProvider = sessionCoordinatorProvider;
    this.realtimeCoordinatorProvider = realtimeCoordinatorProvider;
    this.pinLockStoreProvider = pinLockStoreProvider;
    this.pinLockTransientLeaveProvider = pinLockTransientLeaveProvider;
    this.appForegroundTrackerProvider = appForegroundTrackerProvider;
    this.activeRoomTrackerProvider = activeRoomTrackerProvider;
  }

  @Override
  public SessionViewModel get() {
    return newInstance(authRepositoryProvider.get(), tokenStoreProvider.get(), sessionCoordinatorProvider.get(), realtimeCoordinatorProvider.get(), pinLockStoreProvider.get(), pinLockTransientLeaveProvider.get(), appForegroundTrackerProvider.get(), activeRoomTrackerProvider.get());
  }

  public static SessionViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<TokenStore> tokenStoreProvider,
      Provider<SessionCoordinator> sessionCoordinatorProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider,
      Provider<PinLockStore> pinLockStoreProvider,
      Provider<PinLockTransientLeave> pinLockTransientLeaveProvider,
      Provider<AppForegroundTracker> appForegroundTrackerProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider) {
    return new SessionViewModel_Factory(authRepositoryProvider, tokenStoreProvider, sessionCoordinatorProvider, realtimeCoordinatorProvider, pinLockStoreProvider, pinLockTransientLeaveProvider, appForegroundTrackerProvider, activeRoomTrackerProvider);
  }

  public static SessionViewModel newInstance(AuthRepository authRepository, TokenStore tokenStore,
      SessionCoordinator sessionCoordinator, RealtimeCoordinator realtimeCoordinator,
      PinLockStore pinLockStore, PinLockTransientLeave pinLockTransientLeave,
      AppForegroundTracker appForegroundTracker, ActiveRoomTracker activeRoomTracker) {
    return new SessionViewModel(authRepository, tokenStore, sessionCoordinator, realtimeCoordinator, pinLockStore, pinLockTransientLeave, appForegroundTracker, activeRoomTracker);
  }
}
