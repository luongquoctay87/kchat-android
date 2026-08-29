package com.kchat.di;

import com.kchat.data.repository.AccessTokenHolder;
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
public final class RepositoryModule_ProvideAccessTokenHolderFactory implements Factory<AccessTokenHolder> {
  @Override
  public AccessTokenHolder get() {
    return provideAccessTokenHolder();
  }

  public static RepositoryModule_ProvideAccessTokenHolderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AccessTokenHolder provideAccessTokenHolder() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideAccessTokenHolder());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvideAccessTokenHolderFactory INSTANCE = new RepositoryModule_ProvideAccessTokenHolderFactory();
  }
}
