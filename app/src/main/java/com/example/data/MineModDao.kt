package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MineModDao {
    @Query("SELECT * FROM mine_mods ORDER BY createdAt DESC")
    fun getAllMods(): Flow<List<MineMod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMod(mod: MineMod)

    @Query("DELETE FROM mine_mods WHERE id = :id")
    suspend fun deleteModById(id: Int)

    @Query("SELECT * FROM mine_mods WHERE id = :id LIMIT 1")
    suspend fun getModById(id: Int): MineMod?
}
