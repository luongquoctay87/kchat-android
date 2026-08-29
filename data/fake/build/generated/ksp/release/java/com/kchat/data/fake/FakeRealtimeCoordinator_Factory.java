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
public final class FakeRealtimeCoordinator_Factory implements Factory<FakeRealtimeCoordinator> {
  @Override
  public FakeRealtimeCoordinator get() {
    return newInstance();
  }

  public static FakeRealtimeCoordinator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeRealtimeCoordinator newInstance() {
    return new FakeRealtimeCoordinator();
  }

  private static final class InstanceHolder {
    private static final FakeRealtimeCoordinator_Factory INSTANCE = new FakeRealtimeCoordinator_Factory();
  }
}
