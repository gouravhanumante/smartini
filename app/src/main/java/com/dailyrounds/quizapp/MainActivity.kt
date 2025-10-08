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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dailyrounds.quizapp.navigation.QuizNavigation
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.repository.ModulesRepository
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.ui.theme.QuizAppTheme
import com.dailyrounds.quizapp.viewmodel.ModulesViewModel
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modulesRepository: ModulesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        enableEdgeToEdge()

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var selectedTheme by rememberSaveable { mutableStateOf(AppTheme.BLUE) }

            QuizAppTheme(
                selectedTheme = selectedTheme,
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                val viewModel: QuestionViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                val moduleViewModel: ModulesViewModel = hiltViewModel()
                val moduleUIState by moduleViewModel.uiState.collectAsState()

                val result = uiState.result
                val navController = rememberNavController()

                LaunchedEffect(uiState) {
                    when (result) {
                        is Result.Success -> {
                            if (result.data.isNotEmpty()) {
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

                LaunchedEffect(moduleUIState.selectedModule, moduleUIState.isRetake) {
                    val selected = moduleUIState.selectedModule
                    if (selected != null) {
                        val completion = modulesRepository.fetchCompletionStatus(selected.id)
                        
                        if (moduleUIState.isRetake) {
                            viewModel.retakeQuiz(selected.id, selected.questions_url)
                            navController.navigate("main_page") {
                                launchSingleTop = true
                            }
                            moduleViewModel.clearSelection()
                        } 
                        else if (completion?.isCompleted == true) {
                            viewModel.setPreviousResults(
                                score = completion.previousScore,
                                totalQuestions = completion.totalQuestions,
                                highestStreak = completion.highestStreak,
                                skippedQuestions = completion.skippedQuestions
                            )
                            navController.navigate("result_screen") {
                                launchSingleTop = true
                            }
                            moduleViewModel.clearSelection()
                        } 
                        else {
                            viewModel.loadQuestionsforModule(
                                selected.id,
                                selected.questions_url
                            )
                            navController.navigate("main_page") {
                                launchSingleTop = true
                            }
                            moduleViewModel.clearSelection()
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QuizNavigation(
                        navController = navController,
                        questionViewModel = viewModel,
                        modulesViewModel = moduleViewModel,
                        modulesRepository = modulesRepository,
                        selectedTheme = selectedTheme,
                        onThemeChange = { theme ->
                            selectedTheme = theme
                        }
                    )
                }
            }
        }
    }
}

