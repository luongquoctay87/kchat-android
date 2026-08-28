package com.kchat.di;

import com.kchat.data.fake.FakeChatRepository;
import com.kchat.data.network.repository.NetworkChatRepository;
import com.kchat.data.repository.ChatRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class RepositoryModule_ProvideChatRepositoryFactory implements Factory<ChatRepository> {
  private final Provider<FakeChatRepository> fakeProvider;

  private final Provider<NetworkChatRepository> networkProvider;

  public RepositoryModule_ProvideChatRepositoryFactory(Provider<FakeChatRepository> fakeProvider,
      Provider<NetworkChatRepository> networkProvider) {
    this.fakeProvider = fakeProvider;
    this.networkProvider = networkProvider;
  }

  @Override
  public ChatRepository get() {
    return provideChatRepository(fakeProvider.get(), networkProvider.get());
  }

  public static RepositoryModule_ProvideChatRepositoryFactory create(
      Provider<FakeChatRepository> fakeProvider, Provider<NetworkChatRepository> networkProvider) {
    return new RepositoryModule_ProvideChatRepositoryFactory(fakeProvider, networkProvider);
  }

  public static ChatRepository provideChatRepository(FakeChatRepository fake,
      NetworkChatRepository network) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideChatRepository(fake, network));
  }
}
