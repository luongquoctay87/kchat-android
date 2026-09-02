package com.kchat.push;

import com.kchat.data.repository.ActiveRoomTracker;
import com.kchat.data.repository.AppForegroundTracker;
import com.kchat.data.repository.EmergencyWipeStore;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class KChatFirebaseMessagingService_MembersInjector implements MembersInjector<KChatFirebaseMessagingService> {
  private final Provider<FcmTokenHandler> fcmTokenHandlerProvider;

  private final Provider<PushNotificationHelper> pushNotificationHelperProvider;

  private final Provider<ActiveRoomTracker> activeRoomTrackerProvider;

  private final Provider<AppForegroundTracker> appForegroundTrackerProvider;

  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  public KChatFirebaseMessagingService_MembersInjector(
      Provider<FcmTokenHandler> fcmTokenHandlerProvider,
      Provider<PushNotificationHelper> pushNotificationHelperProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<AppForegroundTracker> appForegroundTrackerProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    this.fcmTokenHandlerProvider = fcmTokenHandlerProvider;
    this.pushNotificationHelperProvider = pushNotificationHelperProvider;
    this.activeRoomTrackerProvider = activeRoomTrackerProvider;
    this.appForegroundTrackerProvider = appForegroundTrackerProvider;
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
  }

  public static MembersInjector<KChatFirebaseMessagingService> create(
      Provider<FcmTokenHandler> fcmTokenHandlerProvider,
      Provider<PushNotificationHelper> pushNotificationHelperProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<AppForegroundTracker> appForegroundTrackerProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    return new KChatFirebaseMessagingService_MembersInjector(fcmTokenHandlerProvider, pushNotificationHelperProvider, activeRoomTrackerProvider, appForegroundTrackerProvider, emergencyWipeStoreProvider);
  }

  @Override
  public void injectMembers(KChatFirebaseMessagingService instance) {
    injectFcmTokenHandler(instance, fcmTokenHandlerProvider.get());
    injectPushNotificationHelper(instance, pushNotificationHelperProvider.get());
    injectActiveRoomTracker(instance, activeRoomTrackerProvider.get());
    injectAppForegroundTracker(instance, appForegroundTrackerProvider.get());
    injectEmergencyWipeStore(instance, emergencyWipeStoreProvider.get());
  }

  @InjectedFieldSignature("com.kchat.push.KChatFirebaseMessagingService.fcmTokenHandler")
  public static void injectFcmTokenHandler(KChatFirebaseMessagingService instance,
      FcmTokenHandler fcmTokenHandler) {
    instance.fcmTokenHandler = fcmTokenHandler;
  }

  @InjectedFieldSignature("com.kchat.push.KChatFirebaseMessagingService.pushNotificationHelper")
  public static void injectPushNotificationHelper(KChatFirebaseMessagingService instance,
      PushNotificationHelper pushNotificationHelper) {
    instance.pushNotificationHelper = pushNotificationHelper;
  }

  @InjectedFieldSignature("com.kchat.push.KChatFirebaseMessagingService.activeRoomTracker")
  public static void injectActiveRoomTracker(KChatFirebaseMessagingService instance,
      ActiveRoomTracker activeRoomTracker) {
    instance.activeRoomTracker = activeRoomTracker;
  }

  @InjectedFieldSignature("com.kchat.push.KChatFirebaseMessagingService.appForegroundTracker")
  public static void injectAppForegroundTracker(KChatFirebaseMessagingService instance,
      AppForegroundTracker appForegroundTracker) {
    instance.appForegroundTracker = appForegroundTracker;
  }

  @InjectedFieldSignature("com.kchat.push.KChatFirebaseMessagingService.emergencyWipeStore")
  public static void injectEmergencyWipeStore(KChatFirebaseMessagingService instance,
      EmergencyWipeStore emergencyWipeStore) {
    instance.emergencyWipeStore = emergencyWipeStore;
  }
}
