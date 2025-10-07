package com.dailyrounds.quizapp.ui

import com.dailyrounds.quizapp.data.Module
import com.dailyrounds.quizapp.network.Result

data class ModuleUIState(
    val result: Result<List<Module>> = Result.Loading,
    val selectedModule: Module? = null
)