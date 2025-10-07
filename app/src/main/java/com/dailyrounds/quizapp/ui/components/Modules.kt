package com.dailyrounds.quizapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dailyrounds.quizapp.data.Module
import com.dailyrounds.quizapp.db.ModuleEntity
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.repository.ModulesRepository
import com.dailyrounds.quizapp.ui.theme.AppTheme
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
                module, moduleViewModel, repository, selectedTheme
            )
        }
    }
}

@Composable
fun ModuleItem(
    module: Module,
    moduleViewModel: ModulesViewModel,
    repository: ModulesRepository,
    selectedTheme: AppTheme
) {
    var dbModuleData = remember { mutableStateOf<ModuleEntity?>(null) }
    LaunchedEffect(module) {
        dbModuleData.value = withContext(Dispatchers.IO) {
            repository.fetchCompletionStatus(module.id)
        }
    }

    val isCompleted = dbModuleData.value?.isCompleted ?: false
    val score = dbModuleData.value?.previousScore ?: 0

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(vertical = 10.dp)
            .clickable {
                moduleViewModel.selectModule(module)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 8.dp else 4.dp
        )
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (isCompleted && score > 0) {
                Text(
                    text = "Previous Score: $score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
                val buttonText = if (isCompleted) "Review Results" else "Start Quiz"
                OutlinedButton(
                    onClick = {
                        moduleViewModel.selectModule(module)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = buttonText)
                }
        }
    }
}
