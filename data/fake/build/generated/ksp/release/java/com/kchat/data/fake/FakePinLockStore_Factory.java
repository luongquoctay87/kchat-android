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
public final class FakePinLockStore_Factory implements Factory<FakePinLockStore> {
  @Override
  public FakePinLockStore get() {
    return newInstance();
  }

  public static FakePinLockStore_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakePinLockStore newInstance() {
    return new FakePinLockStore();
  }

  private static final class InstanceHolder {
    private static final FakePinLockStore_Factory INSTANCE = new FakePinLockStore_Factory();
  }
}
