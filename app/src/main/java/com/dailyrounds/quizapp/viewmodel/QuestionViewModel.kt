package com.dailyrounds.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrounds.quizapp.data.Question
import com.dailyrounds.quizapp.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class QuestionUiState(
    val result: com.dailyrounds.quizapp.network.Result<List<Question>> = com.dailyrounds.quizapp.network.Result.Loading,
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val answerShown: Boolean = false,
    val score: Int = 0,
    val skipButtonClicked: Boolean = false,
    val reachedViaSkip: Boolean = false,
    val animationDirection: AnimationDirection = AnimationDirection.NONE,
    val dragOffset: Float = 0f,
    val isDragging: Boolean = false,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val skippedQuestions: Int = 0
)

enum class AnimationDirection {
    NONE, FORWARD, BACKWARD
}

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var totalPages = 1
    private val currentQuestions = mutableListOf<Question>()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState(result = com.dailyrounds.quizapp.network.Result.Loading)

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

                    _uiState.value =
                        QuestionUiState(result = com.dailyrounds.quizapp.network.Result.Success(currentQuestions.toList()))

                    if (questionResponse.hasMore) {
                        preloadRemainingQuestions()
                    }
                } else {
                    throw Exception("Network response unsuccessful")
                }

            } catch (e: Exception) {
                _uiState.value =
                    QuestionUiState(result = com.dailyrounds.quizapp.network.Result.Error(e.message ?: "An exception occurred"))
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

        val newCurrentStreak = if (isCorrect) currentState.currentStreak + 1 else 0
        val newHighestStreak = maxOf(currentState.highestStreak, newCurrentStreak)

        _uiState.value = currentState.copy(
            selectedAnswer = optionIndex,
            answerShown = true,
            score = newScore,
            currentStreak = newCurrentStreak,
            highestStreak = newHighestStreak
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val questions = (currentState.result as? com.dailyrounds.quizapp.network.Result.Success)?.data ?: return

        if (currentState.currentQuestionIndex < questions.size - 1) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1,
                selectedAnswer = null,
                answerShown = false,
                skipButtonClicked = false,
                reachedViaSkip = currentState.skipButtonClicked,
                animationDirection = AnimationDirection.FORWARD
            )
        } else {
            _uiState.value = currentState.copy(
                currentQuestionIndex = questions.size,
                selectedAnswer = null,
                answerShown = false,
                skipButtonClicked = false,
                reachedViaSkip = false,
                animationDirection = AnimationDirection.FORWARD
            )
        }
    }


    fun skipQuestion() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            skipButtonClicked = true,
            skippedQuestions = currentState.skippedQuestions + 1
        )
        nextQuestion()
    }

    fun undoSkip() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1,
                selectedAnswer = null,
                answerShown = false,
                skipButtonClicked = false,
                reachedViaSkip = false,
                animationDirection = AnimationDirection.BACKWARD,
                skippedQuestions = currentState.skippedQuestions - 1
            )
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

    fun restartQuiz() {
        repository.clearCache()
        currentPage = 0
        totalPages = 1
        currentQuestions.clear()
        _uiState.value = QuestionUiState()
        loadQuestions()
    }

    fun updateDragOffset(offset: Float) {
        _uiState.value = _uiState.value.copy(dragOffset = offset)
    }

    fun startDragging() {
        _uiState.value = _uiState.value.copy(isDragging = true)
    }

    fun stopDragging() {
        _uiState.value = _uiState.value.copy(isDragging = false)
    }

    fun snapBack() {
        _uiState.value = _uiState.value.copy(dragOffset = 0f, isDragging = false)
    }


}
