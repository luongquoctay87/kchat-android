package com.kchat.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kchat.data.local.entity.RoomEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RoomDao_Impl implements RoomDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RoomEntity> __insertionAdapterOfRoomEntity;

  private final SharedSQLiteStatement __preparedStmtOfResetPreviewsAfterMessageWipe;

  private final SharedSQLiteStatement __preparedStmtOfClearPreviewsForEmptyRooms;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public RoomDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRoomEntity = new EntityInsertionAdapter<RoomEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `rooms` (`id`,`title`,`preview`,`time`,`unreadCount`,`isOnline`,`isChannel`,`isGroup`,`memberCount`,`disappearingAfterSeconds`,`myRole`,`isMuted`,`mutedUntilEpochMs`,`avatarUrl`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RoomEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getPreview());
        statement.bindString(4, entity.getTime());
        statement.bindLong(5, entity.getUnreadCount());
        final int _tmp = entity.isOnline() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.isChannel() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.isGroup() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        statement.bindLong(9, entity.getMemberCount());
        if (entity.getDisappearingAfterSeconds() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getDisappearingAfterSeconds());
        }
        if (entity.getMyRole() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMyRole());
        }
        final int _tmp_3 = entity.isMuted() ? 1 : 0;
        statement.bindLong(12, _tmp_3);
        if (entity.getMutedUntilEpochMs() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getMutedUntilEpochMs());
        }
        if (entity.getAvatarUrl() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getAvatarUrl());
        }
        statement.bindLong(15, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfResetPreviewsAfterMessageWipe = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE rooms SET preview = '', unreadCount = 0";
        return _query;
      }
    };
    this.__preparedStmtOfClearPreviewsForEmptyRooms = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE rooms SET preview = '', time = ''\n"
                + "        WHERE id NOT IN (SELECT DISTINCT roomId FROM messages)\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rooms";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<RoomEntity> rooms,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRoomEntity.insert(rooms);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object resetPreviewsAfterMessageWipe(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfResetPreviewsAfterMessageWipe.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfResetPreviewsAfterMessageWipe.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPreviewsForEmptyRooms(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPreviewsForEmptyRooms.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearPreviewsForEmptyRooms.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RoomEntity>> observeRooms() {
    final String _sql = "SELECT * FROM rooms ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"rooms"}, new Callable<List<RoomEntity>>() {
      @Override
      @NonNull
      public List<RoomEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfPreview = CursorUtil.getColumnIndexOrThrow(_cursor, "preview");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "isOnline");
          final int _cursorIndexOfIsChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "isChannel");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfDisappearingAfterSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingAfterSeconds");
          final int _cursorIndexOfMyRole = CursorUtil.getColumnIndexOrThrow(_cursor, "myRole");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfMutedUntilEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "mutedUntilEpochMs");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarUrl");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RoomEntity> _result = new ArrayList<RoomEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoomEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpPreview;
            _tmpPreview = _cursor.getString(_cursorIndexOfPreview);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final boolean _tmpIsChannel;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsChannel);
            _tmpIsChannel = _tmp_1 != 0;
            final boolean _tmpIsGroup;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp_2 != 0;
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final Integer _tmpDisappearingAfterSeconds;
            if (_cursor.isNull(_cursorIndexOfDisappearingAfterSeconds)) {
              _tmpDisappearingAfterSeconds = null;
            } else {
              _tmpDisappearingAfterSeconds = _cursor.getInt(_cursorIndexOfDisappearingAfterSeconds);
            }
            final String _tmpMyRole;
            if (_cursor.isNull(_cursorIndexOfMyRole)) {
              _tmpMyRole = null;
            } else {
              _tmpMyRole = _cursor.getString(_cursorIndexOfMyRole);
            }
            final boolean _tmpIsMuted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_3 != 0;
            final Long _tmpMutedUntilEpochMs;
            if (_cursor.isNull(_cursorIndexOfMutedUntilEpochMs)) {
              _tmpMutedUntilEpochMs = null;
            } else {
              _tmpMutedUntilEpochMs = _cursor.getLong(_cursorIndexOfMutedUntilEpochMs);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RoomEntity(_tmpId,_tmpTitle,_tmpPreview,_tmpTime,_tmpUnreadCount,_tmpIsOnline,_tmpIsChannel,_tmpIsGroup,_tmpMemberCount,_tmpDisappearingAfterSeconds,_tmpMyRole,_tmpIsMuted,_tmpMutedUntilEpochMs,_tmpAvatarUrl,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final String id, final Continuation<? super RoomEntity> $completion) {
    final String _sql = "SELECT * FROM rooms WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RoomEntity>() {
      @Override
      @Nullable
      public RoomEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfPreview = CursorUtil.getColumnIndexOrThrow(_cursor, "preview");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "isOnline");
          final int _cursorIndexOfIsChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "isChannel");
          final int _cursorIndexOfIsGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "isGroup");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfDisappearingAfterSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "disappearingAfterSeconds");
          final int _cursorIndexOfMyRole = CursorUtil.getColumnIndexOrThrow(_cursor, "myRole");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfMutedUntilEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "mutedUntilEpochMs");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarUrl");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RoomEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpPreview;
            _tmpPreview = _cursor.getString(_cursorIndexOfPreview);
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final boolean _tmpIsChannel;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsChannel);
            _tmpIsChannel = _tmp_1 != 0;
            final boolean _tmpIsGroup;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsGroup);
            _tmpIsGroup = _tmp_2 != 0;
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final Integer _tmpDisappearingAfterSeconds;
            if (_cursor.isNull(_cursorIndexOfDisappearingAfterSeconds)) {
              _tmpDisappearingAfterSeconds = null;
            } else {
              _tmpDisappearingAfterSeconds = _cursor.getInt(_cursorIndexOfDisappearingAfterSeconds);
            }
            final String _tmpMyRole;
            if (_cursor.isNull(_cursorIndexOfMyRole)) {
              _tmpMyRole = null;
            } else {
              _tmpMyRole = _cursor.getString(_cursorIndexOfMyRole);
            }
            final boolean _tmpIsMuted;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_3 != 0;
            final Long _tmpMutedUntilEpochMs;
            if (_cursor.isNull(_cursorIndexOfMutedUntilEpochMs)) {
              _tmpMutedUntilEpochMs = null;
            } else {
              _tmpMutedUntilEpochMs = _cursor.getLong(_cursorIndexOfMutedUntilEpochMs);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RoomEntity(_tmpId,_tmpTitle,_tmpPreview,_tmpTime,_tmpUnreadCount,_tmpIsOnline,_tmpIsChannel,_tmpIsGroup,_tmpMemberCount,_tmpDisappearingAfterSeconds,_tmpMyRole,_tmpIsMuted,_tmpMutedUntilEpochMs,_tmpAvatarUrl,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
