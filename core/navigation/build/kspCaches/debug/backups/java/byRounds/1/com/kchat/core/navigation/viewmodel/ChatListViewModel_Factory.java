package com.kchat.core.navigation.viewmodel;

import com.kchat.data.repository.ChatRepository;
import com.kchat.data.repository.TypingStateStore;
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
public final class ChatListViewModel_Factory implements Factory<ChatListViewModel> {
  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<TypingStateStore> typingStateStoreProvider;

  public ChatListViewModel_Factory(Provider<ChatRepository> chatRepositoryProvider,
      Provider<TypingStateStore> typingStateStoreProvider) {
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.typingStateStoreProvider = typingStateStoreProvider;
  }

  @Override
  public ChatListViewModel get() {
    return newInstance(chatRepositoryProvider.get(), typingStateStoreProvider.get());
  }

  public static ChatListViewModel_Factory create(Provider<ChatRepository> chatRepositoryProvider,
      Provider<TypingStateStore> typingStateStoreProvider) {
    return new ChatListViewModel_Factory(chatRepositoryProvider, typingStateStoreProvider);
  }

  public static ChatListViewModel newInstance(ChatRepository chatRepository,
      TypingStateStore typingStateStore) {
    return new ChatListViewModel(chatRepository, typingStateStore);
  }
}
