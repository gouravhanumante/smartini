package com.dailyrounds.quizapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleDao {

    @Query("SELECT * FROM modules_table WHERE moduleId = :moduleId")
    suspend fun getModuleData(moduleId: String): ModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveModuleData(moduleEntity: ModuleEntity)

    @Query("DELETE FROM modules_table WHERE moduleId = :moduleId")
    suspend fun clearModuleData(moduleId: String)

    @Query("SELECT * FROM modules_table")
    suspend fun getAllModules(): List<ModuleEntity>

}