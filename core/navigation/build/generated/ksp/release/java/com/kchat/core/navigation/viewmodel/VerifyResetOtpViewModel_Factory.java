package com.kchat.core.navigation.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.kchat.data.repository.AuthRepository;
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
public final class VerifyResetOtpViewModel_Factory implements Factory<VerifyResetOtpViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public VerifyResetOtpViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public VerifyResetOtpViewModel get() {
    return newInstance(savedStateHandleProvider.get(), authRepositoryProvider.get());
  }

  public static VerifyResetOtpViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new VerifyResetOtpViewModel_Factory(savedStateHandleProvider, authRepositoryProvider);
  }

  public static VerifyResetOtpViewModel newInstance(SavedStateHandle savedStateHandle,
      AuthRepository authRepository) {
    return new VerifyResetOtpViewModel(savedStateHandle, authRepository);
  }
}
