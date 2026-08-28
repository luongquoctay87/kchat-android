package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.ContactsRepository;
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

  public ContactsViewModel_Factory(Provider<ContactsRepository> contactsRepositoryProvider) {
    this.contactsRepositoryProvider = contactsRepositoryProvider;
  }

  @Override
  public ContactsViewModel get() {
    return newInstance(contactsRepositoryProvider.get());
  }

  public static ContactsViewModel_Factory create(
      Provider<ContactsRepository> contactsRepositoryProvider) {
    return new ContactsViewModel_Factory(contactsRepositoryProvider);
  }

  public static ContactsViewModel newInstance(ContactsRepository contactsRepository) {
    return new ContactsViewModel(contactsRepository);
  }
}
