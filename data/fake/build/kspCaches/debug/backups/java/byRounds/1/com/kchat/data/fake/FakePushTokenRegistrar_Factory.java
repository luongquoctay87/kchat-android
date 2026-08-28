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
public final class FakePushTokenRegistrar_Factory implements Factory<FakePushTokenRegistrar> {
  @Override
  public FakePushTokenRegistrar get() {
    return newInstance();
  }

  public static FakePushTokenRegistrar_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakePushTokenRegistrar newInstance() {
    return new FakePushTokenRegistrar();
  }

  private static final class InstanceHolder {
    private static final FakePushTokenRegistrar_Factory INSTANCE = new FakePushTokenRegistrar_Factory();
  }
}
