package com.dailyrounds.quizapp.repository

import android.content.Context
import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.network.ApiClient
import com.dailyrounds.quizapp.network.ApiService
import com.dailyrounds.quizapp.network.Result

class QuestionRepository(private val context: Context) {

    private val apiService: ApiService = ApiClient.apiService

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
