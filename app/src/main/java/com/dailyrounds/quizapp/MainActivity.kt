package com.dailyrounds.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.ui.screen.MainPage
import com.dailyrounds.quizapp.ui.screen.StartScreen
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.ui.theme.QuizAppTheme
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        enableEdgeToEdge()



        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var selectedTheme by remember { mutableStateOf(AppTheme.BLUE) }

            QuizAppTheme(
                selectedTheme = selectedTheme,
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                key(selectedTheme) {
                    val viewModel: QuestionViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsState()
                    val result = uiState.result

                    var showStartPage by rememberSaveable { mutableStateOf(true) }

                    LaunchedEffect(uiState) {
                        when (result) {
                            is Result.Success -> {
                                if (result.data.isNotEmpty()) {
                                    splashScreen.setKeepOnScreenCondition { false }
                                }
                                if (!viewModel.isQuizCompleted() && uiState.currentQuestionIndex > 0) {
                                    showStartPage = false
                                }
                            }

                            is Result.Error -> {
                                splashScreen.setKeepOnScreenCondition { false }
                            }

                            else -> {
                            }
                        }
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        if (showStartPage) {
                            StartScreen(
                                onStartQuiz = {
                                    viewModel.startQuiz()
                                    showStartPage = false
                                },
                                selectedTheme = selectedTheme,
                                onThemeChange = { theme -> selectedTheme = theme }
                            )
                        } else {
                            MainPage(viewModel, selectedTheme, onHome = {
                                viewModel.restartQuiz()
                                showStartPage = true
                            })
                        }
                    }
                }
            }
        }
    }
}

