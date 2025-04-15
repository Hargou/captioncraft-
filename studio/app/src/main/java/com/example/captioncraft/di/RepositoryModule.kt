package com.example.captioncraft.di

import com.example.captioncraft.data.local.dao.CaptionDao
import com.example.captioncraft.data.local.dao.FollowDao
import com.example.captioncraft.data.local.dao.PostDao
import com.example.captioncraft.data.local.dao.UserDao
import com.example.captioncraft.data.remote.api.CaptionApi
import com.example.captioncraft.data.remote.api.FollowApi
import com.example.captioncraft.data.remote.api.PostApi
import com.example.captioncraft.data.remote.api.UserApi
import com.example.captioncraft.data.repository.CaptionRepository
import com.example.captioncraft.data.repository.FollowRepository
import com.example.captioncraft.data.repository.PostRepository
import com.example.captioncraft.data.repository.UserRepository
import com.example.captioncraft.util.UserSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserSessionManager(): UserSessionManager = UserSessionManager()
    
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        userApi: UserApi,
        sessionManager: UserSessionManager
    ): UserRepository = UserRepository(userDao, userApi, sessionManager)

    @Provides
    @Singleton
    fun providePostRepository(
        postDao: PostDao,
        followDao: FollowDao,
        postApi: PostApi,
        userRepository: UserRepository,
        sessionManager: UserSessionManager
    ): PostRepository = PostRepository(postDao, followDao, postApi, userRepository, sessionManager)

    @Provides
    @Singleton
    fun provideCaptionRepository(
        captionDao: CaptionDao,
        captionApi: CaptionApi,
        sessionManager: UserSessionManager
    ): CaptionRepository = CaptionRepository(captionDao, captionApi, sessionManager)

    @Provides
    @Singleton
    fun provideFollowRepository(
        followDao: FollowDao,
        followApi: FollowApi
    ): FollowRepository = FollowRepository(followDao, followApi)
} 