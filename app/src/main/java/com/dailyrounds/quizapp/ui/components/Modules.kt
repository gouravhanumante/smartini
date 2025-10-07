package com.dailyrounds.quizapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
            CompactErrorScreen(
                message = result.message,
                onRetry = { moduleViewModel.fetchModules() }
            )
        }

        Result.Loading -> {
            CompactLoadingIndicator(
                message = "Loading modules..."
            )
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
                onClickModule = { }
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
    onClickModule: (ModuleEntity?) -> Unit
) {
    var dbModuleData = remember { mutableStateOf<ModuleEntity?>(null) }
    LaunchedEffect(module) {
        dbModuleData.value = withContext(Dispatchers.IO) {
            repository.fetchCompletionStatus(module.id)
        }
    }

    val isCompleted = dbModuleData.value?.isCompleted ?: false
    val score = dbModuleData.value?.previousScore ?: 0

    val cardColor = if (isCompleted) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(vertical = 10.dp)
            .clickable {
                moduleViewModel.selectModule(module)
            },
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
        )
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp)) {
            Text(text = module.title)
            val text = if (isCompleted) "Review" else "Start Quiz"
            if (score != 0) {
                Text(text = "Score $score")
            }
            ButtonStore.PrimaryButton(text, onClick = {})
        }
    }
}
