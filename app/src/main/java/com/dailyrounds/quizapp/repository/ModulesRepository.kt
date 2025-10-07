package com.dailyrounds.quizapp.repository

import android.content.Context
import com.dailyrounds.quizapp.data.Module
import com.dailyrounds.quizapp.db.ModuleDao
import com.dailyrounds.quizapp.db.ModuleEntity
import com.dailyrounds.quizapp.network.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModulesRepository(
    private val context: Context,
    private val moduleDao: ModuleDao
) {

    var cachedModules = emptyList<Module>()


    fun fetchModulesFromJson(): Result<List<Module>> {

        if (cachedModules.isNotEmpty()) {
            return Result.Success(cachedModules)
        }

        return try {
            val jsonString = context.assets.open("modules.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Module>>() {}.type
            val modules: List<Module> = Gson().fromJson(jsonString, type)
            cachedModules = modules
            Result.Success(modules)
        } catch (e: Exception) {
            Result.Error("Failed to fetch from json")
        }

    }


    suspend fun fetchCompletionStatus(moduleId: String): ModuleEntity? {
        return try {
            val moduleData = withContext(Dispatchers.IO) {
                moduleDao.getModuleData(moduleId)
            }
            moduleData
        } catch (e: Exception) {
            null
        }
    }
}