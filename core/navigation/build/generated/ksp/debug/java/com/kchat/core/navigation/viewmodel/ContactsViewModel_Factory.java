package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.EmergencyWipeCoordinator;
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
public final class ContactsViewModel_Factory implements Factory<ContactsViewModel> {
  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<EmergencyWipeCoordinator> emergencyWipeCoordinatorProvider;

  public ContactsViewModel_Factory(Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeCoordinator> emergencyWipeCoordinatorProvider) {
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.emergencyWipeCoordinatorProvider = emergencyWipeCoordinatorProvider;
  }

  @Override
  public ContactsViewModel get() {
    return newInstance(contactsRepositoryProvider.get(), emergencyWipeCoordinatorProvider.get());
  }

  public static ContactsViewModel_Factory create(
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<EmergencyWipeCoordinator> emergencyWipeCoordinatorProvider) {
    return new ContactsViewModel_Factory(contactsRepositoryProvider, emergencyWipeCoordinatorProvider);
  }

  public static ContactsViewModel newInstance(ContactsRepository contactsRepository,
      EmergencyWipeCoordinator emergencyWipeCoordinator) {
    return new ContactsViewModel(contactsRepository, emergencyWipeCoordinator);
  }
}
