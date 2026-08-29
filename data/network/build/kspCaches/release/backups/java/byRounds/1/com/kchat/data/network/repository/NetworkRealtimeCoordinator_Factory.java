package com.kchat.data.network.repository;

import com.kchat.data.local.ChatLocalDataSource;
import com.kchat.data.network.api.KChatApi;
import com.kchat.data.network.ws.KChatWebSocketClient;
import com.kchat.data.repository.AccessTokenHolder;
import com.kchat.data.repository.ActiveRoomTracker;
import com.kchat.data.repository.CallRepository;
import com.kchat.data.repository.CallSignalBus;
import com.kchat.data.repository.ChatRepository;
import com.kchat.data.repository.ContactsRepository;
import com.kchat.data.repository.DeviceTokenStore;
import com.kchat.data.repository.IncomingMessageNotifier;
import com.kchat.data.repository.TokenStore;
import com.kchat.data.repository.TypingStateStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkRealtimeCoordinator_Factory implements Factory<NetworkRealtimeCoordinator> {
  private final Provider<KChatWebSocketClient> webSocketClientProvider;

  private final Provider<KChatApi> apiProvider;

  private final Provider<AccessTokenHolder> accessTokenHolderProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  private final Provider<DeviceTokenStore> deviceTokenStoreProvider;

  private final Provider<ChatLocalDataSource> localDataSourceProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ContactsRepository> contactsRepositoryProvider;

  private final Provider<ActiveRoomTracker> activeRoomTrackerProvider;

  private final Provider<TypingStateStore> typingStateStoreProvider;

  private final Provider<CallSignalBus> callSignalBusProvider;

  private final Provider<CallRepository> callRepositoryProvider;

  private final Provider<IncomingMessageNotifier> incomingMessageNotifierProvider;

  private final Provider<String> apiBaseUrlProvider;

  public NetworkRealtimeCoordinator_Factory(Provider<KChatWebSocketClient> webSocketClientProvider,
      Provider<KChatApi> apiProvider, Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<ChatLocalDataSource> localDataSourceProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<TypingStateStore> typingStateStoreProvider,
      Provider<CallSignalBus> callSignalBusProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<IncomingMessageNotifier> incomingMessageNotifierProvider,
      Provider<String> apiBaseUrlProvider) {
    this.webSocketClientProvider = webSocketClientProvider;
    this.apiProvider = apiProvider;
    this.accessTokenHolderProvider = accessTokenHolderProvider;
    this.tokenStoreProvider = tokenStoreProvider;
    this.deviceTokenStoreProvider = deviceTokenStoreProvider;
    this.localDataSourceProvider = localDataSourceProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.contactsRepositoryProvider = contactsRepositoryProvider;
    this.activeRoomTrackerProvider = activeRoomTrackerProvider;
    this.typingStateStoreProvider = typingStateStoreProvider;
    this.callSignalBusProvider = callSignalBusProvider;
    this.callRepositoryProvider = callRepositoryProvider;
    this.incomingMessageNotifierProvider = incomingMessageNotifierProvider;
    this.apiBaseUrlProvider = apiBaseUrlProvider;
  }

  @Override
  public NetworkRealtimeCoordinator get() {
    return newInstance(webSocketClientProvider.get(), apiProvider.get(), accessTokenHolderProvider.get(), tokenStoreProvider.get(), deviceTokenStoreProvider.get(), localDataSourceProvider.get(), chatRepositoryProvider.get(), contactsRepositoryProvider.get(), activeRoomTrackerProvider.get(), typingStateStoreProvider.get(), callSignalBusProvider.get(), callRepositoryProvider.get(), incomingMessageNotifierProvider.get(), apiBaseUrlProvider.get());
  }

  public static NetworkRealtimeCoordinator_Factory create(
      Provider<KChatWebSocketClient> webSocketClientProvider, Provider<KChatApi> apiProvider,
      Provider<AccessTokenHolder> accessTokenHolderProvider,
      Provider<TokenStore> tokenStoreProvider, Provider<DeviceTokenStore> deviceTokenStoreProvider,
      Provider<ChatLocalDataSource> localDataSourceProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ContactsRepository> contactsRepositoryProvider,
      Provider<ActiveRoomTracker> activeRoomTrackerProvider,
      Provider<TypingStateStore> typingStateStoreProvider,
      Provider<CallSignalBus> callSignalBusProvider,
      Provider<CallRepository> callRepositoryProvider,
      Provider<IncomingMessageNotifier> incomingMessageNotifierProvider,
      Provider<String> apiBaseUrlProvider) {
    return new NetworkRealtimeCoordinator_Factory(webSocketClientProvider, apiProvider, accessTokenHolderProvider, tokenStoreProvider, deviceTokenStoreProvider, localDataSourceProvider, chatRepositoryProvider, contactsRepositoryProvider, activeRoomTrackerProvider, typingStateStoreProvider, callSignalBusProvider, callRepositoryProvider, incomingMessageNotifierProvider, apiBaseUrlProvider);
  }

  public static NetworkRealtimeCoordinator newInstance(KChatWebSocketClient webSocketClient,
      KChatApi api, AccessTokenHolder accessTokenHolder, TokenStore tokenStore,
      DeviceTokenStore deviceTokenStore, ChatLocalDataSource localDataSource,
      ChatRepository chatRepository, ContactsRepository contactsRepository,
      ActiveRoomTracker activeRoomTracker, TypingStateStore typingStateStore,
      CallSignalBus callSignalBus, CallRepository callRepository,
      IncomingMessageNotifier incomingMessageNotifier, String apiBaseUrl) {
    return new NetworkRealtimeCoordinator(webSocketClient, api, accessTokenHolder, tokenStore, deviceTokenStore, localDataSource, chatRepository, contactsRepository, activeRoomTracker, typingStateStore, callSignalBus, callRepository, incomingMessageNotifier, apiBaseUrl);
  }
}
