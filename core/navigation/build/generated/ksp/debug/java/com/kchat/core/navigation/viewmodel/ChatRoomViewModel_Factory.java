package com.kchat.core.navigation.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.kchat.data.repository.ActiveRoomTracker;
import com.kchat.data.repository.ChatRepository;
import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.RealtimeCoordinator;
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
public final class ChatRoomViewModel_Factory implements Factory<ChatRoomViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<ActiveRoomTracker> activeRoomTrackerProvider;

  private final Provider<RealtimeCoordinator> realtimeCoordinatorProvider;

  public ChatRoomViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.activeRoomTrackerProvider = activeRoomTrackerProvider;
    this.realtimeCoordinatorProvider = realtimeCoordinatorProvider;
  }

  @Override
  public ChatRoomViewModel get() {
    return newInstance(savedStateHandleProvider.get(), chatRepositoryProvider.get(), contactsRepositoryProvider.get(), activeRoomTrackerProvider.get(), realtimeCoordinatorProvider.get());
  }

  public static ChatRoomViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<RealtimeCoordinator> realtimeCoordinatorProvider) {
    return new ChatRoomViewModel_Factory(savedStateHandleProvider, chatRepositoryProvider, contactsRepositoryProvider, activeRoomTrackerProvider, realtimeCoordinatorProvider);
  }

  public static ChatRoomViewModel newInstance(SavedStateHandle savedStateHandle,
      ChatRepository chatRepository, ContactsRepository contactsRepository,
      ActiveRoomTracker activeRoomTracker, RealtimeCoordinator realtimeCoordinator) {
    return new ChatRoomViewModel(savedStateHandle, chatRepository, contactsRepository, activeRoomTracker, realtimeCoordinator);
  }
}
