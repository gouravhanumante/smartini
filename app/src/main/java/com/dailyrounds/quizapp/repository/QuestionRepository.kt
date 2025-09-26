package com.dailyrounds.quizapp.repository

import android.content.Context
import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.data.QuestionResponse
import com.dailyrounds.quizapp.network.ApiClient
import com.dailyrounds.quizapp.network.ApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import com.dailyrounds.quizapp.network.Result

class QuestionRepository(private val context: Context) {


    private val apiService: ApiService = ApiClient.apiService
    private var cachedQuestions: List<Question>? = null

    suspend fun getQuestionsAndMetadata(page: Int, pageSize: Int): Result<QuestionResponse> {
        return try {
            val response = apiService.getAllQuestions(page, pageSize)

            if (response.isSuccessful && response.body() != null) {
                val questionResponse = response.body()!!
                Result.Success(questionResponse)
            } else {
                when (val jsonResult = loadAllQuestionsFromJson()) {
                    is Result.Success -> {
                        val questions = jsonResult.data
                        val questionResponse = createResponseFromJson(questions)
                        Result.Success(questionResponse)
                    }

                    is Result.Error -> Result.Error(jsonResult.message)
                    else -> Result.Error("Unexpected result type")
                }
            }

        } catch (e: Exception) {
            when (val jsonResult = loadAllQuestionsFromJson()) {
                is Result.Success -> {
                    val questions = jsonResult.data
                    val questionResponse = createResponseFromJson(questions)
                    Result.Success(questionResponse)
                }

                is Result.Error -> Result.Error(jsonResult.message)
                else -> Result.Error("Failed to load questions: ${e.message}")
            }
        }
    }

    fun createResponseFromJson(questions: List<Question>): QuestionResponse {
        return QuestionResponse(
            questions = questions,
            page = 0,
            size = questions.size,
            total = questions.size,
            hasMore = false,
            success = true
        )
    }


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
}
