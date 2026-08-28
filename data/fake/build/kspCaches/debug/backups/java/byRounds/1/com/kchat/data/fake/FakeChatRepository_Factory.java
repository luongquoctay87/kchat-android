package com.kchat.data.fake;

import com.kchat.data.repository.EmergencyWipeStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class FakeChatRepository_Factory implements Factory<FakeChatRepository> {
  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  public FakeChatRepository_Factory(Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
  }

  @Override
  public FakeChatRepository get() {
    return newInstance(emergencyWipeStoreProvider.get());
  }

  public static FakeChatRepository_Factory create(
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    return new FakeChatRepository_Factory(emergencyWipeStoreProvider);
  }

  public static FakeChatRepository newInstance(EmergencyWipeStore emergencyWipeStore) {
    return new FakeChatRepository(emergencyWipeStore);
  }
}
