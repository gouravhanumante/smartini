package com.dailyrounds.quizapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ModuleDao {

    @Query("SELECT * FROM modules_table WHERE moduleId = :moduleId")
    suspend fun getModuleData(moduleId: String): ModuleEntity?

    @Update
    suspend fun updateModuleData(progress: ModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveModuleData(moduleEntity: ModuleEntity)

}