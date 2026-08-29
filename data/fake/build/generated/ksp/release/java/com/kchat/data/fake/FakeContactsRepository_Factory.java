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
public final class FakeContactsRepository_Factory implements Factory<FakeContactsRepository> {
  @Override
  public FakeContactsRepository get() {
    return newInstance();
  }

  public static FakeContactsRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeContactsRepository newInstance() {
    return new FakeContactsRepository();
  }

  private static final class InstanceHolder {
    private static final FakeContactsRepository_Factory INSTANCE = new FakeContactsRepository_Factory();
  }
}
