package com.kchat.di;

import com.kchat.data.local.KChatDatabase;
import com.kchat.data.local.dao.RoomDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideRoomDaoFactory implements Factory<RoomDao> {
  private final Provider<KChatDatabase> dbProvider;

  public DatabaseModule_ProvideRoomDaoFactory(Provider<KChatDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public RoomDao get() {
    return provideRoomDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideRoomDaoFactory create(Provider<KChatDatabase> dbProvider) {
    return new DatabaseModule_ProvideRoomDaoFactory(dbProvider);
  }

  public static RoomDao provideRoomDao(KChatDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRoomDao(db));
  }
}
