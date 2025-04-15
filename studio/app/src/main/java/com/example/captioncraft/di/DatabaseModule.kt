package com.example.captioncraft.di

import android.content.Context
import androidx.room.Room
import com.example.captioncraft.data.local.AppDatabase
import com.example.captioncraft.data.local.dao.CaptionDao
import com.example.captioncraft.data.local.dao.FollowDao
import com.example.captioncraft.data.local.dao.PostDao
import com.example.captioncraft.data.local.dao.UserDao
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "caprank.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    fun provideCaptionDao(database: AppDatabase): CaptionDao {
        return database.captionDao()
    }

    @Provides
    fun provideFollowDao(database: AppDatabase): FollowDao {
        return database.followDao()
    }
} 