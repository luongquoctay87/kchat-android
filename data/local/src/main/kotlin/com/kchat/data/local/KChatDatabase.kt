package com.kchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kchat.data.local.dao.MessageDao
import com.kchat.data.local.dao.RoomDao
import com.kchat.data.local.entity.MessageEntity
import com.kchat.data.local.entity.RoomEntity

@Database(
    entities = [RoomEntity::class, MessageEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class KChatDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao

    abstract fun messageDao(): MessageDao
}
