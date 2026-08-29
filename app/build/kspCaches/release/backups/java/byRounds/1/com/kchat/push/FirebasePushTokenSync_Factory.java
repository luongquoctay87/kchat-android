package com.kchat.push;

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
public final class FirebasePushTokenSync_Factory implements Factory<FirebasePushTokenSync> {
  private final Provider<FcmTokenHandler> fcmTokenHandlerProvider;

  public FirebasePushTokenSync_Factory(Provider<FcmTokenHandler> fcmTokenHandlerProvider) {
    this.fcmTokenHandlerProvider = fcmTokenHandlerProvider;
  }

  @Override
  public FirebasePushTokenSync get() {
    return newInstance(fcmTokenHandlerProvider.get());
  }

  public static FirebasePushTokenSync_Factory create(
      Provider<FcmTokenHandler> fcmTokenHandlerProvider) {
    return new FirebasePushTokenSync_Factory(fcmTokenHandlerProvider);
  }

  public static FirebasePushTokenSync newInstance(FcmTokenHandler fcmTokenHandler) {
    return new FirebasePushTokenSync(fcmTokenHandler);
  }
}
