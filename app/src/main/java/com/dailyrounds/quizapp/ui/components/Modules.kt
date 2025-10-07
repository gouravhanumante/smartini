package com.dailyrounds.quizapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dailyrounds.quizapp.data.Module
import com.dailyrounds.quizapp.db.ModuleEntity
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.repository.ModulesRepository
import com.dailyrounds.quizapp.ui.screen.ResultScreen
import com.dailyrounds.quizapp.ui.theme.AppTheme
import com.dailyrounds.quizapp.ui.theme.ButtonStore
import com.dailyrounds.quizapp.viewmodel.ModulesViewModel

@Composable
fun Modules(
    moduleViewModel: ModulesViewModel,
    repository: ModulesRepository,
    selectedTheme: AppTheme
) {
    val uiState by moduleViewModel.uiState.collectAsState()
    val result = uiState.result
    when (result) {
        is Result.Error<*> -> {
            Text("Unable to fetch modules")
        }

        Result.Loading -> {
//            LoadingIndicator()
        }

        is Result.Success -> {
            ModulesUI(result.data, moduleViewModel, repository, selectedTheme)
        }
    }
}

@Composable
fun ModulesUI(
    result: List<Module>,
    moduleViewModel: ModulesViewModel,
    repository: ModulesRepository,
    selectedTheme: AppTheme
) {
    Column {
        for (module in result) {
            ModuleItem(
                module, moduleViewModel, repository, selectedTheme,
                onClickModule = {  }
            )
        }
    }
}

@Composable
fun ModuleItem(
    module: Module,
    moduleViewModel: ModulesViewModel,
    repository: ModulesRepository,
    selectedTheme: AppTheme,
    onClickModule :(ModuleEntity?) -> Unit
) {
    var dbModuleData = remember { mutableStateOf<ModuleEntity?>(null) }
    LaunchedEffect(module) {
        dbModuleData.value = withContext(Dispatchers.IO) {
            repository.fetchCompletionStatus(module.id)
        }
    }

    val isCompleted = dbModuleData.value?.isCompleted ?: false
    val score = dbModuleData.value?.previousScore
    val totalQuestions = dbModuleData.value?.totalQuestions

    val showResult = remember { mutableStateOf(false) }
    Card(Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(vertical = 10.dp)
        .clickable {
                moduleViewModel.selectModule(module)
//            onClickModule.invoke(dbModuleData.value)
//            if (isCompleted == true) {
//                showResult.value = true
//
//            } else {
//                showResult.value = false
//            }
        }) {
//        if (showResult.value) {
//            ResultScreen(
//                score = score ?: 0,
//                totalQuestions = totalQuestions ?: 10,
//                highestStreak = 0,
//                skippedQuestions = 0,
//                selectedTheme = selectedTheme,
//                onRestartQuiz = {}
//            ) { }
//        } else {
//        }
            Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp)) {
                Text(text = module.title)
                val text = if (isCompleted) "Review" else "Start Quiz"
                ButtonStore.PrimaryButton(text, onClick = {})
                Text(text = dbModuleData.value?.isCompleted?.toString() ?: "false")
                val score = dbModuleData.value?.previousScore ?: 0
                if (score!= 0) {
                    Text(text = "Score $score")
                }
            }
    }
}
