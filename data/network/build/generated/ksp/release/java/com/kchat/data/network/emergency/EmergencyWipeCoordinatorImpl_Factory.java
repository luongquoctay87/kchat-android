package com.kchat.data.network.emergency;

import com.kchat.data.repository.ChatRepository;
import com.kchat.data.repository.ContactsRepository;
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
public final class EmergencyWipeCoordinatorImpl_Factory implements Factory<EmergencyWipeCoordinatorImpl> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<EmergencyWipeStore> emergencyWipeStoreProvider;

  public EmergencyWipeCoordinatorImpl_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.emergencyWipeStoreProvider = emergencyWipeStoreProvider;
  }

  @Override
  public EmergencyWipeCoordinatorImpl get() {
    return newInstance(chatRepositoryProvider.get(), contactsRepositoryProvider.get(), emergencyWipeStoreProvider.get());
  }

  public static EmergencyWipeCoordinatorImpl_Factory create(
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeStore> emergencyWipeStoreProvider) {
    return new EmergencyWipeCoordinatorImpl_Factory(chatRepositoryProvider, contactsRepositoryProvider, emergencyWipeStoreProvider);
  }

  public static EmergencyWipeCoordinatorImpl newInstance(ChatRepository chatRepository,
      ContactsRepository contactsRepository, EmergencyWipeStore emergencyWipeStore) {
    return new EmergencyWipeCoordinatorImpl(chatRepository, contactsRepository, emergencyWipeStore);
  }
}
