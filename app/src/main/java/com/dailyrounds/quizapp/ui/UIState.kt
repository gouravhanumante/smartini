package com.dailyrounds.quizapp.ui

import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.viewmodel.AnimationDirection

data class QuestionUiState(
    val result: com.dailyrounds.quizapp.network.Result<List<Question>> = com.dailyrounds.quizapp.network.Result.Loading,
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val answerShown: Boolean = false,
    val score: Int = 0,
    val skipButtonClicked: Boolean = false,
    val reachedViaSkip: Boolean = false,
    val animationDirection: AnimationDirection = AnimationDirection.NONE,
    val currentStreak: Int = 0,
    val previousStreak: Int = 0,
    val highestStreak: Int = 0,
    val skippedQuestions: Int = 0,
    val timeRemaining: Int = 15,
    val timerActive: Boolean = false
)