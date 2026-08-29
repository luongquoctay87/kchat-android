package com.kchat.push;

import android.content.Context;
import com.kchat.data.repository.PushNavigationStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PushNotificationHelper_Factory implements Factory<PushNotificationHelper> {
  private final Provider<Context> contextProvider;

  private final Provider<PushNavigationStore> pushNavigationStoreProvider;

  public PushNotificationHelper_Factory(Provider<Context> contextProvider,
      Provider<PushNavigationStore> pushNavigationStoreProvider) {
    this.contextProvider = contextProvider;
    this.pushNavigationStoreProvider = pushNavigationStoreProvider;
  }

  @Override
  public PushNotificationHelper get() {
    return newInstance(contextProvider.get(), pushNavigationStoreProvider.get());
  }

  public static PushNotificationHelper_Factory create(Provider<Context> contextProvider,
      Provider<PushNavigationStore> pushNavigationStoreProvider) {
    return new PushNotificationHelper_Factory(contextProvider, pushNavigationStoreProvider);
  }

  public static PushNotificationHelper newInstance(Context context,
      PushNavigationStore pushNavigationStore) {
    return new PushNotificationHelper(context, pushNavigationStore);
  }
}
