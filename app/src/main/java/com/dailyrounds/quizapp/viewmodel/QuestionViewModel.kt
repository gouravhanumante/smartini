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

    private var currentPage = 0
    private var totalPages = 1
    private val currentQuestions = mutableListOf<Question>()
    private var timerJob: kotlinx.coroutines.Job? = null
    private var currentModuleId: String? = null

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.value =
                QuestionUiState(result = com.dailyrounds.quizapp.network.Result.Loading)

            try {
                val questionsState = withContext(Dispatchers.IO) {
                    repository.getQuestionsAndMetadata(0, 20)
                }

                if (questionsState is com.dailyrounds.quizapp.network.Result.Success) {
                    val questionResponse = questionsState.data

                    currentPage = questionResponse.page
                    totalPages = (questionResponse.total + 20 - 1) / 20

                    currentQuestions.clear()
                    currentQuestions.addAll(questionResponse.questions)

                    _uiState.value = QuestionUiState(
                        result = com.dailyrounds.quizapp.network.Result.Success(
                            currentQuestions.toList()
                        )
                    )

                    if (questionResponse.hasMore) {
                        preloadRemainingQuestions()
                    }
                } else {
                    throw Exception("Network response unsuccessful")
                }

            } catch (e: Exception) {
                _uiState.value = QuestionUiState(
                    result = com.dailyrounds.quizapp.network.Result.Error(
                        e.message ?: "An exception occurred"
                    )
                )
            }
        }
    }

    private fun preloadRemainingQuestions() {
        viewModelScope.launch {
            try {
                while (currentPage < totalPages - 1) {
                    currentPage++
                    val response = withContext(Dispatchers.IO) {
                        repository.getQuestionsAndMetadata(currentPage, 20)
                    }

                    if (response is com.dailyrounds.quizapp.network.Result.Success) {
                        val questionResponse = response.data
                        val newQuestions = questionResponse.questions

                        currentQuestions.addAll(newQuestions)
                    } else {
                        break
                    }
                }
            } catch (e: Exception) {
            }
            //do nothing its background call in error casw
        }
    }

    fun retry() {
        loadQuestions()
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
            saveResult()
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
        return if (currentState.currentQuestionIndex < currentQuestions.size) {
            currentQuestions[currentState.currentQuestionIndex]
        } else null
    }

    fun isLastQuestion(): Boolean {
        val currentState = _uiState.value
        return currentState.currentQuestionIndex >= currentQuestions.size - 1
    }

    fun isQuizCompleted(): Boolean {
        val currentState = _uiState.value
        return currentState.currentQuestionIndex >= currentQuestions.size
    }

    fun getTotalQuestions(): Int {
        return currentQuestions.size
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

    fun saveResult() {
        val currentState = _uiState.value
        val moduleId = currentModuleId ?: ""
        val score = currentState.score
        val totalQuestions = getTotalQuestions()

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {

                    val moduleData = ModuleEntity(
                        moduleId = moduleId,
                        previousScore = score,
                        totalQuestions = totalQuestions,
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
        repository.clearCache()
        currentPage = 0
        totalPages = 1
        currentQuestions.clear()
        stopTimer()
        _uiState.value = QuestionUiState()
        loadQuestions()
    }

    fun loadQuestionsforModule(moduleId: String, questionsUrl: String) {
        currentModuleId = moduleId
        viewModelScope.launch {
            _uiState.value = QuestionUiState(result = Result.Loading)
            
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getQuestions(questionsUrl)
                }
                
                if (result is Result.Success) {
                    currentQuestions.clear()
                    currentQuestions.addAll(result.data ?: emptyList())
                    
                    _uiState.value = QuestionUiState(
                        result = Result.Success(result.data ?: emptyList())
                    )
                } else {
                    _uiState.value = QuestionUiState(
                        result = Result.Error(
                            (result as Result.Error).message
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = QuestionUiState(
                    result = Result.Error(
                        e.message ?: "Failed to load module questions"
                    )
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
