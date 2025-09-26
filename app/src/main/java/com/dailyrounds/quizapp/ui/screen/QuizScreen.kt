package com.dailyrounds.quizapp.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel
import kotlinx.coroutines.delay


@Composable
fun QuizScreen(viewModel: QuestionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = viewModel.getCurrentQuestion()

    if (currentQuestion == null || viewModel.isQuizCompleted()) {
        return
    }

    var countdown by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.selectedAnswer) {
        if (uiState.selectedAnswer != null && uiState.answerShown) {
            countdown = 2
            repeat(2) {
                delay(1000)
                countdown--
            }
            viewModel.nextQuestion()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {

            val undoAlpha by animateFloatAsState(
                targetValue = if (uiState.reachedViaSkip && uiState.selectedAnswer == null) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "undoAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.undoSkip()
                    },
            ) {
                Row(Modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = undoAlpha)) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Undo skip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }

            val totalQuestions = viewModel.getTotalQuestions()
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question ${uiState.currentQuestionIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Score: ${uiState.score}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val targetProgress = if (totalQuestions > 0) {
                (uiState.currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat()
            } else {
                0f
            }

            val animatedProgress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                ),
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    currentQuestion.options.forEachIndexed { index, option ->

                        OptionItem(
                            text = option,
                            isSelected = uiState.selectedAnswer == index,
                            isCorrect = uiState.answerShown && index == currentQuestion.correctOptionIndex,
                            isInCorrect = uiState.answerShown && index != currentQuestion.correctOptionIndex && uiState.selectedAnswer == index,
                            onClick = {
                                if (!uiState.answerShown) {
                                    viewModel.selectAnswer(index)
                                }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SkipSection(viewModel)
        }
    }
}