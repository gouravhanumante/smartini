package com.dailyrounds.quizapp.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel

@Composable
fun MainPage(viewModel: QuestionViewModel, selectedTheme: AppTheme, onHome: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    Box(Modifier.fillMaxSize()) {
        when (result) {
            is Result.Error<*> -> {

            }

            Result.Loading -> {
            }

            is Result.Success<*> -> {
                if (viewModel.isQuizCompleted()) {
                    val score = viewModel.getFinalScore()
                    val totalQuestions = viewModel.getTotalQuestions()
                    val highestStreak = viewModel.getHighestStreak()
                    val skippedQuestions = viewModel.getSkippedQuestions()

                    ResultScreen(
                        score,
                        totalQuestions,
                        highestStreak,
                        skippedQuestions,
                        selectedTheme,
                        onRestartQuiz = {
                            viewModel.restartQuiz()
                        },
                        onHome = onHome
                    )
                } else {
                    QuizScreen(viewModel, selectedTheme)
                }
            }
        }
    }

}