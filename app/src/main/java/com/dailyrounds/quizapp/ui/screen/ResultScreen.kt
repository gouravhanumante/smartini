package com.dailyrounds.quizapp.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailyrounds.quizapp.ui.components.FinalScore
import com.dailyrounds.quizapp.ui.components.SkipTile
import com.dailyrounds.quizapp.ui.components.StreakTile
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.ui.theme.ButtonStore
import com.dailyrounds.quizapp.ui.theme.QuizAppTheme
import com.dailyrounds.quizapp.utils.ScreenshotUtil

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    highestStreak: Int,
    skippedQuestions: Int,
    selectedTheme: AppTheme,
    onRestartQuiz: () -> Unit,
    onHome: () -> Unit,
    onRetakeQuiz: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val cardWidthPx = with(density) { (configuration.screenWidthDp.dp - 48.dp).roundToPx() }
    
    val percentage = if (totalQuestions > 0) (score * 100f / totalQuestions).toInt() else 0
    val successMsg = when {
        percentage >= 90 -> "Excellent! 🎉"
        percentage >= 80 -> "Great job! 👏"
        percentage >= 70 -> "Good work! 👍"
        percentage >= 60 -> "Not bad! 📚"
        else -> "Keep practicing! 💪"
    }
    
    val resultCardContent: @Composable () -> Unit = {
        ResultCard(score, totalQuestions, highestStreak, skippedQuestions)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        resultCardContent()

        Spacer(Modifier.height(20.dp))

        Text(
            text = successMsg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        
        ButtonStore.SecondaryButton(
            text = "Share Result",
            onClick = {
                android.util.Log.d("ResultScreen", "Share button clicked, cardWidthPx: $cardWidthPx")
                try {
                    ScreenshotUtil.captureAndShareComposable(
                        context = context,
                        widthPx = cardWidthPx
                    ) {
                        QuizAppTheme(selectedTheme, darkTheme = false, dynamicColor = false) {
                            resultCardContent()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ResultScreen", "Error in share button", e)
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onRetakeQuiz != null) {
                ButtonStore.SecondaryButton(
                    text = "Retake Quiz",
                    onClick = onRetakeQuiz,
                    modifier = Modifier.weight(1f)
                )
            }
            
            ButtonStore.PrimaryButton(
                text = "Go To Home",
                onClick = onHome,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ResultCard(
    score: Int,
    totalQuestions: Int,
    highestStreak: Int,
    skippedQuestions: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FinalScore(score, totalQuestions)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                StreakTile(highestStreak)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                SkipTile(skippedQuestions)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FinalScore(score, totalQuestions)
                    Spacer(Modifier.height(20.dp))
                    SkipTile(skippedQuestions)
                    Spacer(Modifier.height(20.dp))
                    StreakTile(highestStreak)
                }
                HorizontalDivider()
            }
        }
    }
}
