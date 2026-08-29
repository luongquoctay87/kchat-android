package com.kchat.core.navigation.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.kchat.data.repository.ChatRepository;
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
public final class InChatSearchViewModel_Factory implements Factory<InChatSearchViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  public InChatSearchViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
  }

  @Override
  public InChatSearchViewModel get() {
    return newInstance(savedStateHandleProvider.get(), chatRepositoryProvider.get());
  }

  public static InChatSearchViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider) {
    return new InChatSearchViewModel_Factory(savedStateHandleProvider, chatRepositoryProvider);
  }

  public static InChatSearchViewModel newInstance(SavedStateHandle savedStateHandle,
      ChatRepository chatRepository) {
    return new InChatSearchViewModel(savedStateHandle, chatRepository);
  }
}
