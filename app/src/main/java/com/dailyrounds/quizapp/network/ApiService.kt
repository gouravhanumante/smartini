package com.dailyrounds.quizapp.network

import com.dailyrounds.quizapp.data.QuestionResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("questions/all")
    suspend fun getAllQuestions(page: Int, pageSize: Int): Response<QuestionResponse>
}

