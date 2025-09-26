package com.dailyrounds.quizapp.data

data class QuestionResponse(
    val questions: List<Question>,
    val page: Int,
    val size: Int,
    val total: Int,
    val hasMore: Boolean,
    val success: Boolean = true,
    val message: String? = null
)

data class Question(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int
)
