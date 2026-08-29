package com.kchat.di;

import com.kchat.data.repository.TypingStateStore;
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
public final class RepositoryModule_ProvideTypingStateStoreFactory implements Factory<TypingStateStore> {
  @Override
  public TypingStateStore get() {
    return provideTypingStateStore();
  }

  public static RepositoryModule_ProvideTypingStateStoreFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TypingStateStore provideTypingStateStore() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideTypingStateStore());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvideTypingStateStoreFactory INSTANCE = new RepositoryModule_ProvideTypingStateStoreFactory();
  }
}
