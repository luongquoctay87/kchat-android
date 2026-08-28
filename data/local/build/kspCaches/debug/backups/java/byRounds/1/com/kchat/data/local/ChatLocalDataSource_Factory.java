package com.kchat.data.local;

import com.kchat.data.local.dao.MessageDao;
import com.kchat.data.local.dao.RoomDao;
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
public final class ChatLocalDataSource_Factory implements Factory<ChatLocalDataSource> {
  private final Provider<RoomDao> roomDaoProvider;

  private final Provider<MessageDao> messageDaoProvider;

  public ChatLocalDataSource_Factory(Provider<RoomDao> roomDaoProvider,
      Provider<MessageDao> messageDaoProvider) {
    this.roomDaoProvider = roomDaoProvider;
    this.messageDaoProvider = messageDaoProvider;
  }

  @Override
  public ChatLocalDataSource get() {
    return newInstance(roomDaoProvider.get(), messageDaoProvider.get());
  }

  public static ChatLocalDataSource_Factory create(Provider<RoomDao> roomDaoProvider,
      Provider<MessageDao> messageDaoProvider) {
    return new ChatLocalDataSource_Factory(roomDaoProvider, messageDaoProvider);
  }

  public static ChatLocalDataSource newInstance(RoomDao roomDao, MessageDao messageDao) {
    return new ChatLocalDataSource(roomDao, messageDao);
  }
}
