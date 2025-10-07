package com.dailyrounds.quizapp.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.ui.components.ErrorScreen
import com.dailyrounds.quizapp.ui.components.LoadingIndicator
import com.dailyrounds.quizapp.ui.screen.QuizScreen
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel

@Composable
fun MainPage(
    viewModel: QuestionViewModel, 
    selectedTheme: AppTheme, 
    onNavigateToResult: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    LaunchedEffect(uiState.currentQuestionIndex, uiState.result) {
        if (viewModel.isQuizCompleted() && uiState.result is Result.Success) {
            viewModel.completeQuiz()
            onNavigateToResult()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (result) {
            is Result.Error<*> -> {
                ErrorScreen(
                    title = "Unable to Load Questions",
                    message = result.message,
                    onRetry = {
                        viewModel.retry()
                    },
                    onBack = onBackClick
                )
            }

            is Result.Loading -> {
                LoadingIndicator(
                    message = "Loading questions..."
                )
            }

            is Result.Success<*> -> {
                QuizScreen(
                    viewModel = viewModel,
                    selectedTheme = selectedTheme,
                    onBackClick = onBackClick
                )
            }
        }
    }
}