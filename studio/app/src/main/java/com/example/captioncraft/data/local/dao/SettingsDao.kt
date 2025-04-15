package com.example.captioncraft.data.local.dao

import androidx.room.Dao

import androidx.room.*
import com.example.captioncraft.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SettingsEntity)

    @Update
    suspend fun update(settings: SettingsEntity)

    @Delete
    suspend fun delete(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = :userId LIMIT 1")
    fun getSettingsByUserId(userId: Int): Flow<SettingsEntity?>

    @Query("DELETE FROM settings WHERE id = :userId")
    suspend fun deleteByUserId(userId: Int)
}
