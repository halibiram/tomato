package com.tomatomediacenter.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tomatomediacenter.data.db.entity.PlaceholderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceholderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(placeholder: PlaceholderEntity): Long

    @Query("SELECT * FROM placeholders WHERE id = :id")
    fun getById(id: Int): Flow<PlaceholderEntity?>

    @Query("SELECT * FROM placeholders ORDER BY name ASC")
    fun getAll(): Flow<List<PlaceholderEntity>>

    @Update
    suspend fun update(placeholder: PlaceholderEntity)

    @Delete
    suspend fun delete(placeholder: PlaceholderEntity)

    @Query("DELETE FROM placeholders")
    suspend fun deleteAll()
}
