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
public final class FakeDeviceTokenStore_Factory implements Factory<FakeDeviceTokenStore> {
  @Override
  public FakeDeviceTokenStore get() {
    return newInstance();
  }

  public static FakeDeviceTokenStore_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeDeviceTokenStore newInstance() {
    return new FakeDeviceTokenStore();
  }

  private static final class InstanceHolder {
    private static final FakeDeviceTokenStore_Factory INSTANCE = new FakeDeviceTokenStore_Factory();
  }
}
