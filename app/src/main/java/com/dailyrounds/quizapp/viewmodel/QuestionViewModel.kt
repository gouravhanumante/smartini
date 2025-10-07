package com.dailyrounds.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.db.ModuleDao
import com.dailyrounds.quizapp.db.ModuleEntity
import com.dailyrounds.quizapp.network.Result
import com.dailyrounds.quizapp.repository.QuestionRepository
import com.dailyrounds.quizapp.ui.QuestionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


enum class AnimationDirection {
    NONE, FORWARD, BACKWARD
}

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val moduleDao: ModuleDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null



    fun retry() {
        _uiState.value = QuestionUiState(
            result = Result.Error(
                "Please go back and select a module to retry"
            )
        )
    }

    fun selectAnswer(optionIndex: Int) {
        val currentState = _uiState.value
        val currentQuestion = getCurrentQuestion()

        val isCorrect = currentQuestion?.correctOptionIndex == optionIndex
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        val previousStreak = currentState.currentStreak
        val newCurrentStreak = if (isCorrect) currentState.currentStreak + 1 else 0
        val newHighestStreak = maxOf(currentState.highestStreak, newCurrentStreak)

        stopTimer()
        _uiState.value = currentState.copy(
            selectedAnswer = optionIndex,
            answerShown = true,
            score = newScore,
            currentStreak = newCurrentStreak,
            previousStreak = previousStreak,
            highestStreak = newHighestStreak,
            timerActive = false
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val questions =
            (currentState.result as? com.dailyrounds.quizapp.network.Result.Success)?.data ?: return

        stopTimer()

        if (currentState.currentQuestionIndex < questions.size - 1) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                selectedAnswer = null,
                answerShown = false,
                skipButtonClicked = false,
                reachedViaSkip = currentState.skipButtonClicked,
                animationDirection = AnimationDirection.FORWARD,
                timeRemaining = 15,
                timerActive = true
            )
            startTimer()
        } else {
            _uiState.value = currentState.copy(
                currentQuestionIndex = questions.size,
                selectedAnswer = null,
                answerShown = false,
                skipButtonClicked = false,
                reachedViaSkip = false,
                animationDirection = AnimationDirection.FORWARD,
                timeRemaining = 15,
                timerActive = false
            )
        }
    }


    fun skipQuestion() {
        val currentState = _uiState.value
        stopTimer()
        _uiState.value = currentState.copy(
            skipButtonClicked = true, skippedQuestions = currentState.skippedQuestions + 1
        )
        nextQuestion()
    }

    fun undoSkip() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            stopTimer()
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1,
                selectedAnswer = null,
                answerShown = false,
                skipButtonClicked = false,
                reachedViaSkip = false,
                animationDirection = AnimationDirection.BACKWARD,
                skippedQuestions = currentState.skippedQuestions - 1,
                timeRemaining = 15,
                timerActive = true
            )
            startTimer()
        }
    }

    fun getCurrentQuestion(): Question? {
        val currentState = _uiState.value
        val questions = (currentState.result as? Result.Success)?.data ?: return null
        return if (currentState.currentQuestionIndex < questions.size) {
            questions[currentState.currentQuestionIndex]
        } else null
    }

    fun isLastQuestion(): Boolean {
        val currentState = _uiState.value
        val questions = (currentState.result as? Result.Success)?.data ?: return false
        return currentState.currentQuestionIndex >= questions.size - 1
    }

    fun isQuizCompleted(): Boolean {
        val currentState = _uiState.value
        val questions = (currentState.result as? Result.Success)?.data ?: return false
        return currentState.currentQuestionIndex >= questions.size
    }
    
    fun completeQuiz(moduleId: String = "") {
        val currentState = _uiState.value
        val idToUse = moduleId.ifEmpty { currentState.currentModuleId }
        saveResult(idToUse)
    }

    fun getTotalQuestions(): Int {
        val currentState = _uiState.value
        val questions = (currentState.result as? Result.Success)?.data ?: return 0
        return questions.size
    }

    fun getFinalScore(): Int {
        val currentState = _uiState.value
        return currentState.score
    }

    fun getHighestStreak(): Int {
        return _uiState.value.highestStreak
    }

    fun getSkippedQuestions(): Int {
        return _uiState.value.skippedQuestions
    }

    fun setPreviousResults(score: Int, totalQuestions: Int, highestStreak: Int = 0, skippedQuestions: Int = 0) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            score = score,
            currentQuestionIndex = totalQuestions, 
            timerActive = false,
            highestStreak = highestStreak,
            skippedQuestions = skippedQuestions
        )
    }

    fun saveResult(moduleId: String) {
        val currentState = _uiState.value
        val score = currentState.score
        val totalQuestions = getTotalQuestions()
        val highestStreak = currentState.highestStreak
        val skippedQuestions = currentState.skippedQuestions

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val moduleData = ModuleEntity(
                        moduleId = moduleId,
                        previousScore = score,
                        totalQuestions = totalQuestions,
                        highestStreak = highestStreak,
                        skippedQuestions = skippedQuestions,
                        isCompleted = true,
                    )
                    moduleDao.saveModuleData(moduleData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun resetAnimationDirection() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(animationDirection = AnimationDirection.NONE)
    }

    fun startQuiz() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            timerActive = true, timeRemaining = 15
        )
        startTimer()
    }

    fun restartQuiz() {
        stopTimer()
        _uiState.value = QuestionUiState(
            result = Result.Error(
                "Please select a module to load questions"
            )
        )
    }

    fun loadQuestionsforModule(moduleId: String, questionsUrl: String) {
        viewModelScope.launch {
            stopTimer()
            _uiState.value = QuestionUiState(
                result = Result.Loading,
                currentModuleId = moduleId
            )
            
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getQuestions(questionsUrl)
                }
                
                if (result is Result.Success) {
                    _uiState.value = QuestionUiState(
                        result = Result.Success(result.data ?: emptyList()),
                        currentModuleId = moduleId,
                        timerActive = true,
                        timeRemaining = 15
                    )
                    startTimer()
                } else {
                    _uiState.value = QuestionUiState(
                        result = Result.Error(
                            (result as Result.Error).message
                        ),
                        currentModuleId = moduleId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = QuestionUiState(
                    result = Result.Error(
                        e.message ?: "Failed to load module questions"
                    ),
                    currentModuleId = moduleId
                )
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && _uiState.value.timerActive) {
                delay(1000)
                val currentState = _uiState.value
                if (currentState.timerActive) {
                    val newTimeRemaining = currentState.timeRemaining - 1
                    _uiState.value = currentState.copy(timeRemaining = newTimeRemaining)

                    if (newTimeRemaining <= 0) {
                        autoSkipQuestion()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun autoSkipQuestion() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            skipButtonClicked = true,
            skippedQuestions = currentState.skippedQuestions + 1,
            timerActive = false
        )
        nextQuestion()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

}
