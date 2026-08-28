package com.kchat.di

import android.content.Context
import androidx.room.Room
import com.kchat.data.local.KChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KChatDatabase =
        Room.databaseBuilder(context, KChatDatabase::class.java, "kchat.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRoomDao(db: KChatDatabase) = db.roomDao()

    @Provides
    fun provideMessageDao(db: KChatDatabase) = db.messageDao()
}
