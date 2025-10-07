package com.dailyrounds.quizapp.repository

import android.content.Context
import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.network.ApiClient
import com.dailyrounds.quizapp.network.ApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import com.dailyrounds.quizapp.network.Result

class QuestionRepository(private val context: Context) {


    private val apiService: ApiService = ApiClient.apiService
    private var cachedQuestions: List<Question>? = null

    private suspend fun loadAllQuestionsFromJson(): Result<List<Question>> {
        if (cachedQuestions != null) {
            return Result.Success(cachedQuestions!!)
        }

        try {
            val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Question>>() {}.type
            val questions: List<Question> = Gson().fromJson(jsonString, type)
            cachedQuestions = questions

            return Result.Success(questions)
        } catch (e: IOException) {
            return Result.Error("Failed to read local questions: ${e.message}")
        } catch (e: Exception) {
            return Result.Error("Failed to parse local questions: ${e.message}")
        }
    }

    fun clearCache() {
        cachedQuestions = null
    }

    suspend fun getQuestions(questionsUrl: String): Result<List<Question>?> {
        return try {
            val response = apiService.getQuestions(questionsUrl)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body())
            } else {
                throw Exception("Failed to load questions from module")
            }
        } catch (e: Exception) {
            Result.Error("Failed to load questions from module: ${e.message}")
        }
    }
}
