package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.ChatRepository;
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
public final class CreateGroupViewModel_Factory implements Factory<CreateGroupViewModel> {
  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  public CreateGroupViewModel_Factory(Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<ChatRepository> chatRepositoryProvider) {
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public CreateGroupViewModel get() {
    return newInstance(contactsRepositoryProvider.get(), chatRepositoryProvider.get());
  }

  public static CreateGroupViewModel_Factory create(
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<ChatRepository> chatRepositoryProvider) {
    return new CreateGroupViewModel_Factory(contactsRepositoryProvider, chatRepositoryProvider);
  }

  public static CreateGroupViewModel newInstance(ContactsRepository contactsRepository,
      ChatRepository chatRepository) {
    return new CreateGroupViewModel(contactsRepository, chatRepository);
  }
}
