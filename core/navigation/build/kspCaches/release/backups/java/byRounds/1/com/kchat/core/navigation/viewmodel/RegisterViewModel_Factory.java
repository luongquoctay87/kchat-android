package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.AuthRepository;
import com.kchat.data.repository.SessionCoordinator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SessionCoordinator> sessionCoordinatorProvider;

  public RegisterViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<SessionCoordinator> sessionCoordinatorProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.sessionCoordinatorProvider = sessionCoordinatorProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(authRepositoryProvider.get(), sessionCoordinatorProvider.get());
  }

  public static RegisterViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<SessionCoordinator> sessionCoordinatorProvider) {
    return new RegisterViewModel_Factory(authRepositoryProvider, sessionCoordinatorProvider);
  }

  public static RegisterViewModel newInstance(AuthRepository authRepository,
      SessionCoordinator sessionCoordinator) {
    return new RegisterViewModel(authRepository, sessionCoordinator);
  }
}
