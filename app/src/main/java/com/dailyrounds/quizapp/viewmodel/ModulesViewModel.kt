package com.dailyrounds.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrounds.quizapp.data.Module
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.repository.ModulesRepository
import com.dailyrounds.quizapp.ui.ModuleUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ModulesViewModel @Inject constructor(private val repository: ModulesRepository): ViewModel() {

    private val _uiState = MutableStateFlow<ModuleUIState>(ModuleUIState())
    val uiState = _uiState.asStateFlow()

    fun fetchModules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(result = Result.Loading)

            try {
                val result = withContext(Dispatchers.IO) {
                    repository.fetchModulesFromJson()
                }
                if (result is Result.Success) {
                    _uiState.value = _uiState.value.copy(
                        result = Result.Success(result.data)
                    )
                } else if (result is Result.Error) {
                    _uiState.value = _uiState.value.copy(
                        result = Result.Error(result.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    result = Result.Error("Failed to fetch modules: ${e.message}")
                )
            }
        }
    }

    fun selectModule(module: Module, isRetake: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            selectedModule = module,
            isRetake = isRetake
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedModule = null,
            isRetake = false
        )
    }


}