package com.kchat.di;

import com.kchat.data.repository.PushNavigationStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class RepositoryModule_ProvidePushNavigationStoreFactory implements Factory<PushNavigationStore> {
  @Override
  public PushNavigationStore get() {
    return providePushNavigationStore();
  }

  public static RepositoryModule_ProvidePushNavigationStoreFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PushNavigationStore providePushNavigationStore() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.providePushNavigationStore());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvidePushNavigationStoreFactory INSTANCE = new RepositoryModule_ProvidePushNavigationStoreFactory();
  }
}
