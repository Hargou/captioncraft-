package com.example.captioncraft.data.local.dao

import androidx.room.*
import com.example.captioncraft.data.local.entity.FollowEntity
import com.example.captioncraft.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Delete
    suspend fun deleteFollow(follow: FollowEntity)

    @Query("SELECT followingId FROM Follow WHERE followerId = :userId")
    fun getFollowingIds(userId: Int): Flow<List<Int>>

    @Query("SELECT followerId FROM Follow WHERE followingId = :userId")
    fun getFollowerIds(userId: Int): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM Follow WHERE followerId = :followerId AND followingId = :followingId)")
    fun isFollowing(followerId: Int, followingId: Int): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM Follow WHERE followerId = :userId")
    fun getFollowingCount(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM Follow WHERE followingId = :userId")
    fun getFollowerCount(userId: Int): Flow<Int>
}
