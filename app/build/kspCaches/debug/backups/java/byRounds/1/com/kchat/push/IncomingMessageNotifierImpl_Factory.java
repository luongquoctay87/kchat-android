package com.kchat.push;

import com.kchat.data.repository.ActiveRoomTracker;
import com.kchat.data.repository.AppForegroundTracker;
import com.kchat.data.repository.SettingsRepository;
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
public final class IncomingMessageNotifierImpl_Factory implements Factory<IncomingMessageNotifierImpl> {
  private final Provider<PushNotificationHelper> pushNotificationHelperProvider;

  private final Provider<AppForegroundTracker> appForegroundTrackerProvider;

  private final Provider<ActiveRoomTracker> activeRoomTrackerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public IncomingMessageNotifierImpl_Factory(
      Provider<PushNotificationHelper> pushNotificationHelperProvider,
      Provider<AppForegroundTracker> appForegroundTrackerProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.pushNotificationHelperProvider = pushNotificationHelperProvider;
    this.appForegroundTrackerProvider = appForegroundTrackerProvider;
    this.activeRoomTrackerProvider = activeRoomTrackerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public IncomingMessageNotifierImpl get() {
    return newInstance(pushNotificationHelperProvider.get(), appForegroundTrackerProvider.get(), activeRoomTrackerProvider.get(), settingsRepositoryProvider.get());
  }

  public static IncomingMessageNotifierImpl_Factory create(
      Provider<PushNotificationHelper> pushNotificationHelperProvider,
      Provider<AppForegroundTracker> appForegroundTrackerProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new IncomingMessageNotifierImpl_Factory(pushNotificationHelperProvider, appForegroundTrackerProvider, activeRoomTrackerProvider, settingsRepositoryProvider);
  }

  public static IncomingMessageNotifierImpl newInstance(
      PushNotificationHelper pushNotificationHelper, AppForegroundTracker appForegroundTracker,
      ActiveRoomTracker activeRoomTracker, SettingsRepository settingsRepository) {
    return new IncomingMessageNotifierImpl(pushNotificationHelper, appForegroundTracker, activeRoomTracker, settingsRepository);
  }
}
