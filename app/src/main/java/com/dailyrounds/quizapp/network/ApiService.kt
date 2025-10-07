package com.dailyrounds.quizapp.network

import com.dailyrounds.quizapp.data.Question
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getQuestions(@Url url: String): Response<List<Question>>
}

