package com.dailyrounds.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.ui.theme.QuizAppTheme
import com.dailyrounds.quizapp.repository.QuestionRepository
import com.dailyrounds.quizapp.ui.screen.MainPage
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        enableEdgeToEdge()

        setContent {
            QuizAppTheme {
                val viewModel: QuestionViewModel = hiltViewModel()
                
                LaunchedEffect(viewModel) {
                    viewModel.uiState.collect { uiState ->
                        when (uiState.result) {
                            is Result.Success -> {
                                if (uiState.result.data.isNotEmpty()) {
                                    splashScreen.setKeepOnScreenCondition { false }
                                }
                            }
                            is Result.Error -> {
                                splashScreen.setKeepOnScreenCondition { false }
                            }
                            else -> {
                            }
                        }
                    }
                }

                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainPage(viewModel)
                }
            }
        }
    }
}

