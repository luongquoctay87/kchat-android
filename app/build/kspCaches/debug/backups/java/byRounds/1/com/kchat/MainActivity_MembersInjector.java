package com.kchat;

import com.kchat.data.repository.PinLockTransientLeave;
import com.kchat.data.repository.PushNavigationStore;
import com.kchat.push.PushNotificationHelper;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<PushNavigationStore> pushNavigationStoreProvider;

  private final Provider<PushNotificationHelper> pushNotificationHelperProvider;

  private final Provider<PinLockTransientLeave> pinLockTransientLeaveProvider;

  public MainActivity_MembersInjector(Provider<PushNavigationStore> pushNavigationStoreProvider,
      Provider<PushNotificationHelper> pushNotificationHelperProvider,
      Provider<PinLockTransientLeave> pinLockTransientLeaveProvider) {
    this.pushNavigationStoreProvider = pushNavigationStoreProvider;
    this.pushNotificationHelperProvider = pushNotificationHelperProvider;
    this.pinLockTransientLeaveProvider = pinLockTransientLeaveProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<PushNavigationStore> pushNavigationStoreProvider,
      Provider<PushNotificationHelper> pushNotificationHelperProvider,
      Provider<PinLockTransientLeave> pinLockTransientLeaveProvider) {
    return new MainActivity_MembersInjector(pushNavigationStoreProvider, pushNotificationHelperProvider, pinLockTransientLeaveProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectPushNavigationStore(instance, pushNavigationStoreProvider.get());
    injectPushNotificationHelper(instance, pushNotificationHelperProvider.get());
    injectPinLockTransientLeave(instance, pinLockTransientLeaveProvider.get());
  }

  @InjectedFieldSignature("com.kchat.MainActivity.pushNavigationStore")
  public static void injectPushNavigationStore(MainActivity instance,
      PushNavigationStore pushNavigationStore) {
    instance.pushNavigationStore = pushNavigationStore;
  }

  @InjectedFieldSignature("com.kchat.MainActivity.pushNotificationHelper")
  public static void injectPushNotificationHelper(MainActivity instance,
      PushNotificationHelper pushNotificationHelper) {
    instance.pushNotificationHelper = pushNotificationHelper;
  }

  @InjectedFieldSignature("com.kchat.MainActivity.pinLockTransientLeave")
  public static void injectPinLockTransientLeave(MainActivity instance,
      PinLockTransientLeave pinLockTransientLeave) {
    instance.pinLockTransientLeave = pinLockTransientLeave;
  }
}
