package com.example.captioncraft.data.local.dao
import androidx.room.*
import com.example.captioncraft.data.local.entity.CaptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaption(caption: CaptionEntity)

    @Query("SELECT * FROM Caption WHERE id = :id")
    fun getCaptionById(id: Int): Flow<CaptionEntity?>

    @Query("SELECT * FROM Caption WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCaptionsByPost(postId: Int): Flow<List<CaptionEntity>>

    @Query("SELECT * FROM Caption WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCaptionsByUser(userId: Int): Flow<List<CaptionEntity>>

    @Update
    suspend fun updateCaption(caption: CaptionEntity)

    @Delete
    suspend fun deleteCaption(caption: CaptionEntity)
}
