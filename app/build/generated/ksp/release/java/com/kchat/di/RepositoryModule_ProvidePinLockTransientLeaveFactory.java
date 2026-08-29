package com.kchat.di;

import com.kchat.data.repository.PinLockTransientLeave;
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
public final class RepositoryModule_ProvidePinLockTransientLeaveFactory implements Factory<PinLockTransientLeave> {
  @Override
  public PinLockTransientLeave get() {
    return providePinLockTransientLeave();
  }

  public static RepositoryModule_ProvidePinLockTransientLeaveFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PinLockTransientLeave providePinLockTransientLeave() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.providePinLockTransientLeave());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvidePinLockTransientLeaveFactory INSTANCE = new RepositoryModule_ProvidePinLockTransientLeaveFactory();
  }
}
