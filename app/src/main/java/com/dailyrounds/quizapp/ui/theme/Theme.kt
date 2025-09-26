package com.dailyrounds.quizapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


enum class AppTheme(val displayName: String,) {
    BLUE("Blue"),
    GREEN("Green"),
    ORANGE("Orange"),
    PURPLE("Purple")
}


private val BlueDarkTheme = darkColorScheme(
    primary = Color(0xFF81C4FF),
    secondary = Color(0xFF26C6DA),
    tertiary = Color(0xFF4DD0E1),
    background = Color(0xFF0A1628)
)

private val BlueLightTheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    secondary = Color(0xFF4FC3F7),
    tertiary = Color(0xFF00BCD4),
    background = Color(0xFFF8FDFF)
)


private val OrangeDarkTheme = darkColorScheme(
    primary = Color(0xFFFFCC02),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF2A1A0A)
)

private val OrangeLightTheme = lightColorScheme(
    primary = Color(0xFFFF9800),
    secondary = Color(0xFFFF7043),
    tertiary = Color(0xFFFFB300),
    background = Color(0xFFFFF8F1)
)

private val PurpleDarkTheme = darkColorScheme(
    primary = Color(0xFFB39DDB),
    secondary = Color(0xFFCE93D8),
    tertiary = Color(0xFFBA68C8),
    background = Color(0xFF1E0A2A)
)

private val PurpleLightTheme = lightColorScheme(
    primary = Color(0xFF673AB7),
    secondary = Color(0xFF9C27B0),
    tertiary = Color(0xFF8E24AA),
    background = Color(0xFFF8F1FF)
)

private val GreenDarkTheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF66BB6A),
    background = Color(0xFF0D1F0F)
)
private val GreenLightTheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF8BC34A),
    tertiary = Color(0xFF009688),
    background = Color(0xFFF1F8F1)
)

fun getTheme(theme: AppTheme): Pair<ColorScheme, ColorScheme> {
    return when (theme) {
        AppTheme.BLUE -> Pair(BlueLightTheme, BlueDarkTheme)
        AppTheme.GREEN -> Pair(GreenLightTheme, GreenDarkTheme)
        AppTheme.ORANGE -> Pair(OrangeLightTheme, OrangeDarkTheme)
        AppTheme.PURPLE -> Pair(PurpleLightTheme, PurpleDarkTheme)
    }
}



@Composable
fun QuizAppTheme(
    selectedTheme: AppTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val (light, dark) = getTheme(selectedTheme)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> dark
        else -> light
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}