package com.kchat.data.fake;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NoOpPushTokenSync_Factory implements Factory<NoOpPushTokenSync> {
  @Override
  public NoOpPushTokenSync get() {
    return newInstance();
  }

  public static NoOpPushTokenSync_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NoOpPushTokenSync newInstance() {
    return new NoOpPushTokenSync();
  }

  private static final class InstanceHolder {
    private static final NoOpPushTokenSync_Factory INSTANCE = new NoOpPushTokenSync_Factory();
  }
}
