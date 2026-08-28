package com.kchat.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.kchat.data.local.dao.MessageDao;
import com.kchat.data.local.dao.MessageDao_Impl;
import com.kchat.data.local.dao.RoomDao;
import com.kchat.data.local.dao.RoomDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class KChatDatabase_Impl extends KChatDatabase {
  private volatile RoomDao _roomDao;

  private volatile MessageDao _messageDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(6) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `rooms` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `preview` TEXT NOT NULL, `time` TEXT NOT NULL, `unreadCount` INTEGER NOT NULL, `isOnline` INTEGER NOT NULL, `isChannel` INTEGER NOT NULL, `isGroup` INTEGER NOT NULL, `memberCount` INTEGER NOT NULL, `disappearingAfterSeconds` INTEGER, `myRole` TEXT, `isMuted` INTEGER NOT NULL, `mutedUntilEpochMs` INTEGER, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `roomId` TEXT NOT NULL, `type` TEXT NOT NULL, `text` TEXT NOT NULL, `fileName` TEXT, `fileSize` TEXT, `imageLabel` TEXT, `mediaUrl` TEXT, `senderName` TEXT, `isMine` INTEGER NOT NULL, `time` TEXT NOT NULL, `replyAuthor` TEXT, `replyText` TEXT, `replyMessageId` TEXT, `replyMediaUrl` TEXT, `replyPreviewType` TEXT, `reactionsJson` TEXT, `isRead` INTEGER NOT NULL, `isEdited` INTEGER NOT NULL, `botTitle` TEXT, `botService` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8f299ad653c2d5d8d1ba6db0d9336fa8')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `rooms`");
        db.execSQL("DROP TABLE IF EXISTS `messages`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRooms = new HashMap<String, TableInfo.Column>(14);
        _columnsRooms.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("preview", new TableInfo.Column("preview", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("time", new TableInfo.Column("time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("isOnline", new TableInfo.Column("isOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("isChannel", new TableInfo.Column("isChannel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("isGroup", new TableInfo.Column("isGroup", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("memberCount", new TableInfo.Column("memberCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("disappearingAfterSeconds", new TableInfo.Column("disappearingAfterSeconds", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("myRole", new TableInfo.Column("myRole", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("isMuted", new TableInfo.Column("isMuted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("mutedUntilEpochMs", new TableInfo.Column("mutedUntilEpochMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRooms.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRooms = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRooms = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRooms = new TableInfo("rooms", _columnsRooms, _foreignKeysRooms, _indicesRooms);
        final TableInfo _existingRooms = TableInfo.read(db, "rooms");
        if (!_infoRooms.equals(_existingRooms)) {
          return new RoomOpenHelper.ValidationResult(false, "rooms(com.kchat.data.local.entity.RoomEntity).\n"
                  + " Expected:\n" + _infoRooms + "\n"
                  + " Found:\n" + _existingRooms);
        }
        final HashMap<String, TableInfo.Column> _columnsMessages = new HashMap<String, TableInfo.Column>(22);
        _columnsMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("roomId", new TableInfo.Column("roomId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("fileName", new TableInfo.Column("fileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("fileSize", new TableInfo.Column("fileSize", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("imageLabel", new TableInfo.Column("imageLabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("mediaUrl", new TableInfo.Column("mediaUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("senderName", new TableInfo.Column("senderName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isMine", new TableInfo.Column("isMine", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("time", new TableInfo.Column("time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("replyAuthor", new TableInfo.Column("replyAuthor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("replyText", new TableInfo.Column("replyText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("replyMessageId", new TableInfo.Column("replyMessageId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("replyMediaUrl", new TableInfo.Column("replyMediaUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("replyPreviewType", new TableInfo.Column("replyPreviewType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("reactionsJson", new TableInfo.Column("reactionsJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isRead", new TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isEdited", new TableInfo.Column("isEdited", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("botTitle", new TableInfo.Column("botTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("botService", new TableInfo.Column("botService", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMessages = new TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages);
        final TableInfo _existingMessages = TableInfo.read(db, "messages");
        if (!_infoMessages.equals(_existingMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "messages(com.kchat.data.local.entity.MessageEntity).\n"
                  + " Expected:\n" + _infoMessages + "\n"
                  + " Found:\n" + _existingMessages);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8f299ad653c2d5d8d1ba6db0d9336fa8", "4a20d4db26bf3c4fec5ea4eb280784ed");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "rooms","messages");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `rooms`");
      _db.execSQL("DELETE FROM `messages`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RoomDao.class, RoomDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MessageDao.class, MessageDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RoomDao roomDao() {
    if (_roomDao != null) {
      return _roomDao;
    } else {
      synchronized(this) {
        if(_roomDao == null) {
          _roomDao = new RoomDao_Impl(this);
        }
        return _roomDao;
      }
    }
  }

  @Override
  public MessageDao messageDao() {
    if (_messageDao != null) {
      return _messageDao;
    } else {
      synchronized(this) {
        if(_messageDao == null) {
          _messageDao = new MessageDao_Impl(this);
        }
        return _messageDao;
      }
    }
  }
}
