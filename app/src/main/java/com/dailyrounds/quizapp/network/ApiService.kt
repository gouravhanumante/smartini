package com.dailyrounds.quizapp.network

import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.data.QuestionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET("questions/all")
    suspend fun getAllQuestions(page: Int, pageSize: Int): Response<QuestionResponse>

    @GET
    suspend fun getQuestions(@Url url: String): Response<List<Question>>
}

