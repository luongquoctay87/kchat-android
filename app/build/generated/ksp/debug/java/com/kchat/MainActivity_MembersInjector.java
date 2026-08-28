package com.kchat;

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

  public MainActivity_MembersInjector(Provider<PushNavigationStore> pushNavigationStoreProvider,
      Provider<PushNotificationHelper> pushNotificationHelperProvider) {
    this.pushNavigationStoreProvider = pushNavigationStoreProvider;
    this.pushNotificationHelperProvider = pushNotificationHelperProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<PushNavigationStore> pushNavigationStoreProvider,
      Provider<PushNotificationHelper> pushNotificationHelperProvider) {
    return new MainActivity_MembersInjector(pushNavigationStoreProvider, pushNotificationHelperProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectPushNavigationStore(instance, pushNavigationStoreProvider.get());
    injectPushNotificationHelper(instance, pushNotificationHelperProvider.get());
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
}
