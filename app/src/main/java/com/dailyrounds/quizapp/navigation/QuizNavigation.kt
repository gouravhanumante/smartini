package com.dailyrounds.quizapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailyrounds.quizapp.ui.screen.MainPage
import com.dailyrounds.quizapp.ui.screen.QuizScreen
import com.dailyrounds.quizapp.ui.screen.ResultScreen
import com.dailyrounds.quizapp.ui.screen.StartScreen
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.viewmodel.ModulesViewModel
import com.dailyrounds.quizapp.viewmodel.QuestionViewModel
import com.dailyrounds.quizapp.repository.ModulesRepository

object QuizRoutes {
    const val START_SCREEN = "start_screen"
    const val QUIZ_SCREEN = "quiz_screen"
    const val RESULT_SCREEN = "result_screen"
    const val MAIN_PAGE = "main_page"
}

@Composable
fun QuizNavigation(
    navController: NavHostController = rememberNavController(),
    questionViewModel: QuestionViewModel,
    modulesViewModel: ModulesViewModel,
    modulesRepository: ModulesRepository,
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = QuizRoutes.START_SCREEN
    ) {
        composable(QuizRoutes.START_SCREEN) {
            StartScreen(
                onStartQuiz = {
                    questionViewModel.startQuiz()
                    navController.navigate(QuizRoutes.MAIN_PAGE)
                },
                selectedTheme = selectedTheme,
                onThemeChange = onThemeChange,
                moduleViewModel = modulesViewModel,
                repository = modulesRepository
            )
        }
        
        composable(QuizRoutes.MAIN_PAGE) {
            MainPage(
                viewModel = questionViewModel,
                selectedTheme = selectedTheme,
                onNavigateToResult = {
                    navController.navigate(QuizRoutes.RESULT_SCREEN)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(QuizRoutes.QUIZ_SCREEN) {
            QuizScreen(
                viewModel = questionViewModel,
                selectedTheme = selectedTheme,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(QuizRoutes.RESULT_SCREEN) {
            ResultScreen(
                score = questionViewModel.getFinalScore(),
                totalQuestions = questionViewModel.getTotalQuestions(),
                highestStreak = questionViewModel.getHighestStreak(),
                skippedQuestions = questionViewModel.getSkippedQuestions(),
                selectedTheme = selectedTheme,
                onRestartQuiz = {
                    val selectedModule = modulesViewModel.uiState.value.selectedModule
                    selectedModule?.let { module ->
                        questionViewModel.loadQuestionsforModule(module.id, module.questions_url)
                        navController.navigate(QuizRoutes.MAIN_PAGE)
                    }
                },
                onHome = {
                    questionViewModel.restartQuiz()
                    modulesViewModel.clearSelection()
                    navController.navigate(QuizRoutes.START_SCREEN) {
                        popUpTo(QuizRoutes.START_SCREEN) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
