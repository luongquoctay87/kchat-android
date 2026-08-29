package com.kchat.di;

import com.kchat.data.fake.NoOpPushTokenSync;
import com.kchat.data.repository.PushTokenSync;
import com.kchat.push.FirebasePushTokenSync;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RepositoryModule_ProvidePushTokenSyncFactory implements Factory<PushTokenSync> {
  private final Provider<FirebasePushTokenSync> firebaseProvider;

  private final Provider<NoOpPushTokenSync> noopProvider;

  public RepositoryModule_ProvidePushTokenSyncFactory(
      Provider<FirebasePushTokenSync> firebaseProvider, Provider<NoOpPushTokenSync> noopProvider) {
    this.firebaseProvider = firebaseProvider;
    this.noopProvider = noopProvider;
  }

  @Override
  public PushTokenSync get() {
    return providePushTokenSync(firebaseProvider.get(), noopProvider.get());
  }

  public static RepositoryModule_ProvidePushTokenSyncFactory create(
      Provider<FirebasePushTokenSync> firebaseProvider, Provider<NoOpPushTokenSync> noopProvider) {
    return new RepositoryModule_ProvidePushTokenSyncFactory(firebaseProvider, noopProvider);
  }

  public static PushTokenSync providePushTokenSync(FirebasePushTokenSync firebase,
      NoOpPushTokenSync noop) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.providePushTokenSync(firebase, noop));
  }
}
