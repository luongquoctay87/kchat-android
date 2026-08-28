package com.kchat.di;

import com.kchat.data.fake.NoOpIncomingMessageNotifier;
import com.kchat.data.repository.IncomingMessageNotifier;
import com.kchat.push.IncomingMessageNotifierImpl;
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
public final class RepositoryModule_ProvideIncomingMessageNotifierFactory implements Factory<IncomingMessageNotifier> {
  private final Provider<NoOpIncomingMessageNotifier> noopProvider;

  private final Provider<IncomingMessageNotifierImpl> implProvider;

  public RepositoryModule_ProvideIncomingMessageNotifierFactory(
      Provider<NoOpIncomingMessageNotifier> noopProvider,
      Provider<IncomingMessageNotifierImpl> implProvider) {
    this.noopProvider = noopProvider;
    this.implProvider = implProvider;
  }

  @Override
  public IncomingMessageNotifier get() {
    return provideIncomingMessageNotifier(noopProvider.get(), implProvider.get());
  }

  public static RepositoryModule_ProvideIncomingMessageNotifierFactory create(
      Provider<NoOpIncomingMessageNotifier> noopProvider,
      Provider<IncomingMessageNotifierImpl> implProvider) {
    return new RepositoryModule_ProvideIncomingMessageNotifierFactory(noopProvider, implProvider);
  }

  public static IncomingMessageNotifier provideIncomingMessageNotifier(
      NoOpIncomingMessageNotifier noop, IncomingMessageNotifierImpl impl) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideIncomingMessageNotifier(noop, impl));
  }
}
