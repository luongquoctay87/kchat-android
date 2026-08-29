package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.SettingsRepository;
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
public final class DevicesViewModel_Factory implements Factory<DevicesViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public DevicesViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DevicesViewModel get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static DevicesViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DevicesViewModel_Factory(settingsRepositoryProvider);
  }

  public static DevicesViewModel newInstance(SettingsRepository settingsRepository) {
    return new DevicesViewModel(settingsRepository);
  }
}
