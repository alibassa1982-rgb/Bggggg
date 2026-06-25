package com.example.data

import kotlinx.coroutines.flow.Flow

class MineModRepository(private val mineModDao: MineModDao) {
    val allMods: Flow<List<MineMod>> = mineModDao.getAllMods()

    suspend fun insert(mod: MineMod) {
        mineModDao.insertMod(mod)
    }

    suspend fun deleteById(id: Int) {
        mineModDao.deleteModById(id)
    }

    suspend fun getModById(id: Int): MineMod? {
        return mineModDao.getModById(id)
    }
}
