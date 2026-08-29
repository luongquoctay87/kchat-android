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
import com.kchat.data.local.entity.MessageEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearRoom;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredLocalMessages;

  private final SharedSQLiteStatement __preparedStmtOfMarkMineReadUpTo;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`roomId`,`type`,`text`,`fileName`,`fileSize`,`imageLabel`,`mediaUrl`,`senderName`,`isMine`,`time`,`replyAuthor`,`replyText`,`replyMessageId`,`replyMediaUrl`,`replyPreviewType`,`reactionsJson`,`isRead`,`isEdited`,`botTitle`,`botService`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getRoomId());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getText());
        if (entity.getFileName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getFileName());
        }
        if (entity.getFileSize() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getFileSize());
        }
        if (entity.getImageLabel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getImageLabel());
        }
        if (entity.getMediaUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMediaUrl());
        }
        if (entity.getSenderName() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSenderName());
        }
        final int _tmp = entity.isMine() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindString(11, entity.getTime());
        if (entity.getReplyAuthor() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getReplyAuthor());
        }
        if (entity.getReplyText() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getReplyText());
        }
        if (entity.getReplyMessageId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getReplyMessageId());
        }
        if (entity.getReplyMediaUrl() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getReplyMediaUrl());
        }
        if (entity.getReplyPreviewType() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getReplyPreviewType());
        }
        if (entity.getReactionsJson() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getReactionsJson());
        }
        final int _tmp_1 = entity.isRead() ? 1 : 0;
        statement.bindLong(18, _tmp_1);
        final int _tmp_2 = entity.isEdited() ? 1 : 0;
        statement.bindLong(19, _tmp_2);
        if (entity.getBotTitle() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getBotTitle());
        }
        if (entity.getBotService() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getBotService());
        }
        statement.bindLong(22, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfClearRoom = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE roomId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredLocalMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM messages\n"
                + "        WHERE id IN (\n"
                + "            SELECT m.id FROM messages AS m\n"
                + "            LEFT JOIN rooms AS r ON r.id = m.roomId\n"
                + "            WHERE\n"
                + "                (r.disappearingAfterSeconds IS NOT NULL AND r.disappearingAfterSeconds > 0\n"
                + "                    AND m.createdAt < (? - (r.disappearingAfterSeconds * 1000)))\n"
                + "                OR\n"
                + "                ((r.disappearingAfterSeconds IS NULL OR r.disappearingAfterSeconds <= 0 OR r.id IS NULL)\n"
                + "                    AND m.createdAt < ?)\n"
                + "        )\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfMarkMineReadUpTo = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE messages SET isRead = 1\n"
                + "        WHERE roomId = ? AND isMine = 1 AND isRead = 0 AND createdAt <= ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<MessageEntity> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearRoom(final String roomId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearRoom.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, roomId);
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
          __preparedStmtOfClearRoom.release(_stmt);
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
  public Object deleteById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredLocalMessages(final long nowMillis, final long globalCutoff,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredLocalMessages.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, nowMillis);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, globalCutoff);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredLocalMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markMineReadUpTo(final String roomId, final long upToCreatedAt,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkMineReadUpTo.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, roomId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, upToCreatedAt);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkMineReadUpTo.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MessageEntity>> observeMessages(final String roomId) {
    final String _sql = "SELECT * FROM messages WHERE roomId = ? ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, roomId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoomId = CursorUtil.getColumnIndexOrThrow(_cursor, "roomId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfImageLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "imageLabel");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfIsMine = CursorUtil.getColumnIndexOrThrow(_cursor, "isMine");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfReplyAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "replyAuthor");
          final int _cursorIndexOfReplyText = CursorUtil.getColumnIndexOrThrow(_cursor, "replyText");
          final int _cursorIndexOfReplyMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "replyMessageId");
          final int _cursorIndexOfReplyMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "replyMediaUrl");
          final int _cursorIndexOfReplyPreviewType = CursorUtil.getColumnIndexOrThrow(_cursor, "replyPreviewType");
          final int _cursorIndexOfReactionsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionsJson");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfBotTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "botTitle");
          final int _cursorIndexOfBotService = CursorUtil.getColumnIndexOrThrow(_cursor, "botService");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRoomId;
            _tmpRoomId = _cursor.getString(_cursorIndexOfRoomId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpFileSize;
            if (_cursor.isNull(_cursorIndexOfFileSize)) {
              _tmpFileSize = null;
            } else {
              _tmpFileSize = _cursor.getString(_cursorIndexOfFileSize);
            }
            final String _tmpImageLabel;
            if (_cursor.isNull(_cursorIndexOfImageLabel)) {
              _tmpImageLabel = null;
            } else {
              _tmpImageLabel = _cursor.getString(_cursorIndexOfImageLabel);
            }
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpSenderName;
            if (_cursor.isNull(_cursorIndexOfSenderName)) {
              _tmpSenderName = null;
            } else {
              _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            }
            final boolean _tmpIsMine;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMine);
            _tmpIsMine = _tmp != 0;
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpReplyAuthor;
            if (_cursor.isNull(_cursorIndexOfReplyAuthor)) {
              _tmpReplyAuthor = null;
            } else {
              _tmpReplyAuthor = _cursor.getString(_cursorIndexOfReplyAuthor);
            }
            final String _tmpReplyText;
            if (_cursor.isNull(_cursorIndexOfReplyText)) {
              _tmpReplyText = null;
            } else {
              _tmpReplyText = _cursor.getString(_cursorIndexOfReplyText);
            }
            final String _tmpReplyMessageId;
            if (_cursor.isNull(_cursorIndexOfReplyMessageId)) {
              _tmpReplyMessageId = null;
            } else {
              _tmpReplyMessageId = _cursor.getString(_cursorIndexOfReplyMessageId);
            }
            final String _tmpReplyMediaUrl;
            if (_cursor.isNull(_cursorIndexOfReplyMediaUrl)) {
              _tmpReplyMediaUrl = null;
            } else {
              _tmpReplyMediaUrl = _cursor.getString(_cursorIndexOfReplyMediaUrl);
            }
            final String _tmpReplyPreviewType;
            if (_cursor.isNull(_cursorIndexOfReplyPreviewType)) {
              _tmpReplyPreviewType = null;
            } else {
              _tmpReplyPreviewType = _cursor.getString(_cursorIndexOfReplyPreviewType);
            }
            final String _tmpReactionsJson;
            if (_cursor.isNull(_cursorIndexOfReactionsJson)) {
              _tmpReactionsJson = null;
            } else {
              _tmpReactionsJson = _cursor.getString(_cursorIndexOfReactionsJson);
            }
            final boolean _tmpIsRead;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final String _tmpBotTitle;
            if (_cursor.isNull(_cursorIndexOfBotTitle)) {
              _tmpBotTitle = null;
            } else {
              _tmpBotTitle = _cursor.getString(_cursorIndexOfBotTitle);
            }
            final String _tmpBotService;
            if (_cursor.isNull(_cursorIndexOfBotService)) {
              _tmpBotService = null;
            } else {
              _tmpBotService = _cursor.getString(_cursorIndexOfBotService);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new MessageEntity(_tmpId,_tmpRoomId,_tmpType,_tmpText,_tmpFileName,_tmpFileSize,_tmpImageLabel,_tmpMediaUrl,_tmpSenderName,_tmpIsMine,_tmpTime,_tmpReplyAuthor,_tmpReplyText,_tmpReplyMessageId,_tmpReplyMediaUrl,_tmpReplyPreviewType,_tmpReactionsJson,_tmpIsRead,_tmpIsEdited,_tmpBotTitle,_tmpBotService,_tmpCreatedAt);
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
  public Object getById(final String id, final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoomId = CursorUtil.getColumnIndexOrThrow(_cursor, "roomId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfImageLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "imageLabel");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfIsMine = CursorUtil.getColumnIndexOrThrow(_cursor, "isMine");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfReplyAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "replyAuthor");
          final int _cursorIndexOfReplyText = CursorUtil.getColumnIndexOrThrow(_cursor, "replyText");
          final int _cursorIndexOfReplyMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "replyMessageId");
          final int _cursorIndexOfReplyMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "replyMediaUrl");
          final int _cursorIndexOfReplyPreviewType = CursorUtil.getColumnIndexOrThrow(_cursor, "replyPreviewType");
          final int _cursorIndexOfReactionsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionsJson");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfBotTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "botTitle");
          final int _cursorIndexOfBotService = CursorUtil.getColumnIndexOrThrow(_cursor, "botService");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRoomId;
            _tmpRoomId = _cursor.getString(_cursorIndexOfRoomId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpFileSize;
            if (_cursor.isNull(_cursorIndexOfFileSize)) {
              _tmpFileSize = null;
            } else {
              _tmpFileSize = _cursor.getString(_cursorIndexOfFileSize);
            }
            final String _tmpImageLabel;
            if (_cursor.isNull(_cursorIndexOfImageLabel)) {
              _tmpImageLabel = null;
            } else {
              _tmpImageLabel = _cursor.getString(_cursorIndexOfImageLabel);
            }
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpSenderName;
            if (_cursor.isNull(_cursorIndexOfSenderName)) {
              _tmpSenderName = null;
            } else {
              _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            }
            final boolean _tmpIsMine;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMine);
            _tmpIsMine = _tmp != 0;
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpReplyAuthor;
            if (_cursor.isNull(_cursorIndexOfReplyAuthor)) {
              _tmpReplyAuthor = null;
            } else {
              _tmpReplyAuthor = _cursor.getString(_cursorIndexOfReplyAuthor);
            }
            final String _tmpReplyText;
            if (_cursor.isNull(_cursorIndexOfReplyText)) {
              _tmpReplyText = null;
            } else {
              _tmpReplyText = _cursor.getString(_cursorIndexOfReplyText);
            }
            final String _tmpReplyMessageId;
            if (_cursor.isNull(_cursorIndexOfReplyMessageId)) {
              _tmpReplyMessageId = null;
            } else {
              _tmpReplyMessageId = _cursor.getString(_cursorIndexOfReplyMessageId);
            }
            final String _tmpReplyMediaUrl;
            if (_cursor.isNull(_cursorIndexOfReplyMediaUrl)) {
              _tmpReplyMediaUrl = null;
            } else {
              _tmpReplyMediaUrl = _cursor.getString(_cursorIndexOfReplyMediaUrl);
            }
            final String _tmpReplyPreviewType;
            if (_cursor.isNull(_cursorIndexOfReplyPreviewType)) {
              _tmpReplyPreviewType = null;
            } else {
              _tmpReplyPreviewType = _cursor.getString(_cursorIndexOfReplyPreviewType);
            }
            final String _tmpReactionsJson;
            if (_cursor.isNull(_cursorIndexOfReactionsJson)) {
              _tmpReactionsJson = null;
            } else {
              _tmpReactionsJson = _cursor.getString(_cursorIndexOfReactionsJson);
            }
            final boolean _tmpIsRead;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final String _tmpBotTitle;
            if (_cursor.isNull(_cursorIndexOfBotTitle)) {
              _tmpBotTitle = null;
            } else {
              _tmpBotTitle = _cursor.getString(_cursorIndexOfBotTitle);
            }
            final String _tmpBotService;
            if (_cursor.isNull(_cursorIndexOfBotService)) {
              _tmpBotService = null;
            } else {
              _tmpBotService = _cursor.getString(_cursorIndexOfBotService);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new MessageEntity(_tmpId,_tmpRoomId,_tmpType,_tmpText,_tmpFileName,_tmpFileSize,_tmpImageLabel,_tmpMediaUrl,_tmpSenderName,_tmpIsMine,_tmpTime,_tmpReplyAuthor,_tmpReplyText,_tmpReplyMessageId,_tmpReplyMediaUrl,_tmpReplyPreviewType,_tmpReactionsJson,_tmpIsRead,_tmpIsEdited,_tmpBotTitle,_tmpBotService,_tmpCreatedAt);
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

  @Override
  public Object getByRoomId(final String roomId,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE roomId = ? ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, roomId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoomId = CursorUtil.getColumnIndexOrThrow(_cursor, "roomId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfImageLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "imageLabel");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfIsMine = CursorUtil.getColumnIndexOrThrow(_cursor, "isMine");
          final int _cursorIndexOfTime = CursorUtil.getColumnIndexOrThrow(_cursor, "time");
          final int _cursorIndexOfReplyAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "replyAuthor");
          final int _cursorIndexOfReplyText = CursorUtil.getColumnIndexOrThrow(_cursor, "replyText");
          final int _cursorIndexOfReplyMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "replyMessageId");
          final int _cursorIndexOfReplyMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "replyMediaUrl");
          final int _cursorIndexOfReplyPreviewType = CursorUtil.getColumnIndexOrThrow(_cursor, "replyPreviewType");
          final int _cursorIndexOfReactionsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionsJson");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfBotTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "botTitle");
          final int _cursorIndexOfBotService = CursorUtil.getColumnIndexOrThrow(_cursor, "botService");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRoomId;
            _tmpRoomId = _cursor.getString(_cursorIndexOfRoomId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final String _tmpFileSize;
            if (_cursor.isNull(_cursorIndexOfFileSize)) {
              _tmpFileSize = null;
            } else {
              _tmpFileSize = _cursor.getString(_cursorIndexOfFileSize);
            }
            final String _tmpImageLabel;
            if (_cursor.isNull(_cursorIndexOfImageLabel)) {
              _tmpImageLabel = null;
            } else {
              _tmpImageLabel = _cursor.getString(_cursorIndexOfImageLabel);
            }
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpSenderName;
            if (_cursor.isNull(_cursorIndexOfSenderName)) {
              _tmpSenderName = null;
            } else {
              _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            }
            final boolean _tmpIsMine;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMine);
            _tmpIsMine = _tmp != 0;
            final String _tmpTime;
            _tmpTime = _cursor.getString(_cursorIndexOfTime);
            final String _tmpReplyAuthor;
            if (_cursor.isNull(_cursorIndexOfReplyAuthor)) {
              _tmpReplyAuthor = null;
            } else {
              _tmpReplyAuthor = _cursor.getString(_cursorIndexOfReplyAuthor);
            }
            final String _tmpReplyText;
            if (_cursor.isNull(_cursorIndexOfReplyText)) {
              _tmpReplyText = null;
            } else {
              _tmpReplyText = _cursor.getString(_cursorIndexOfReplyText);
            }
            final String _tmpReplyMessageId;
            if (_cursor.isNull(_cursorIndexOfReplyMessageId)) {
              _tmpReplyMessageId = null;
            } else {
              _tmpReplyMessageId = _cursor.getString(_cursorIndexOfReplyMessageId);
            }
            final String _tmpReplyMediaUrl;
            if (_cursor.isNull(_cursorIndexOfReplyMediaUrl)) {
              _tmpReplyMediaUrl = null;
            } else {
              _tmpReplyMediaUrl = _cursor.getString(_cursorIndexOfReplyMediaUrl);
            }
            final String _tmpReplyPreviewType;
            if (_cursor.isNull(_cursorIndexOfReplyPreviewType)) {
              _tmpReplyPreviewType = null;
            } else {
              _tmpReplyPreviewType = _cursor.getString(_cursorIndexOfReplyPreviewType);
            }
            final String _tmpReactionsJson;
            if (_cursor.isNull(_cursorIndexOfReactionsJson)) {
              _tmpReactionsJson = null;
            } else {
              _tmpReactionsJson = _cursor.getString(_cursorIndexOfReactionsJson);
            }
            final boolean _tmpIsRead;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final String _tmpBotTitle;
            if (_cursor.isNull(_cursorIndexOfBotTitle)) {
              _tmpBotTitle = null;
            } else {
              _tmpBotTitle = _cursor.getString(_cursorIndexOfBotTitle);
            }
            final String _tmpBotService;
            if (_cursor.isNull(_cursorIndexOfBotService)) {
              _tmpBotService = null;
            } else {
              _tmpBotService = _cursor.getString(_cursorIndexOfBotService);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new MessageEntity(_tmpId,_tmpRoomId,_tmpType,_tmpText,_tmpFileName,_tmpFileSize,_tmpImageLabel,_tmpMediaUrl,_tmpSenderName,_tmpIsMine,_tmpTime,_tmpReplyAuthor,_tmpReplyText,_tmpReplyMessageId,_tmpReplyMediaUrl,_tmpReplyPreviewType,_tmpReactionsJson,_tmpIsRead,_tmpIsEdited,_tmpBotTitle,_tmpBotService,_tmpCreatedAt);
            _result.add(_item);
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
