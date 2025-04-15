package com.example.captioncraft.data.local.dao

import androidx.room.*
import com.example.captioncraft.data.local.entity.UserEntity
import com.example.captioncraft.domain.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    fun getUsersByIds(userIds: List<Int>): Flow<List<UserEntity>>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): User?

}
