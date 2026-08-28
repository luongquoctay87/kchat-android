package com.kchat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kchat.data.local.entity.MessageEntity
import com.kchat.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY updatedAt DESC")
    fun observeRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rooms: List<RoomEntity>)

    @Query("UPDATE rooms SET preview = '', unreadCount = 0")
    suspend fun resetPreviewsAfterMessageWipe()

    @Query(
        """
        UPDATE rooms SET preview = '', time = ''
        WHERE id NOT IN (SELECT DISTINCT roomId FROM messages)
        """,
    )
    suspend fun clearPreviewsForEmptyRooms()

    @Query("DELETE FROM rooms")
    suspend fun clearAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY createdAt ASC")
    fun observeMessages(roomId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY createdAt ASC")
    suspend fun getByRoomId(roomId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    suspend fun clearRoom(roomId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Purge cached messages past per-room disappearing TTL, or global retention for other rooms.
     * Mirrors backend disappearing cleanup (#20).
     */
    @Query(
        """
        DELETE FROM messages
        WHERE id IN (
            SELECT m.id FROM messages AS m
            LEFT JOIN rooms AS r ON r.id = m.roomId
            WHERE
                (r.disappearingAfterSeconds IS NOT NULL AND r.disappearingAfterSeconds > 0
                    AND m.createdAt < (:nowMillis - (r.disappearingAfterSeconds * 1000)))
                OR
                ((r.disappearingAfterSeconds IS NULL OR r.disappearingAfterSeconds <= 0 OR r.id IS NULL)
                    AND m.createdAt < :globalCutoff)
        )
        """,
    )
    suspend fun deleteExpiredLocalMessages(nowMillis: Long, globalCutoff: Long): Int

    @Query(
        """
        UPDATE messages SET isRead = 1
        WHERE roomId = :roomId AND isMine = 1 AND isRead = 0 AND createdAt <= :upToCreatedAt
        """,
    )
    suspend fun markMineReadUpTo(roomId: String, upToCreatedAt: Long): Int
}
