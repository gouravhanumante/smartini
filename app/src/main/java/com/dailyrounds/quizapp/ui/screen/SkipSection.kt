package com.dailyrounds.quizapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dailyrounds.quizapp.ui.theme.ButtonStore
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel

@Composable
fun SkipSection(viewModel: QuestionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    if (!uiState.skipButtonClicked && uiState.selectedAnswer == null) {

        val isLastQuestion = viewModel.isLastQuestion()

        if (isLastQuestion) {
            ButtonStore.PrimaryButton(
                text = "Skip and see Result",
                onClick = { viewModel.skipQuestion() }
            )
        } else {
            ButtonStore.SecondaryButton(
                text = "Skip",
                onClick = { viewModel.skipQuestion() }
            )
        }
    }
}