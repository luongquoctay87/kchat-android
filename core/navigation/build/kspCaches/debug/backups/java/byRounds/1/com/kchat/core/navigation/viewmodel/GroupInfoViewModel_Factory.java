package com.kchat.core.navigation.viewmodel;

import androidx.lifecycle.SavedStateHandle;
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
public final class GroupInfoViewModel_Factory implements Factory<GroupInfoViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  public GroupInfoViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
  }

  @Override
  public GroupInfoViewModel get() {
    return newInstance(savedStateHandleProvider.get(), chatRepositoryProvider.get(), contactsRepositoryProvider.get());
  }

  public static GroupInfoViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider) {
    return new GroupInfoViewModel_Factory(savedStateHandleProvider, chatRepositoryProvider, contactsRepositoryProvider);
  }

  public static GroupInfoViewModel newInstance(SavedStateHandle savedStateHandle,
      ChatRepository chatRepository, ContactsRepository contactsRepository) {
    return new GroupInfoViewModel(savedStateHandle, chatRepository, contactsRepository);
  }
}
