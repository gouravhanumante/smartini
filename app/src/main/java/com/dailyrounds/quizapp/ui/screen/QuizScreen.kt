package com.dailyrounds.quizapp.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FastForward
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.ui.components.StreakSection
import com.dailyrounds.quizapp.ui.components.Timer
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel
import kotlinx.coroutines.delay


@Composable
fun QuizScreen(viewModel: QuestionViewModel, selectedTheme: AppTheme) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = viewModel.getCurrentQuestion()

    if (currentQuestion == null || viewModel.isQuizCompleted()) {
        return
    }

    var countdown by remember { mutableIntStateOf(0) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f

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

    LaunchedEffect(uiState.animationDirection) {
        if (uiState.animationDirection != com.dailyrounds.quizapp.viewmodel.AnimationDirection.NONE) {
            delay(300)
            viewModel.resetAnimationDirection()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 16.dp)
        ) {

            val undoAlpha by animateFloatAsState(
                targetValue = if (uiState.reachedViaSkip && uiState.selectedAnswer == null) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "undo_alpha"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable {
                            viewModel.undoSkip()
                        },
                ) {
                    Row(
                        Modifier
                            .graphicsLayer(alpha = undoAlpha)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Undo skip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(enabled = !uiState.answerShown) { viewModel.skipQuestion() }
                            .padding(end = 8.dp)
                    )
                    Timer(uiState.timeRemaining)
                }
            }

            val totalQuestions = viewModel.getTotalQuestions()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Question ${uiState.currentQuestionIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
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
            } else 0f

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

            val slideOffset by animateFloatAsState(
                targetValue = when (uiState.animationDirection) {
                    com.dailyrounds.quizapp.viewmodel.AnimationDirection.FORWARD -> -100f
                    com.dailyrounds.quizapp.viewmodel.AnimationDirection.BACKWARD -> 100f
                    else -> 0f
                },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "slide_offset"
            )

            val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
            val padding = if (isLandscape) 24.dp else 12.dp

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .graphicsLayer(
                        translationX = slideOffset,
                        alpha = if (slideOffset != 0f) 0.7f else 1f
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (kotlin.math.abs(dragDistance) > swipeThreshold) {
                                    if (dragDistance > 0) {
                                        if (uiState.reachedViaSkip && uiState.currentQuestionIndex > 0) {
                                            viewModel.undoSkip()
                                        }
                                    } else {
                                        if (!uiState.answerShown) {
                                            viewModel.skipQuestion()
                                        }
                                    }
                                }
                                dragDistance = 0f
                            }
                        ) { change, _ ->
                            val deltaX = change.position.x - change.previousPosition.x
                            val deltaY = change.position.y - change.previousPosition.y

                            if (kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY) * 2f) {
                                dragDistance += deltaX
                            }
                        }
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (!isLandscape) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)

                    ) {
                        QuestionUI(currentQuestion)
                        OptionsUI(currentQuestion, viewModel, selectedTheme)

                    }
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            QuestionUI(currentQuestion)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            OptionsUI(currentQuestion, viewModel, selectedTheme)
                        }
                    }
                }
            }

            StreakSection(
                currentStreak = uiState.currentStreak,
                previousStreak = uiState.previousStreak,
                selectedTheme = selectedTheme
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuestionUI(currentQuestion: Question) {
    Text(
        text = currentQuestion.question,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
fun OptionsUI(currentQuestion: Question, viewmodel: QuestionViewModel, selectedTheme: AppTheme) {
    val uiState by viewmodel.uiState.collectAsState()
    currentQuestion.options.forEachIndexed { index, option ->
        OptionItem(
            text = option,
            isSelected = uiState.selectedAnswer == index,
            isCorrect = uiState.answerShown && index == currentQuestion.correctOptionIndex,
            isInCorrect = uiState.answerShown && index != currentQuestion.correctOptionIndex && uiState.selectedAnswer == index,
            selectedTheme = selectedTheme,
            onClick = {
                if (!uiState.answerShown) {
                    viewmodel.selectAnswer(index)
                }
            }
        )
    }
}