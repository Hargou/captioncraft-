package com.example.captioncraft.data.local.dao

import androidx.room.*
import com.example.captioncraft.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostByIdFlow(id: Int): Flow<PostEntity?>
    
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Int): PostEntity?

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: Int): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("SELECT * FROM posts WHERE userId IN (:userIds) ORDER BY createdAt DESC")
    fun getPostsByUsers(userIds: List<Int>): Flow<List<PostEntity>>

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()
}
