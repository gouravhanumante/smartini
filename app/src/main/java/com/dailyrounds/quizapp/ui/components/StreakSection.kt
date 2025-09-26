package com.dailyrounds.quizapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dailyrounds.quizapp.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun StreakSection(
    currentStreak: Int, previousStreak: Int, selectedTheme: AppTheme, modifier: Modifier = Modifier
) {
    var showSmoke by remember { mutableStateOf(false) }
    var smokeAnimationTrigger by remember { mutableIntStateOf(0) }
    var showFire by remember { mutableStateOf(true) }

    val fireAnimation = when {
        currentStreak >= 7 -> "fire_4.json"
        currentStreak >= 5 -> "fire_3.json"
        currentStreak >= 3 -> "fire_2.json"
        currentStreak >= 1 -> "fire_1.json"
        else -> null
    }

    LaunchedEffect(currentStreak, previousStreak) {
        if (previousStreak > 0 && currentStreak == 0) {
            showSmoke = true
            showFire = false
            smokeAnimationTrigger++
            delay(3000)
            showSmoke = false
            showFire = true
        } else if (currentStreak > 0) {
            showSmoke = false
            showFire = true
        }
    }

    if (showSmoke) {
        SmokeAnimation(
            smokeAnimationTrigger,
            selectedTheme = selectedTheme,
            modifier = modifier
        )
    } else if (showFire && fireAnimation != null) {
        FireAnimation(
            animationFile = fireAnimation, streakCount = currentStreak, selectedTheme = selectedTheme, modifier = modifier
        )
    }
}

@Composable
private fun FireAnimation(
    animationFile: String, streakCount: Int, selectedTheme: AppTheme, modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset(animationFile)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition, iterations = Int.MAX_VALUE
    )

    val animationSize = (30.dp + (streakCount * 8.dp)).coerceAtMost(100.dp)

    val scale by animateFloatAsState(
        targetValue = 1f + (streakCount * 0.1f).coerceAtMost(0.5f),
        animationSpec = tween(durationMillis = 500),
        label = "fire_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(animationSize)
                .scale(scale)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Streak: $streakCount",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SmokeAnimation(
    smokeAnimationTrigger: Int,
    selectedTheme: AppTheme,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("smoke_1.json")
    )

    val progress by animateLottieCompositionAsState(
        composition = composition, iterations = 1, restartOnPlay = true
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition, progress = { progress }, modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Streak Broken!", 
            fontSize = 16.sp, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

